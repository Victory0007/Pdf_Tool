package com.USL.PDF_Tool.controller;

import com.USL.PDF_Tool.service.RAGService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/pdfTool")
public class DocumentController {

    private final RAGService ragService;

    public DocumentController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * Ingest a PDF or supported image into the knowledge base.
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> ingestDocument(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "The uploaded file is empty."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().matches(".*\\.(pdf|png|jpe?g)$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only PDF, PNG and JPEG files are supported."));
        }

        String suffix = filename.substring(filename.lastIndexOf('.'));
        Path temporaryFile = Files.createTempFile("pdf-tool-", suffix);
        try {
            file.transferTo(temporaryFile);
            ragService.processDocument(temporaryFile.toString());
            return ResponseEntity.accepted().body(
                    Map.of("message", "Document ingestion was submitted."));
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Query documents that have already been ingested.
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(@RequestBody String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A question is required."));
        }
        return ResponseEntity.ok(Map.of("answer", ragService.queryRAG(prompt)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}
