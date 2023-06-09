package fpt.edu.fileservice.services.impls;

import fpt.edu.fileservice.exchangeDtos.ExchangeAttachment;
import fpt.edu.fileservice.services.AttachmentService;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.io.BufferingOutputStream;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Truong Duc Duong
 */

@Service
@Log4j2
@EnableScheduling
public class AttachmentServiceImpl extends BaseService implements AttachmentService {

    @Value("${folder.uploads}")
    private String UPLOADS;
    @Value("${folder.uploads.attachments}")
    private String ATTACHMENTS;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;
    @Value("${rabbitmq.routing-key.attachment-processing-done}")
    private String ATTACHMENT_PROCESSING_DONE_ROUTING_KEY;

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MessageConverter jsonConverter;
    @Autowired
    private Environment env;

    @RabbitListener(queues = {"${rabbitmq.queue.attachment-processing}"}, messageConverter = "jsonConverter")
    private void processUploadedAttachment(ExchangeAttachment exchangeAttachment) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS + File.separator + ATTACHMENTS;

        String taskId = exchangeAttachment.getTaskId();
        String filename = exchangeAttachment.getFilename();
        log.info("Receive file-retrieving request for {}/{} from queue '{}'", taskId, filename, env.getProperty("rabbitmq.queue.attachment-processing"));

        String folderDirectory = attachmentFolderDirectory + File.separator + taskId;

        Flux<DataBuffer> dataBufferFlux = this.retrieveAttachmentDataBuffer(taskId, filename);

        DataBufferUtils.join(dataBufferFlux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return bytes;
                })
                .subscribe(
                        bytes -> {
//                                create containing folder in case of non-existing
                                this.createFolder(folderDirectory);

                                String filePath = folderDirectory + File.separator + filename;
                                File file = new File(filePath);

//                                write data to file
                                try (BufferingOutputStream stream = new BufferingOutputStream(new FileOutputStream(file))) {
                                    stream.write(bytes);

//                                    send message to Task-service to delete temporary file
                                    rabbitTemplate.setMessageConverter(jsonConverter);
                                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ATTACHMENT_PROCESSING_DONE_ROUTING_KEY, exchangeAttachment);
                                    log.info("Message has been published via routing key '{}'", ATTACHMENT_PROCESSING_DONE_ROUTING_KEY);

                                } catch (IOException e) {
                                    log.error(e.getMessage());
                                }
                        },
                        error -> log.error(error.getMessage()),
                        () -> log.info("Attachment {}/{} has been added to {}", taskId, filename, attachmentFolderDirectory)
                );
    }

    @RabbitListener(queues = {"${rabbitmq.queue.attachment-removal}"}, messageConverter = "jsonConverter")
    private void removeAttachment(ExchangeAttachment exchangeAttachment) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS + File.separator + ATTACHMENTS;
        String taskId = exchangeAttachment.getTaskId();
        String filename = exchangeAttachment.getFilename();

        log.info("Receive file-removal request for {}/{} from queue '{}'", taskId, filename, env.getProperty("rabbitmq.queue.attachment-processing"));

        String folderDirectory = attachmentFolderDirectory + File.separator + taskId;
        String filePath = folderDirectory + File.separator + filename;

        File file = new File(filePath);

        boolean isDeleted = file.delete();
        if (isDeleted) {
            log.info("Attachment file '{}' has been deleted", filePath);
        } else {
            log.info("Attachment file '{}' doesn't exist", filePath);
        }
    }

    private ByteArrayResource retrieveAttachment(ExchangeAttachment exchangeAttachment) throws IOException {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS + File.separator + ATTACHMENTS;

        String taskId = exchangeAttachment.getTaskId();
        String filename = exchangeAttachment.getFilename();
        log.info("Retrieving file {}/{} from Task-service", taskId, filename);

        String folderDirectory = attachmentFolderDirectory + File.separator + taskId;

        Flux<DataBuffer> dataBufferFlux = this.retrieveAttachmentDataBuffer(taskId, filename);

        byte[] dataBytes = DataBufferUtils.join(dataBufferFlux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return bytes;
                })
                .block();

//        create containing folder in case of non-existing
        this.createFolder(folderDirectory);

        String fileDirectory = folderDirectory + File.separator + filename;
        File file = new File(fileDirectory);

//        write data to file
        if (dataBytes == null) {
            throw new NotFoundException("Attachment not found");
        }

        try (BufferingOutputStream stream = new BufferingOutputStream(new FileOutputStream(file))) {
            stream.write(dataBytes);
            log.info("Attachment {}/{} has been added to {}", taskId, filename, attachmentFolderDirectory);

            Path path = Paths.get(fileDirectory);
            return new ByteArrayResource(Files.readAllBytes(path));
        }
    }

    private Flux<DataBuffer> retrieveAttachmentDataBuffer(String taskId, String filename) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("task-service")
                        .path("/attachments/" + taskId + "/" + filename)
                        .build())
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }

    @Override
    public ByteArrayResource downloadAttachment(String taskId, String filename) throws IOException {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS + File.separator + ATTACHMENTS;

        String fileDirectory = attachmentFolderDirectory + File.separator + taskId + File.separator + filename;
        File file = new File(fileDirectory);
        Path path = Paths.get(fileDirectory);

        System.out.println(file.exists());

        if (file.exists()) {
            return new ByteArrayResource(Files.readAllBytes(path));
        } else {
            ExchangeAttachment exchangeAttachment = new ExchangeAttachment(taskId, filename);
            return this.retrieveAttachment((exchangeAttachment));
        }
    }
}
