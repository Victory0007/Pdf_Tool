package com.USL.PDF_Tool.controller;

import com.USL.PDF_Tool.service.RAGService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerTest {

    private RAGService ragService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ragService = mock(RAGService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DocumentController(ragService)).build();
    }

    @Test
    void rejectsEmptyUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf",
                "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/pdfTool/documents").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(ragService);
    }

    @Test
    void rejectsUnsupportedFileExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt",
                "text/plain", "not a PDF".getBytes());

        mockMvc.perform(multipart("/pdfTool/documents").file(file))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ragService);
    }

    @Test
    void ingestsSupportedPdfUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf",
                "application/pdf", "pdf bytes".getBytes());

        mockMvc.perform(multipart("/pdfTool/documents").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document ingested successfully."));

        verify(ragService).processDocument(anyString());
    }

    @Test
    void queriesExistingKnowledgeBase() throws Exception {
        when(ragService.queryRAG("What is this about?")).thenReturn("A test answer.");

        mockMvc.perform(post("/pdfTool/query")
                        .contentType("text/plain")
                        .content("What is this about?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("A test answer."));
    }

    @Test
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/pdfTool/query")
                        .contentType("text/plain")
                        .content("   "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ragService);
    }
}
