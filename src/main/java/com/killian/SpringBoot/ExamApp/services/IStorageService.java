package com.killian.SpringBoot.ExamApp.services;

import java.util.stream.Stream;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    public String storeFile(MultipartFile file);

    public void deleteFile(String fileName) throws IOException;

    public Stream<Path> loadAll();

    public byte[] readFileContent(String fileName);

    public void deleteAllFiles();
}
