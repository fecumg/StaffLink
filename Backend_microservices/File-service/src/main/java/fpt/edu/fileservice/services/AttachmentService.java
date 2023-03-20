package fpt.edu.fileservice.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Truong Duc Duong
 */

@Service
public interface AttachmentService {
    ByteArrayResource downloadAttachment(String taskId, String filename) throws IOException;
}
