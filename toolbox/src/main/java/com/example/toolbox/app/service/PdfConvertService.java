package com.example.toolbox.app.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * PDF转换服务接口
 */
public interface PdfConvertService {

    /**
     * PDF转图片
     *
     * @param file       PDF文件
     * @param dpi        图片DPI（分辨率）
     * @param imageFormat 图片格式（png/jpeg）
     * @return 图片字节数组列表，每页一个图片
     * @throws IOException IO异常
     */
    List<byte[]> pdfToImages(MultipartFile file, int dpi, String imageFormat) throws IOException;

    /**
     * PDF转Word
     *
     * @param file PDF文件
     * @return Word文档字节数组
     * @throws IOException IO异常
     */
    byte[] pdfToWord(MultipartFile file) throws IOException;

    /**
     * Word转PDF
     *
     * @param file Word文件
     * @return PDF文档字节数组
     * @throws IOException IO异常
     */
    byte[] wordToPdf(MultipartFile file) throws IOException;
}
