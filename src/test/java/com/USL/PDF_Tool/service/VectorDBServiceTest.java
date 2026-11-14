package com.USL.PDF_Tool.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class VectorDBServiceTest {

    private final VectorDBService service = new VectorDBService(
            mock(TextExtractorService.class),
            "http://localhost:8000",
            "http://localhost:11434",
            "nomic-embed-text");

    @Test
    void splitsTextIntoOverlappingChunks() {
        String text = "abcdefghij";

        List<String> chunks = service.splitText(text, 6, 2);

        assertEquals(List.of("abcdef", "efghij"), chunks);
    }

    @Test
    void returnsNoChunksForEmptyText() {
        assertTrue(service.splitText("", 10, 2).isEmpty());
    }

    @Test
    void rejectsInvalidChunkConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> service.splitText("text", 5, 5));
    }

    @Test
    void rejectsNegativeOverlap() {
        assertThrows(IllegalArgumentException.class,
                () -> service.splitText("text", 5, -1));
    }
}
