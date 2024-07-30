package com.killian.SpringBoot.ExamApp.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("null")
@Service
public class ImageStorageService implements IStorageService {

    private final Path storageFolder = Paths.get("uploads");

    public ImageStorageService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException exception) {
            throw new RuntimeException("Cannot initialize storage", exception);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] { "png", "jpg", "jpeg", "bmp" }).contains(fileExtension.trim().toLowerCase());
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // empty file not allowed
            // System.out.println("Calling Image Storage Service");
            if (file.isEmpty())
                throw new RuntimeException("Failed to store empty file!");
            // image file only
            if (!isImageFile(file))
                throw new RuntimeException("You can only upload image!");
            // size must be <= 25mb
            float fileSizeInMegabytes = file.getSize() / 1000000;
            if (fileSizeInMegabytes > 25.0f)
                throw new RuntimeException("File size too big!");

            // file must be renamed
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename()); // get file extension
            String generatedFilename = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension; // random a
                                                                                                            // file name
            Path destinationFilePath = this.storageFolder.resolve(Paths.get(generatedFilename)).normalize()
                    .toAbsolutePath(); // get path to storage folder
            if (!destinationFilePath.getParent().equals(this.storageFolder.toAbsolutePath()))
                throw new RuntimeException("Cannot store file outside current directory");
            try (InputStream inputStream = file.getInputStream()) { // copy to destination path
                Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return generatedFilename;
        } catch (IOException exception) {
            throw new RuntimeException("Failed to store file", exception);
        }
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = storageFolder.resolve(fileName);
        Files.deleteIfExists(filePath);
    }

    @Override
    public byte[] readFileContent(String fileName) {
        try {
            Path file = storageFolder.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
                return bytes;
            } else {
                throw new RuntimeException("Could not read file: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + fileName);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            // recursively list all files in the directory and its subdirectories
            Stream<Path> pathStream = Files.walk(this.storageFolder)
                    .filter(Files::isRegularFile); // Filter to include only regular files
                                                   // optional: no subdirectiories files
            return pathStream;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load stored files");
        }
    }

    @Override
    public void deleteAllFiles() {

    }
}
