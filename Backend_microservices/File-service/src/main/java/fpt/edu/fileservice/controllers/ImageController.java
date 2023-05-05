package fpt.edu.fileservice.controllers;

import fpt.edu.fileservice.services.ImageService;
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
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping(value = "/images/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<ByteArrayResource> getImage(@PathVariable("filename") String filename) throws IOException {

        ByteArrayResource resource = imageService.getImage(filename);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping(value = "/thumbnails/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<ByteArrayResource> getThumbnail(@PathVariable("filename") String filename) throws IOException {

        ByteArrayResource resource = imageService.getThumbnail(filename);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
