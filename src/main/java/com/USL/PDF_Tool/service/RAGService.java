package com.USL.PDF_Tool.service;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class RAGService {
    private static final String CHROMA_URL = "http://localhost:8000";
    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";
    private static final String MODEL_NAME = "llama3.1"; // Ensure the model is available on Ollama

    private final OllamaChatModel chatModel;

    public RAGService() {
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(MODEL_NAME)
                .build();
    }

    public String getAnswer(String query) {
        try {
            // Retrieve relevant documents from the vector database
            String context = retrieveRelevantDocuments(query);

            if (context.isEmpty()) {
                return "I'm sorry, but I don't have enough information to answer that.";
            }

            // Formulate a system prompt ensuring strict reliance on retrieved knowledge
            String prompt = "You are a helpful assistant. Answer the following question using ONLY the provided information. "
                    + "If you don't know the answer, say 'I don't know.'\n\n"
                    + "Context:\n" + context + "\n\n"
                    + "Question: " + query;

            // Get the AI-generated response
            return chatModel.generate(prompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving answer.";
        }
    }

    private String retrieveRelevantDocuments(String query) throws Exception {
        List<List<Double>> queryEmbedding = generateEmbeddings(query);
        if (queryEmbedding.isEmpty()) {
            return "";
        }

        String collectionId = "test-collection"; // Ensure this matches your stored data
        String url = CHROMA_URL + "/api/v1/collections/" + collectionId + "/query";

        JSONObject payload = new JSONObject();
        payload.put("query_embeddings", new JSONArray(queryEmbedding));
        payload.put("top_k", 3); // Retrieve the top 3 most relevant documents

        String response = sendPostRequest(url, payload.toString());

        // Extract relevant context
        JSONObject jsonResponse = new JSONObject(response);
        if (!jsonResponse.has("documents") || jsonResponse.getJSONArray("documents").isEmpty()) {
            return "";
        }

        JSONArray documentsArray = jsonResponse.getJSONArray("documents");
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < documentsArray.length(); i++) {
            contextBuilder.append(documentsArray.getString(i)).append("\n");
        }

        return contextBuilder.toString();
    }

    private List<List<Double>> generateEmbeddings(String text) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("model", "nomic-embed-text");
        payload.put("prompt", text);

        String response = sendPostRequest(OLLAMA_URL, payload.toString());
        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.has("embedding")) {
            return Collections.emptyList();
        }

        JSONArray embeddingArray = jsonResponse.getJSONArray("embedding");
        List<Double> embedding = embeddingArray.toList().stream()
                .map(obj -> ((Number) obj).doubleValue())
                .toList();

        return Collections.singletonList(embedding);
    }

    private String sendPostRequest(String url, String jsonPayload) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                return response.toString();
            }
        } else {
            return "{}";
        }
    }
}
