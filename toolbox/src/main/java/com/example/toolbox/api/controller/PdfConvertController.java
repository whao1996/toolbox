package com.example.toolbox.api.controller;

import com.example.toolbox.api.vo.ApiResponse;
import com.example.toolbox.api.vo.PdfToImageResponse;
import com.example.toolbox.app.service.PdfConvertService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * PDF转换控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfConvertController {

    private final PdfConvertService pdfConvertService;

    /**
     * PDF转图片 - 返回Base64格式
     *
     * @param file        PDF文件
     * @param dpi         图片DPI（默认150）
     * @param imageFormat 图片格式：png/jpeg（默认png）
     * @return 图片Base64列表
     */
    @PostMapping("/to-images")
    public ApiResponse<PdfToImageResponse> convertToImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dpi", defaultValue = "150") int dpi,
            @RequestParam(value = "format", defaultValue = "png") String imageFormat) {

        // 参数校验
        if (file.isEmpty()) {
            return ApiResponse.error("PDF文件不能为空");
        }
        
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ApiResponse.error("请上传PDF文件");
        }

        // 校验图片格式
        if (!"png".equalsIgnoreCase(imageFormat) && !"jpeg".equalsIgnoreCase(imageFormat) && !"jpg".equalsIgnoreCase(imageFormat)) {
            imageFormat = "png";
        }

        try {
            log.info("开始转换PDF为图片: {}, DPI: {}, 格式: {}", originalFilename, dpi, imageFormat);
            
            List<byte[]> imageBytesList = pdfConvertService.pdfToImages(file, dpi, imageFormat);
            
            // 转换为Base64
            List<String> base64Images = new ArrayList<>();
            for (byte[] imageBytes : imageBytesList) {
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                base64Images.add("data:image/" + imageFormat.toLowerCase() + ";base64," + base64);
            }

            PdfToImageResponse response = new PdfToImageResponse();
            response.setTotalPages(imageBytesList.size());
            response.setImages(base64Images);
            response.setFormat(imageFormat.toLowerCase());

            log.info("PDF转图片完成: {}页", imageBytesList.size());
            return ApiResponse.success(response);
            
        } catch (IOException e) {
            log.error("PDF转图片失败", e);
            return ApiResponse.error("PDF转图片失败: " + e.getMessage());
        }
    }

    /**
     * PDF转图片 - 下载压缩包
     *
     * @param file        PDF文件
     * @param dpi         图片DPI（默认150）
     * @param imageFormat 图片格式：png/jpeg（默认png）
     * @param response    HTTP响应
     */
    @PostMapping("/to-images/download")
    public void convertToImagesDownload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dpi", defaultValue = "150") int dpi,
            @RequestParam(value = "format", defaultValue = "png") String imageFormat,
            HttpServletResponse response) {

        // 参数校验
        if (file.isEmpty()) {
            writeErrorResponse(response, "PDF文件不能为空");
            return;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            writeErrorResponse(response, "请上传PDF文件");
            return;
        }

        if (!"png".equalsIgnoreCase(imageFormat) && !"jpeg".equalsIgnoreCase(imageFormat) && !"jpg".equalsIgnoreCase(imageFormat)) {
            imageFormat = "png";
        }

        try {
            log.info("开始转换PDF为图片(下载模式): {}, DPI: {}, 格式: {}", originalFilename, dpi, imageFormat);
            
            List<byte[]> imageBytesList = pdfConvertService.pdfToImages(file, dpi, imageFormat);
            
            // 创建ZIP文件
            String zipFileName = originalFilename.toLowerCase().replace(".pdf", "_images.zip");
            response.setContentType("application/zip");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(zipFileName, StandardCharsets.UTF_8) + "\"");

            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                for (int i = 0; i < imageBytesList.size(); i++) {
                    String entryName = String.format("page_%03d.%s", i + 1, imageFormat.toLowerCase());
                    java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                    zos.write(imageBytesList.get(i));
                    zos.closeEntry();
                }
            }
            
            log.info("PDF转图片下载完成: {}页", imageBytesList.size());
            
        } catch (IOException e) {
            log.error("PDF转图片失败", e);
            writeErrorResponse(response, "PDF转图片失败: " + e.getMessage());
        }
    }

    /**
     * PDF转Word - 下载Word文件
     *
     * @param file     PDF文件
     * @param response HTTP响应
     */
    @PostMapping("/to-word")
    public void convertToWord(
            @RequestParam("file") MultipartFile file,
            HttpServletResponse response) {

        // 参数校验
        if (file.isEmpty()) {
            writeErrorResponse(response, "PDF文件不能为空");
            return;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            writeErrorResponse(response, "请上传PDF文件");
            return;
        }

        try {
            log.info("开始转换PDF为Word: {}", originalFilename);
            
            byte[] wordBytes = pdfConvertService.pdfToWord(file);
            
            String wordFileName = originalFilename.replace(".pdf", ".docx");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(wordFileName, StandardCharsets.UTF_8) + "\"");
            response.setContentLength(wordBytes.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(wordBytes);
                os.flush();
            }
            
            log.info("PDF转Word完成");
            
        } catch (IOException e) {
            log.error("PDF转Word失败", e);
            writeErrorResponse(response, "PDF转Word失败: " + e.getMessage());
        }
    }

    /**
     * Word转PDF - 下载PDF文件
     *
     * @param file     Word文件
     * @param response HTTP响应
     */
    @PostMapping("/word-to-pdf")
    public void convertWordToPdf(
            @RequestParam("file") MultipartFile file,
            HttpServletResponse response) {

        // 参数校验
        if (file.isEmpty()) {
            writeErrorResponse(response, "Word文件不能为空");
            return;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !(originalFilename.toLowerCase().endsWith(".docx") || originalFilename.toLowerCase().endsWith(".doc"))) {
            writeErrorResponse(response, "请上传Word文件(.docx或.doc)");
            return;
        }

        try {
            log.info("开始转换Word为PDF: {}", originalFilename);
            
            byte[] pdfBytes = pdfConvertService.wordToPdf(file);
            
            String pdfFileName = originalFilename.replaceAll("(?i)\\.(docx|doc)$", ".pdf");
            response.setContentType("application/pdf");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(pdfFileName, StandardCharsets.UTF_8) + "\"");
            response.setContentLength(pdfBytes.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(pdfBytes);
                os.flush();
            }
            
            log.info("Word转PDF完成");
            
        } catch (IOException e) {
            log.error("Word转PDF失败", e);
            writeErrorResponse(response, "Word转PDF失败: " + e.getMessage());
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            String json = String.format("{\"code\":500,\"message\":\"%s\",\"data\":null}", message);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("写入错误响应失败", e);
        }
    }
}
