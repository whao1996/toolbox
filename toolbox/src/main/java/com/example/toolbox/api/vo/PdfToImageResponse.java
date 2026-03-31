package com.example.toolbox.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * PDF转图片响应
 */
@Data
public class PdfToImageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 图片Base64列表
     */
    private List<String> images;

    /**
     * 图片格式
     */
    private String format;
}
