package fpt.edu.fileservice.controllers;

import fpt.edu.fileservice.services.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/files")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping(value = "/{taskId}/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<ByteArrayResource> getImage(@PathVariable("taskId") String taskId, @PathVariable("filename") String filename) throws IOException {

        ByteArrayResource resource = attachmentService.downloadAttachment(taskId, filename);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
