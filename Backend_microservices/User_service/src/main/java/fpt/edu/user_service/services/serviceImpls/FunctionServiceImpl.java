package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.FunctionRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.exceptions.UnauthorizedException;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.FunctionRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.services.FunctionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class FunctionServiceImpl extends BaseService implements FunctionService {

    private static final String getAllMethodCache = "allFunctionResponses";
    private static final String getMethodCache = "FunctionResponses";

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;
    @Value("${rabbitmq.routing-key.guarded-path-update}")
    private String PATH_UPDATE_ROUTING_KEY;
    @Value("${rabbitmq.routing-key.guarded-path-delete}")
    private String PATH_DELETE_ROUTING_KEY;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Override
//    @CacheEvict(value = getAllMethodCache, allEntries = true)
//    @CachePut(value = getMethodCache, key = "#result.getId()")
    public FunctionResponse save(FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException {

//        check unique uri
        if (StringUtils.isNotEmpty(functionRequest.getUri()) && functionRepository.existsByUri(functionRequest.getUri())) {
            throw new UniqueKeyViolationException("Uri already exists");
        }

        Function function = modelMapper.map(functionRequest, Function.class);

//        set createdBy
        setCreatedBy(function, request);

//        set parent
        setNewParent(functionRequest, function);

        Function newFunction = functionRepository.save(function);

//        send message to demand auth-gateway to update guardedPaths redis cache
        rabbitTemplate.convertAndSend(EXCHANGE, PATH_UPDATE_ROUTING_KEY, newFunction.getUri());

        return modelMapper.map(newFunction, FunctionResponse.class);
    }

    @Override
//    @CacheEvict(value = getAllMethodCache, allEntries = true)
//    @CachePut(value = getMethodCache, key = "#id")
    public FunctionResponse update(int id, FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException {
        Optional<Function> optionalFunction = functionRepository.findById(id);
        if (optionalFunction.isPresent()) {
            Function currentFunction = optionalFunction.get();

//            check unique uri
            if (
                    StringUtils.isNotEmpty(functionRequest.getUri()) &&
                    functionRepository.existsByUriAndUriIsNot(functionRequest.getUri(), currentFunction.getUri())) {
                throw new UniqueKeyViolationException("Uri already exists");
            }

            Function function = modelMapper.map(functionRequest, Function.class);

//            set updatedBy
            setUpdatedBy(function, request);

//            set parent
            setEditParent(functionRequest, function, currentFunction);

            function.setId(id);
            Function editedFunction = functionRepository.save(function);

//            send message to demand auth-gateway to update guardedPaths redis cache
            this.sendMessageToUpdatePathCache(currentFunction.getUri());
            if (!functionRequest.getUri().equals(currentFunction.getUri())) {
                this.sendMessageToDeletePathCache(currentFunction.getUri());
            }

//            send message to demand auth-gateway to update authenticatedUser redis cache
            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(editedFunction);
            this.sendMessageToUpdateAuthCache(exchangeUsers);

            return modelMapper.map(editedFunction, FunctionResponse.class);
        } else {
            throw new NotFoundException("function not found");
        }
    }

    private void setNewParent(FunctionRequest functionRequest, Function function) {
        int parentId = functionRequest.getParentId();
        if (parentId == 0) {
            function.setParent(null);
        } else {
            Function parent = functionRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent function not found"));

            function.setParent(parent);
        }
    }

    private void setEditParent(FunctionRequest functionRequest, Function function, Function currentFunction) {
        int parentId = functionRequest.getParentId();
        if (parentId == 0) {
            function.setParent(null);
        } else {
            Function parent = functionRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent function not found"));

            List<Function> descendants = this.getDescendants(currentFunction);

            if (descendants.stream().anyMatch(descendant -> descendant.getId() == parentId)) {
                throw new BadRequestException("Function cannot set a child as its parent");
            }

            function.setParent(parent);
        }
    }

    private List<Function> getDescendants(Function function) {
        List<Function> descendants = new ArrayList<>();
        List<Function> children = function.getChildren();
        if (children != null && children.size() > 0) {
            function.getChildren().forEach(child -> {
                descendants.add(child);
                descendants.addAll(getDescendants(child));
            });
        }
        return descendants;
    }

    private List<ExchangeUser> getAffectedExchangeUser(Function function) {
//        get all exchangeUsers that has been assigned to current role
        return userRepository.findAllByFunctionId(function.getId()).stream()
                .map(ExchangeUser::build)
                .collect(Collectors.toList());
    }

    private void sendMessageToUpdatePathCache(String uri) {
        rabbitTemplate.convertAndSend(EXCHANGE, PATH_UPDATE_ROUTING_KEY, uri);
        log.info("Message has been published to routing key '{}'", PATH_UPDATE_ROUTING_KEY);
    }

    private void sendMessageToDeletePathCache(String uri) {
        rabbitTemplate.convertAndSend(EXCHANGE, PATH_DELETE_ROUTING_KEY, uri);
        log.info("Message has been published to routing key '{}'", PATH_DELETE_ROUTING_KEY);
    }

    @Override
//    @Cacheable(value = getAllMethodCache, key = "#pagination.getPageNumber()")
    public List<FunctionResponse> getAll(Pagination pagination) {
        List<Function> functions = Pagination.retrieve(
                pagination,
                () -> functionRepository.findAll(),
                pageRequest -> functionRepository.findAll(pageRequest).getContent(),
                sort -> functionRepository.findAll(sort),
                Function.class
        );
        List<Function> rearrangedFunctions = this.rearrange(functions, null);
        return modelMapper.map(rearrangedFunctions, new TypeToken<List<FunctionResponse>>() {}.getType());
    }

    @Override
//    @Cacheable(value = getMethodCache, key = "#id")
    public FunctionResponse get(int id) {
        Function function = functionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found"));
        return modelMapper.map(function, FunctionResponse.class);
    }

    @Override
//    @Caching(evict = {
//            @CacheEvict(value = getAllMethodCache),
//            @CacheEvict(value = getMethodCache, key = "#id")
//        }
//    )
    public void delete(int id) {
        Optional<Function> optionalFunction = functionRepository.findById(id);
        if (optionalFunction.isPresent()) {
            Function function = optionalFunction.get();
            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(function);

//            send message to demand auth-gateway to update guardedPaths redis cache
            this.sendMessageToDeletePathCache(function.getUri());

            functionRepository.delete(function);

//            send message to demand auth-gateway to update authenticatedUser redis cache
            this.sendMessageToUpdateAuthCache(exchangeUsers);
        } else {
            throw new NotFoundException("Function not found");
        }
    }

    @Override
    public List<FunctionResponse> getAuthorizedFunctions(HttpServletRequest request) {
        String authUserIdString = request.getHeader(super.AUTH_ID);
        if (StringUtils.isEmpty(authUserIdString)) {
            throw new UnauthorizedException("Unauthorized");
        }
        int authUserId = Integer.parseInt(authUserIdString);
        User authUser = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Function> authorizedFunctions = authUser.getRoles().stream()
                .flatMap(role -> role.getFunctions().stream())
                .distinct()
                .toList();

        List<Function> rearrangedAuthorizedFunctions = this.rearrange(authorizedFunctions, null);

        return modelMapper.map(rearrangedAuthorizedFunctions, new TypeToken<List<FunctionResponse>>() {}.getType());
    }

    @Override
    public List<FunctionResponse> getPotentialParentFunctions(int id) {
        List<Function> allFunctions =  functionRepository.findAll();
        Function currentFunction = functionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found"));

        List<Function> currentDescendants = this.getDescendants(currentFunction);

        List<Function> potentialParents = allFunctions.stream()
                .filter(
                        function -> function.getId() != currentFunction.getId() &&
                                currentDescendants.stream().noneMatch(descendant -> descendant.getId() == function.getId())
                )
                .toList();

        List<Function> rearrangedPotentialParents = this.rearrange(potentialParents, null);

        return modelMapper.map(rearrangedPotentialParents, new TypeToken<List<FunctionResponse>>() {}.getType());
    }

    private List<Function> rearrange(List<Function> functions, Function initialParent) {
        List<Function> rearrangedList = new ArrayList<>();
        functions.forEach(function -> {
            if ((initialParent == null && function.getParent() == null) ||
                    (initialParent != null && function.getParent() != null && initialParent.getId() == function.getParent().getId())) {
                rearrangedList.add(function);
                rearrangedList.addAll(rearrange(functions, function));
            }
        });
        return rearrangedList;
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 60000)
    public void cacheEvict() {
        super.clearCache(getAllMethodCache, getMethodCache);
    }
}
