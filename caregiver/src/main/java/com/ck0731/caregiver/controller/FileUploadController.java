package com.ck0731.caregiver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传Controller
 * 支持图片上传到服务器本地存储
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    // 上传文件保存的根目录
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    // 允许的图片格式
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
        Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")
    );

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            String error = validateFile(file);
            if (error != null) {
                return ResponseEntity.badRequest().body(Map.of("error", error));
            }

            // 保存文件
            String url = saveFile(file);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", url,
                "filename", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 上传多个图片
     */
    @PostMapping("/images")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, String>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 验证文件
                String error = validateFile(file);
                if (error != null) {
                    errors.add(file.getOriginalFilename() + ": " + error);
                    continue;
                }

                // 保存文件
                String url = saveFile(file);
                results.add(Map.of(
                    "url", url,
                    "filename", file.getOriginalFilename()
                ));
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", errors.isEmpty());
        response.put("files", results);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 验证文件
     */
    private String validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "文件不能为空";
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return "文件大小不能超过5MB";
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "文件名无效";
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "只支持图片格式: " + String.join(", ", ALLOWED_EXTENSIONS);
        }

        return null;
    }

    /**
     * 保存文件到服务器
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录（按日期分类）
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, dateFolder);
        Files.createDirectories(uploadDir);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        // 返回访问URL
        return "/" + uploadPath + "/" + dateFolder + "/" + filename;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
