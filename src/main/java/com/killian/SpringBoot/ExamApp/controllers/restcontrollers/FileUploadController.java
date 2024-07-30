package com.killian.SpringBoot.ExamApp.controllers.restcontrollers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.killian.SpringBoot.ExamApp.models.ResponseObject;
import com.killian.SpringBoot.ExamApp.services.ImageStorageService;
import com.killian.SpringBoot.ExamApp.services.SessionManagementService;

@Controller
@SuppressWarnings("null")
@RequestMapping(path = "api/v1/FileUpload")
public class FileUploadController {

    // inject Storage Service
    @Autowired
    private ImageStorageService storageService;

    @Autowired
    private SessionManagementService sessionManagementService;

    @GetMapping("/reloadPageForImageAppear")
    public ResponseEntity<ResponseObject> reloadPageForImageAppear(
            @RequestParam(value = "hash", required = false) String hashValue) {
        if (hashValue != null) {
            sessionManagementService.clearMessage();
            // System.out.println("Page reloaded");
        } else {
            // System.out.println("Page not reloaded yet");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject("Ok", "Page reloaded", null));
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String generatedFileName = storageService.storeFile(file);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Ok", "File uploaded", generatedFileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(new ResponseObject("Ok", e.getMessage(), null));
        }
    }

    // get image
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<byte[]> readFileDetail(@PathVariable String fileName) {
        try {
            byte[] bytes = storageService.readFileContent(fileName);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_JPEG).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/listAll")
    public ResponseEntity<ResponseObject> getUploadedFiles() {
        try {
            List<String> urls = storageService.loadAll().map(path -> {
                String urlPath = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "readFileDetail", path.getFileName().toString()).build().toUri().toString();
                return urlPath;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(new ResponseObject("Ok", "List files successful", urls));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("Failed", "List files failed", new String[] {}));
        }
    }
}
