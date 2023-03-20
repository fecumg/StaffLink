package fpt.edu.fileservice.services.impls;

import fpt.edu.fileservice.services.ImageService;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.io.BufferingOutputStream;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Truong Duc Duong
 */

@Service
@EnableScheduling
@Log4j2
public class ImageServiceImpl extends BaseService implements ImageService {

    private final String IMAGE_FOLDER = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "images";

    @Autowired
    private Environment env;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public ByteArrayResource getImage(String filename) throws IOException {
        String imagePath = IMAGE_FOLDER + File.separator + filename;
        File file = new File(imagePath);
        if (file.exists()) {
            return new ByteArrayResource(Files.readAllBytes(Paths.get(imagePath)));
        } else {
            String extension = filename.substring(filename.lastIndexOf(".") + 1);
            if (!extension.equalsIgnoreCase("jpg")) {
                throw new NoSuchFileException("No such file: " + filename);
            }

//            In case .jpg conversion was not successful, return original format (.png or .jpeg) image if exists
            String nameWithoutExtension = filename.substring(0, filename.lastIndexOf("."));
            String pngImagePath = IMAGE_FOLDER + File.separator + nameWithoutExtension + ".png";
            File pngFile = new File(pngImagePath);
            if (pngFile.exists()) {
                log.warn("Image {} has not been converted to .jpg and is being used to respond a request", pngImagePath);
                return new ByteArrayResource(Files.readAllBytes(Paths.get(pngImagePath)));
            }

            String jpegImagePath = IMAGE_FOLDER + File.separator + nameWithoutExtension + ".jpeg";
            File jpegFile = new File(jpegImagePath);
            if (jpegFile.exists()) {
                log.warn("Image {} has not been converted to .jpg and is being used to respond a request", pngImagePath);
                return new ByteArrayResource(Files.readAllBytes(Paths.get(jpegImagePath)));
            }

            throw new NoSuchFileException("No such file: " + filename);
        }
    }

    @RabbitListener(queues = {"${rabbitmq.queue.avatar-processing}"}, messageConverter = "simpleConverter")
    private void processUploadedAvatar(
            byte[] bytes,
            @Header(value = "Filename") String filename) {

        log.info("receive image '{}' from queue '{}'", filename, env.getProperty("rabbitmq.queue.avatar-processing"));

        String name = filename.substring(0, filename.lastIndexOf("."));
        String extension = filename.substring(filename.lastIndexOf(".") + 1);

//        validate image format
        if (!extension.equalsIgnoreCase("jpg") &&
                !extension.equalsIgnoreCase("jpeg") &&
                !extension.equalsIgnoreCase("png")) {
            log.error("Invalid image format detected: '.{}'. Uploaded images must be in '.jpg', '.jpeg' or '.png' format", extension);
            return;
        }

//        create containing folder in case of non-existing
        this.createFolder(IMAGE_FOLDER);

        String filePath = IMAGE_FOLDER + File.separator + filename;
        File file = new File(filePath);

//        write file
        try (BufferingOutputStream stream = new BufferingOutputStream(new FileOutputStream(file))) {
            stream.write(bytes);
            log.info("New image has been added to {}", filePath);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

//        convert image to .jpg
        if (!extension.equalsIgnoreCase("jpg")) {
            String convertedFilePath = IMAGE_FOLDER + File.separator + name + ".jpg";
            boolean conversionResult = this.convertImageFormat(filePath, convertedFilePath, "JPG");
            if (conversionResult) {
//                delete old file
                file.deleteOnExit();
                log.info("Image {} has been converted to {}", filePath, convertedFilePath);
            } else {
                log.error("Failed to convert image {}", filePath);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 2000)
    private void scanStoredImages() {
        File imageFolder = new File(IMAGE_FOLDER);
        List<File> imageFiles = Arrays.stream(Objects.requireNonNull(imageFolder.listFiles(), "Folder: " + IMAGE_FOLDER + " does not exist")).toList();

//        sync stored images and image files with data from User-service
        webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("user-service")
                        .path("/avatarNames")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .subscribe(
                        imageNames -> {
                            for (File imageFile: imageFiles) {
                                if (!imageNames.contains(imageFile.getName())) {
                                    imageFile.deleteOnExit();
                                    log.warn("image file {} does not sync with data from User-service has been deleted", imageFile.getAbsolutePath());
                                }
                            }
                        },
                        error -> log.error(error.getMessage()),
                        () -> log.info("Stored image scanning completed")
                );
    }
}
