package com.inkfront.logisticsApplication.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtils {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "pdf", "doc", "docx"};

    public static String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public static boolean isValidFileType(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }

    public static void saveFile(MultipartFile file, String directory, String fileName) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Path filePath = path.resolve(fileName);
        file.transferTo(filePath.toFile());
    }

    public static void deleteFile(String directory, String fileName) throws IOException {
        Path filePath = Paths.get(directory, fileName);
        Files.deleteIfExists(filePath);
    }

    public static boolean fileExists(String directory, String fileName) {
        Path filePath = Paths.get(directory, fileName);
        return Files.exists(filePath);
    }

    public static byte[] readFile(String directory, String fileName) throws IOException {
        Path filePath = Paths.get(directory, fileName);
        return Files.readAllBytes(filePath);
    }

    public static String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}