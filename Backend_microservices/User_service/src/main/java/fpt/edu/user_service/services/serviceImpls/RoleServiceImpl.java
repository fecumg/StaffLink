package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.RoleRequest;
import fpt.edu.user_service.dtos.responseDtos.RoleResponse;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.Role;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.*;
import fpt.edu.user_service.services.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

//        set functions
        role.setFunctions(this.getAssignedFunctions(roleRequest));

        Role newRole = roleRepository.save(role);

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

            role.setId(id);

//            set functions
            role.setFunctions(this.getAssignedFunctions(roleRequest));

            Role editedRole = roleRepository.save(role);

//            send message to gateway to update authenticatedUser caches
            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(editedRole);
            this.sendMessageToUpdateAuthCache(exchangeUsers);

            return this.get(editedRole.getId());
        } else {
            throw new NotFoundException("Role not found");
        }
    }

    private List<Function> getAssignedFunctions(RoleRequest roleRequest) {
        List<Function> functions = new ArrayList<>();

        List<Integer> functionIds = roleRequest.getFunctionIds();
        if (functionIds != null && !functionIds.isEmpty()) {
            for (int functionId: functionIds) {
                Optional<Function> optionalFunction = functionRepository.findById(functionId);
                optionalFunction.ifPresent(functions::add);
            }
        }
        return functions;
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
        List<Role> roles = Pagination.retrieve(
                pagination,
                () -> roleRepository.findAll(),
                pageRequest -> roleRepository.findAll(pageRequest).getContent(),
                sort -> roleRepository.findAll(sort),
                Role.class
        );
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

            roleRepository.delete(role);

//            send message to gateway to update authenticatedUser cache
            this.sendMessageToUpdateAuthCache(exchangeUsers);
        } else {
            throw new NotFoundException("Role not found");
        }
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 60000)
    public void cacheEvict() {
        super.clearCache(getAllMethodCache, getMethodCache);
    }
}
