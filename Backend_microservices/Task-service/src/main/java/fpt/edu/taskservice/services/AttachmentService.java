package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.dtos.requestDtos.AttachmentRequest;
import fpt.edu.taskservice.dtos.responseDtos.AttachmentResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Service
public interface AttachmentService {
    Flux<DataBuffer> getAttachmentFile(ExchangeAttachment exchangeAttachment);
    Mono<AttachmentResponse> save(AttachmentRequest attachmentRequest, ServerWebExchange exchange);
    Mono<AttachmentResponse> get(String id);
    Mono<Void> delete(String id);
}
