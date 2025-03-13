package com.USL.PDF_Tool.controller;

import com.USL.PDF_Tool.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdfTool")
public class DocumentController {

    private final RAGService ragService;

    @Autowired
    public DocumentController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/RAG_Service")
    public String getResponse(@RequestBody String prompt) {
        // Process the document (if required)
        ragService.processDocument("C:\\Users\\Dev Mode\\Downloads\\kachasi-brochure.pdf");

        // Query RAG service
        String response = ragService.queryRAG(prompt);
        //System.out.println(response);
        return response;
    }
}
