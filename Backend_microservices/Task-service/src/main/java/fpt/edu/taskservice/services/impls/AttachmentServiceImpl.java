package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.dtos.requestDtos.AttachmentRequest;
import fpt.edu.taskservice.dtos.responseDtos.AttachmentResponse;
import fpt.edu.taskservice.entities.Attachment;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.AttachmentRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.AttachmentService;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class AttachmentServiceImpl extends BaseService<Attachment> implements AttachmentService {

    @Value("${folder.uploads}")
    private String UPLOADS_TMP;
    @Value("${folder.uploads.attachments}")
    private String ATTACHMENT_FOLDER;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Value("${rabbitmq.routing-key.attachment-processing}")
    private String ATTACHMENT_PROCESSING_ROUTING_KEY;

    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Flux<DataBuffer> getAttachmentFile(ExchangeAttachment exchangeAttachment) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS_TMP + File.separator + ATTACHMENT_FOLDER;

        String taskId = exchangeAttachment.getTaskId();
        String filename = exchangeAttachment.getFilename();
        String filePath = attachmentFolderDirectory + File.separator + taskId + File.separator + filename;
        File file = new File(filePath);
        Path path = Paths.get(file.getPath());

        DataBufferFactory dbf = new DefaultDataBufferFactory();
        return DataBufferUtils.read(path, dbf, 10000)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<AttachmentResponse> save(AttachmentRequest attachmentRequest, ServerWebExchange exchange) {
        Mono<Task> taskMono = taskRepository.findById(attachmentRequest.getTaskId())
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")));

        String requestFilename = attachmentRequest.getAttachment().filename();
        Mono<String> filenameMono = taskMono
                .flatMapMany(task -> attachmentRepository.findAllByTask(task))
                .collectList()
                .map(attachments -> this.processFilename(requestFilename, attachments));

        return taskMono
                .zipWith(filenameMono, (task, filename) ->
                        this.processAttachmentFile(attachmentRequest.getAttachment(), filename, task)
                                .thenReturn(new Attachment(filename, task))
                )
                .flatMap(mono -> mono)
                .map(attachment -> {
                    super.setCreatedBy(attachment, exchange);
                    return attachment;
                })
                .flatMap(attachment -> attachmentRepository.save(attachment))
                .map(AttachmentResponse::new)
                .doOnSuccess(attachmentResponse -> log.info("Attachment {} has been added successfully", attachmentResponse.getName()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<AttachmentResponse> getAllByTaskId(String taskId, Pagination pagination) {
        Flux<Attachment> attachmentFlux =  taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMapMany(task -> attachmentRepository.findAllByTask(task));

        return this.buildAttachmentResponseFlux(attachmentFlux, pagination);
    }

    @Override
    public Mono<AttachmentResponse> get(String id) {
        return attachmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Attachment not found")))
                .map(AttachmentResponse::new)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Void> delete(String id) {
        return attachmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Attachment not found")))
                .flatMap(super::deleteAttachmentFile)
                .flatMap(attachment -> attachmentRepository.delete(attachment))
                .doOnSuccess(voidValue -> log.info("Attachment with id {} has been deleted successfully", id))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Mono<FilePart> processAttachmentFile(FilePart filePart, String filename, Task task) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS_TMP + File.separator + ATTACHMENT_FOLDER;

//        create folder in case of non-existing
        String folderDirectory = attachmentFolderDirectory + File.separator + task.getId();
        Path folderPath = Paths.get(folderDirectory);
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            boolean newFolderResult = folder.mkdirs();
            if (newFolderResult) {
                log.info("Create new folder {}" , folderDirectory);
            } else {
                log.error("Failed to create new folder: {}", folderDirectory);
            }
        }

        return filePart.transferTo(folderPath.resolve(filename))
                .thenReturn(filePart)
                .doOnSuccess(fp -> log.info("File {} has been stored in folder {}", filename, folderDirectory))
                .map(fp ->  {
                    ExchangeAttachment exchangeAttachment = new ExchangeAttachment(task.getId(), filename);

//                    send message to ask File-service to retrieve uploaded attachments
                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ATTACHMENT_PROCESSING_ROUTING_KEY, exchangeAttachment);
                    log.info("Message has been published via routing key '{}'", ATTACHMENT_PROCESSING_ROUTING_KEY);

                    return filePart;
                });
    }

    private Flux<AttachmentResponse> buildAttachmentResponseFlux(Flux<Attachment> attachmentFlux, Pagination pagination) {
        return super.paginate(attachmentFlux, pagination)
                .map(AttachmentResponse::new)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @RabbitListener(queues = {"${rabbitmq.queue.attachment-processing-done}"})
    private void listenToAttachmentProcessingDone(ExchangeAttachment exchangeAttachment) {
        super.deleteTemporaryAttachmentFile(exchangeAttachment);
    }

    private String processFilename(String filename, List<Attachment> attachments) {
        for (Attachment attachment: attachments) {
            if (filename.equals(attachment.getName())) {
                return processFilename(generateParallelFilename(filename), attachments);
            }
        }
        return filename;
    }

    private String generateParallelFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf("."));
        String filenameWithoutExtension = filename.substring(0, filename.lastIndexOf("."));
        int lastIndexOfLodash = filenameWithoutExtension.lastIndexOf("_");
        String defaultResult = filenameWithoutExtension + "_" + 1 + extension;
        if (lastIndexOfLodash < 0) {
            return defaultResult;
        } else {
            if (lastIndexOfLodash == filename.length() - 1) {
                return filenameWithoutExtension + 1 + extension;
            }
            String suffix = filenameWithoutExtension.substring(lastIndexOfLodash + 1);
            if (StringUtils.isEmpty(suffix)) {
                return defaultResult;
            }
            try {
                int suffixInt = Integer.parseInt(suffix);
                return filenameWithoutExtension.substring(0, lastIndexOfLodash + 1) + (++suffixInt) + extension;
            } catch (NumberFormatException e) {
                return defaultResult;
            }
        }
    }
}
