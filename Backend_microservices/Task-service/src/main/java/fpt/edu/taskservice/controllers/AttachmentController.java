package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.services.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/attachments")
public class AttachmentController extends BaseController {

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping("/{taskId}/{filename}")
    public Flux<DataBuffer> getAttachmentFile(@ModelAttribute ExchangeAttachment exchangeAttachment) {
        return attachmentService.getAttachmentFile(exchangeAttachment);
    }
}
