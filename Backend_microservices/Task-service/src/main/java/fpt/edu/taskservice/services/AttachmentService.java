package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */

@Service
public interface AttachmentService {
    Flux<DataBuffer> getAttachmentFile(ExchangeAttachment exchangeAttachment);
}
