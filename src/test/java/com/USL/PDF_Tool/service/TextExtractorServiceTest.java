package com.USL.PDF_Tool.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextExtractorServiceTest {

    private final ImageToTextService imageToTextService = mock(ImageToTextService.class);
    private final TextExtractorService service = new TextExtractorService(imageToTextService);

    @Test
    void routesPngFilesToOcr() throws Exception {
        Path image = Files.createTempFile("pdf-tool-test-", ".png");
        try {
            when(imageToTextService.imageToText(image.toString())).thenReturn("recognized text");

            assertEquals("recognized text", service.extractText(image.toString()));
            verify(imageToTextService).imageToText(image.toString());
        } finally {
            Files.deleteIfExists(image);
        }
    }

    @Test
    void routesJpegFilesToOcr() throws Exception {
        Path image = Files.createTempFile("pdf-tool-test-", ".jpeg");
        try {
            when(imageToTextService.imageToText(image.toString())).thenReturn("recognized text");

            assertEquals("recognized text", service.extractText(image.toString()));
            verify(imageToTextService).imageToText(image.toString());
        } finally {
            Files.deleteIfExists(image);
        }
    }

    @Test
    void rejectsMissingFiles() {
        assertThrows(IllegalArgumentException.class,
                () -> service.extractText("does-not-exist.pdf"));
    }

    @Test
    void rejectsUnsupportedExtensions() throws Exception {
        Path file = Files.createTempFile("pdf-tool-test-", ".txt");
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> service.extractText(file.toString()));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
