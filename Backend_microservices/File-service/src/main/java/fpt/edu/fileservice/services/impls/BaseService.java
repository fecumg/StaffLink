package fpt.edu.fileservice.services.impls;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Truong Duc Duong
 */

@Log4j2
public class BaseService {

    protected void createFolder(String folderDirectory) {
        File folder = new File(folderDirectory);
        if (!folder.exists()) {
            boolean newFolderResult = folder.mkdirs();
            if (newFolderResult) {
                log.info("Create new folder {}" , folderDirectory);
            } else {
                log.error("Failed to create new folder: {}", folderDirectory);
            }
        }
    }

    protected boolean convertImageFormat(String inputPath, String outputPath, String format) {
        try (FileInputStream inputStream = new FileInputStream(inputPath);
             FileOutputStream outputStream = new FileOutputStream(outputPath)) {

//            reads input image from file
            BufferedImage inputImage = ImageIO.read(inputStream);

//            writes to the output image in specified format
            boolean result = ImageIO.write(inputImage, format, outputStream);

//            close the streams
            outputStream.close();
            inputStream.close();

            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private void resizeImage(File file, int targetSize) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file);
        BufferedImage outputImage = Scalr.resize(bufferedImage, targetSize);

        Path path = Paths.get(file.getPath());
        File newImageFile = path.toFile();
        ImageIO.write(outputImage, FilenameUtils.getExtension(newImageFile.getName()), newImageFile);
        outputImage.flush();
    }

    protected void resizeThumbnail(File file) throws IOException {
        resizeImage(file, 500);
    }

    protected void resizeNormal(File file) throws IOException {
        resizeImage(file, 1200);
    }
}
