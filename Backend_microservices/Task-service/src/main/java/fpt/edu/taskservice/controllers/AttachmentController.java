package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.dtos.requestDtos.AttachmentRequest;
import fpt.edu.taskservice.dtos.responseDtos.AttachmentResponse;
import fpt.edu.taskservice.services.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/attachments")
public class AttachmentController extends BaseController {

    @Autowired
    private AttachmentService attachmentService;

    @CrossOrigin(origins = "http://localhost:9093")
    @GetMapping("/{taskId}/{filename}")
    public Flux<DataBuffer> getAttachmentFile(@ModelAttribute ExchangeAttachment exchangeAttachment) {
        return attachmentService.getAttachmentFile(exchangeAttachment);
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<AttachmentResponse>> newAttachment(@ModelAttribute AttachmentRequest attachmentRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(attachmentService.save(attachmentRequest, exchange));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<AttachmentResponse>> getAttachment(@PathVariable("id") String id) {
        return ResponseEntity.ok(attachmentService.get(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Mono<Void>> deleteAttachment(@PathVariable("id") String id) {
        return ResponseEntity.ok(attachmentService.delete(id));
    }
}
