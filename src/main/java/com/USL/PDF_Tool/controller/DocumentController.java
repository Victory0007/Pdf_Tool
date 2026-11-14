package com.USL.PDF_Tool.controller;

import com.USL.PDF_Tool.service.RAGService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/pdfTool")
public class DocumentController {

    private final RAGService ragService;

    public DocumentController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> ingestDocument(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "The uploaded file is empty."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).matches(".*\\.(pdf|png|jpe?g)$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only PDF, PNG and JPEG files are supported."));
        }

        Path temporaryFile = Files.createTempFile("pdf-tool-", extensionOf(filename));
        try {
            file.transferTo(temporaryFile);
            ragService.processDocument(temporaryFile.toString());
            return ResponseEntity.ok(Map.of("message", "Document ingested successfully."));
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    @PostMapping(value = "/query", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Map<String, String>> query(@RequestBody String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A question is required."));
        }
        return ResponseEntity.ok(Map.of("answer", ragService.queryRAG(prompt)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    private String extensionOf(String filename) {
        return filename.substring(filename.lastIndexOf('.'));
    }
}
