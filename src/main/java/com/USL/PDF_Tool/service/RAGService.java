package com.USL.PDF_Tool.service;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class RAGService extends VectorDBService {

    private final ChatLanguageModel llm;

    public RAGService(
            TextExtractorService textExtractorService,
            @Value("${pdf-tool.chroma.base-url:http://localhost:8000}") String chromaUrl,
            @Value("${pdf-tool.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${pdf-tool.ollama.embedding-model:nomic-embed-text}") String embeddingModel,
            @Value("${pdf-tool.ollama.chat-model:llama3.1}") String chatModel,
            @Value("${pdf-tool.ollama.timeout:PT5M}") Duration timeout) {
        super(textExtractorService, chromaUrl, ollamaBaseUrl, embeddingModel);
        this.llm = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModel)
                .timeout(timeout)
                .build();
    }

    public String queryRAG(String queryText) {
        try {
            String collectionId = getCollectionId("knowledge-base");
            if (collectionId == null) {
                throw new IllegalStateException(
                        "The knowledge base does not exist. Ingest a document before querying.");
            }

            List<String> retrievedDocs = queryCollection(collectionId, queryText);
            if (retrievedDocs.isEmpty()) {
                return "No relevant document passages were found.";
            }
            return generateResponse(queryText, String.join("\n\n", retrievedDocs));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not retrieve context from the vector database.", exception);
        }
    }

    private String generateResponse(String queryText, String context) {
        String prompt = """
                You are an assistant answering questions about ingested documents.
                Answer using only the context below. If the context does not contain
                the answer, say that the documents do not provide enough information.

                Context:
                %s

                Question:
                %s

                Answer:
                """.formatted(context, queryText);
        return llm.generate(prompt);
    }
}
