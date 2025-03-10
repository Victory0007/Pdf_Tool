package com.USL.PDF_Tool.service;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RAGService extends VectorDBService {
    private final ChatLanguageModel llm;

    @Autowired
    public RAGService(TextExtractorService textExtractorService) {
        super(textExtractorService);
        this.llm = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.1")
                .timeout(java.time.Duration.ofMinutes(50))
                .build();
    }

    public String queryRAG(String queryText) {
        try {
            String collectionName = "knowledge-base";
            String collectionId = getCollectionId(collectionName);
            if (collectionId == null) {
                return "Error: Collection not found.";
            }

            List<String> retrievedDocs = queryCollection(collectionId, queryText);
            if (retrievedDocs.isEmpty()) {
                return "No relevant documents found.";
            }

            String context = String.join("\n", retrievedDocs);
            return generateResponse(queryText, context);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error processing query.";
        }
    }

    private String generateResponse(String queryText, String context) {
        String prompt = "You are an AI assistant. Use the following context to answer the question.\n\n" +
                "Context: " + context + "\n\n" +
                "Question: " + queryText + "\n\nAnswer:";
        return llm.generate(prompt);
    }
}
