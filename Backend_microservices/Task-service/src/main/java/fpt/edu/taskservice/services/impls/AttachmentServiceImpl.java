package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.services.AttachmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Truong Duc Duong
 */

@Service
@Log4j2
public class AttachmentServiceImpl extends BaseService implements AttachmentService {

    @Value("${folder.uploads}")
    private String UPLOADS_TMP;
    @Value("${folder.uploads.attachments}")
    private String ATTACHMENT_FOLDER;

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

    @RabbitListener(queues = {"${rabbitmq.queue.attachment-processing-done}"})
    private void listenToAttachmentProcessingDone(ExchangeAttachment exchangeAttachment) {
        super.deleteTemporaryAttachmentFile(exchangeAttachment);
    }
}
