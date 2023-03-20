package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.FunctionRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.RoleFunctionMapping;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.FunctionRepository;
import fpt.edu.user_service.repositories.RoleFunctionMappingRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.services.FunctionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    private RoleFunctionMappingRepository roleFunctionMappingRepository;

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
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#result.getId()")
    public FunctionResponse save(FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException {

//        check unique uri
        if (functionRepository.existsByUri(functionRequest.getUri())) {
            throw new UniqueKeyViolationException("Uri already exists");
        }

        Function function = modelMapper.map(functionRequest, Function.class);

//        set createdBy
        setCreatedBy(function, request);

//        set parent
        setParent(functionRequest, function);

        Function newFunction = functionRepository.save(function);

//        send message to demand auth-gateway to update guardedPaths redis cache
        rabbitTemplate.convertAndSend(EXCHANGE, PATH_UPDATE_ROUTING_KEY, newFunction.getUri());

        return modelMapper.map(newFunction, FunctionResponse.class);
    }

    @Override
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#id")
    public FunctionResponse update(int id, FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException {
        Optional<Function> optionalFunction = functionRepository.findById(id);
        if (optionalFunction.isPresent()) {
            Function currentFunction = optionalFunction.get();

//            check unique uri
            if (functionRepository.existsByUriAndUriIsNot(functionRequest.getUri(), currentFunction.getUri())) {
                throw new UniqueKeyViolationException("Uri already exists");
            }

            Function function = modelMapper.map(functionRequest, Function.class);

//            set updatedBy
            setUpdatedBy(function, request);

//            set parent
            setParent(functionRequest, function);

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

    private void setParent(FunctionRequest functionRequest, Function function) {
        Function parent = functionRepository.findById(functionRequest.getParentId())
                .orElseThrow(() -> new NotFoundException("Parent function not found"));
        function.setParent(parent);
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
    @Cacheable(value = getAllMethodCache, key = "#pagination.getPageNumber()")
    public List<FunctionResponse> getAll(Pagination pagination) {
        List<Function> functions;
        if (pagination == null) {
            functions =  functionRepository.findAll();
        } else {
            PageRequest pageRequest = Pagination.getPageRequest(pagination);
            functions =  functionRepository.findAll(pageRequest).getContent();
        }
        return modelMapper.map(functions, new TypeToken<List<FunctionResponse>>() {}.getType());

    }

    @Override
    @Cacheable(value = getMethodCache, key = "#id")
    public FunctionResponse get(int id) {
        Function function = functionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found"));
        return modelMapper.map(function, FunctionResponse.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = getAllMethodCache),
            @CacheEvict(value = getMethodCache, key = "#id")
    }
    )
    public void delete(int id) {
        Optional<Function> optionalFunction = functionRepository.findById(id);
        if (optionalFunction.isPresent()) {
            Function function = optionalFunction.get();
            List<ExchangeUser> exchangeUsers = this.getAffectedExchangeUser(function);

            functionRepository.delete(function);

//            delete all existing role-function-mappings
            List<RoleFunctionMapping> roleFunctionMappings = function.getRoleFunctionMappings();
            if (!roleFunctionMappings.isEmpty()) {
                roleFunctionMappingRepository.deleteAll(roleFunctionMappings);
            }

//            send message to demand auth-gateway to update guardedPaths redis cache
            this.sendMessageToDeletePathCache(function.getUri());

//            send message to demand auth-gateway to update authenticatedUser redis cache
            this.sendMessageToUpdateAuthCache(exchangeUsers);
        } else {
            throw new NotFoundException("Function not found");
        }
    }

    @CacheEvict(cacheNames = { getAllMethodCache, getMethodCache })
    @Scheduled(fixedDelay = 6000)
    public void cacheEvict() {
    }
}
