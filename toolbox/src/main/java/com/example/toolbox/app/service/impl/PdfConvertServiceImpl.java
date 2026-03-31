package com.example.toolbox.app.service.impl;

import com.example.toolbox.app.service.PdfConvertService;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF转换服务实现类
 */
@Slf4j
@Service
public class PdfConvertServiceImpl implements PdfConvertService {

    @Override
    public List<byte[]> pdfToImages(MultipartFile file, int dpi, String imageFormat) throws IOException {
        List<byte[]> images = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(file.getInputStream()))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            log.info("PDF总页数: {}, DPI: {}, 格式: {}", pageCount, dpi, imageFormat);
            
            for (int page = 0; page < pageCount; page++) {
                // 渲染PDF页面为图片
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                
                // 将图片写入字节数组
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(bufferedImage, imageFormat, baos);
                    images.add(baos.toByteArray());
                }
                
                log.debug("第 {} 页转换完成", page + 1);
            }
        }
        
        return images;
    }

    @Override
    public byte[] pdfToWord(MultipartFile file) throws IOException {
        try (PDDocument pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(file.getInputStream()))) {
            // 创建Word文档
            try (XWPFDocument wordDocument = new XWPFDocument()) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                int pageCount = pdfDocument.getNumberOfPages();
                
                log.info("PDF总页数: {}", pageCount);
                
                for (int page = 1; page <= pageCount; page++) {
                    // 设置只提取当前页
                    pdfStripper.setStartPage(page);
                    pdfStripper.setEndPage(page);
                    
                    // 提取文本
                    String text = pdfStripper.getText(pdfDocument);
                    
                    // 处理文本，按段落添加到Word
                    String[] paragraphs = text.split("\\r?\\n");
                    for (String paragraph : paragraphs) {
                        if (!paragraph.trim().isEmpty()) {
                            XWPFParagraph wordParagraph = wordDocument.createParagraph();
                            XWPFRun run = wordParagraph.createRun();
                            run.setText(paragraph);
                            run.setFontSize(12);
                            run.setFontFamily("宋体");
                        }
                    }
                    
                    // 添加分页符（除了最后一页）
                    if (page < pageCount) {
                        wordDocument.createParagraph().createRun().addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
                    }
                    
                    log.debug("第 {} 页转换完成", page);
                }
                
                // 将Word文档写入字节数组
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    wordDocument.write(baos);
                    return baos.toByteArray();
                }
            }
        }
    }

    @Override
    public byte[] wordToPdf(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument wordDocument = new XWPFDocument(is);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            log.info("开始转换Word为PDF: {}", file.getOriginalFilename());
            
            // 使用 XDocReport 将 Word 转换为 PDF
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(wordDocument, baos, options);
            
            log.info("Word转PDF完成");
            return baos.toByteArray();
        }
    }
}
