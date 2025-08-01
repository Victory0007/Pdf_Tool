package com.USL.PDF_Tool.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class TextExtractorService {

    private final ImageToTextService imageToTextService;

    @Autowired
    public TextExtractorService(ImageToTextService imageToTextService) {
        this.imageToTextService = imageToTextService;
    }

    public String extractText(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "Error: File not found - " + filePath;
        }

        String fileType = getFileType(file);

        switch (fileType) {
            case "pdf":
                return extractTextFromPDF(file);
            case "jpg":
            case "jpeg":
            case "png":
                return imageToTextService.imageToText(filePath);
            default:
                return "Error: Unsupported file type - " + fileType;
        }
    }

    private String getFileType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) return "pdf";
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image";
        return "unknown";
    }

    private String extractTextFromPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            return "Error reading PDF: " + e.getMessage();
        }
    }
}
