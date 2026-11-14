package com.USL.PDF_Tool.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Service
public class TextExtractorService {

    private final ImageToTextService imageToTextService;

    public TextExtractorService(ImageToTextService imageToTextService) {
        this.imageToTextService = imageToTextService;
    }

    public String extractText(String filePath) {
        File file = new File(filePath);
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("File does not exist or cannot be read: " + filePath);
        }

        String fileType = getFileType(file);
        return switch (fileType) {
            case "pdf" -> extractTextFromPDF(file);
            case "jpg", "jpeg", "png" -> imageToTextService.imageToText(filePath);
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }

    private String getFileType(File file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex >= 0 ? fileName.substring(extensionIndex + 1) : "";
    }

    private String extractTextFromPDF(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not extract text from PDF: " + file.getName(), exception);
        }
    }
}
