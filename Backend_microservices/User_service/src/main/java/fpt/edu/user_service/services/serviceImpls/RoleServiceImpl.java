package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.RoleRequest;
import fpt.edu.user_service.dtos.responseDtos.RoleResponse;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.Role;
import fpt.edu.user_service.entities.RoleFunctionMapping;
import fpt.edu.user_service.entities.UserRoleMapping;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.*;
import fpt.edu.user_service.services.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class RoleServiceImpl extends BaseService implements RoleService {

    private static final String getAllMethodCache = "allRoleResponses";
    private static final String getMethodCache = "roleResponses";

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private RoleFunctionMappingRepository roleFunctionMappingRepository;
    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Override
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#result.getId()")
    public RoleResponse save(RoleRequest roleRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Role role = modelMapper.map(roleRequest, Role.class);

//        set createdBy
        setCreatedBy(role, request);

        Role newRole = roleRepository.save(role);

//        new role-function-mappings
        this.newRoleFunctionMappings(roleRequest, newRole, request);

        return this.get(newRole.getId());
    }

    @Override
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#id")
    public RoleResponse update(int id, RoleRequest roleRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            Role role = modelMapper.map(roleRequest, Role.class);

//            set updatedBy
            setUpdatedBy(role, request);

//            delete all existing function assignments (needs experiments)
            Role currentRole = optionalRole.get();
            List<RoleFunctionMapping> roleFunctionMappings = currentRole.getRoleFunctionMappings();
            if (!roleFunctionMappings.isEmpty()) {
                roleFunctionMappingRepository.deleteAll(roleFunctionMappings);
            }

            role.setId(id);

            Role editedRole = roleRepository.save(role);

//            new role-function-mappings
            this.newRoleFunctionMappings(roleRequest, editedRole, request);

//            send message to gateway to update authenticatedUser caches
            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(editedRole);
            this.sendMessageToUpdateAuthCache(exchangeUsers);

            return this.get(editedRole.getId());
        } else {
            throw new NotFoundException("Role not found");
        }
    }

    private void newRoleFunctionMappings(RoleRequest roleRequest, Role role, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<RoleFunctionMapping> roleFunctionMappings = new ArrayList<>();

        List<Integer> functionIds = roleRequest.getFunctionIds();
        if (functionIds != null && !functionIds.isEmpty()) {
            for (int functionId: functionIds) {
                Optional<Function> optionalFunction = functionRepository.findById(functionId);
                if (optionalFunction.isPresent()) {
                    RoleFunctionMapping roleFunctionMapping = new RoleFunctionMapping(role, optionalFunction.get());
                    this.setCreatedBy(roleFunctionMapping, request);
                    roleFunctionMappings.add(roleFunctionMapping);
                }
            }
        }
        roleFunctionMappingRepository.saveAll(roleFunctionMappings);
    }

    private List<ExchangeUser> getAffectedExchangeUser(Role role) {
//        get all exchangeUsers that has been assigned to current role
        return userRepository.findAllByRoleId(role.getId()).stream()
                .map(ExchangeUser::build)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = getAllMethodCache, key = "#pagination.getPageNumber()")
    public List<RoleResponse> getAll(Pagination pagination) {
        List<Role> roles;
        if (pagination == null) {
            roles =  roleRepository.findAll();
        } else {
            PageRequest pageRequest = Pagination.getPageRequest(pagination);
            roles =  roleRepository.findAll(pageRequest).getContent();
        }
        return modelMapper.map(roles, new TypeToken<List<RoleResponse>>() {}.getType());

    }

    @Override
    @Cacheable(value = getMethodCache, key = "#id")
    public RoleResponse get(int id) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            return modelMapper.map(optionalRole.get(), RoleResponse.class);
        } else {
            throw new NotFoundException("Role not found");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = getAllMethodCache),
            @CacheEvict(value = getMethodCache, key = "#id")
            }
    )
    public void delete(int id) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();

            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(optionalRole.get());

            roleRepository.deleteById(id);

//            delete all existing function assignments (needs experiments)
            List<RoleFunctionMapping> roleFunctionMappings = role.getRoleFunctionMappings();
            if (!roleFunctionMappings.isEmpty()) {
                roleFunctionMappingRepository.deleteAll(roleFunctionMappings);
            }

//            delete all existing user-role-mappings
            List<UserRoleMapping> userRoleMappings = role.getUserRoleMapping();
            if (!userRoleMappings.isEmpty()) {
                userRoleMappingRepository.deleteAll(userRoleMappings);
            }

//            send message to gateway to update authenticatedUser cache
            this.sendMessageToUpdateAuthCache(exchangeUsers);
        } else {
            throw new NotFoundException("Role not found");
        }
    }

    @CacheEvict(cacheNames = { getAllMethodCache, getMethodCache })
    @Scheduled(fixedDelay = 6000)
    public void cacheEvict() {
    }
}
