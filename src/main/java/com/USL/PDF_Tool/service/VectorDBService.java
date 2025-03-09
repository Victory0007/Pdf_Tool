package com.USL.PDF_Tool.service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.*;

public class VectorDBService {
    private static final String CHROMA_URL = "http://localhost:8000"; // Ensure ChromaDB is running
    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings"; // Ollama server

    public static void main(String[] args) {
        try {
            // Extracted text from PDF/Image (Replace with actual method call)
            String extractedText = "Sample extracted text from a document.";

            // Split text into chunks
            List<String> chunks = splitText(extractedText, 1000, 200);

            // Generate unique IDs for chunks
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                ids.add(UUID.randomUUID().toString());
            }

            // Collection handling
            String collectionName = "test-collection";
            String collectionId = getCollectionId(collectionName);
            if (collectionId == null) {
                collectionId = createCollection(collectionName);
            }

            // Generate embeddings for chunks using Ollama
            List<List<Double>> embeddings = generateEmbeddings(chunks);

            // Store chunks in ChromaDB
            addDocuments(collectionId, chunks, ids, embeddings);

            // Query ChromaDB
            queryCollection(collectionId, "Relevant query here");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += (chunkSize - overlap)) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    private static String getCollectionId(String collectionName) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections";
        String response = sendGetRequest(url);

        // Parse response as a JSONArray instead of JSONObject
        JSONArray collectionsArray = new JSONArray(response);

        for (int i = 0; i < collectionsArray.length(); i++) {
            JSONObject collection = collectionsArray.getJSONObject(i);
            if (collection.getString("name").equals(collectionName)) {
                return collection.getString("id"); // Return collection UUID
            }
        }
        return null;
    }



    private static String createCollection(String collectionName) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections";
        JSONObject payload = new JSONObject();
        payload.put("name", collectionName);

        String response = sendPostRequest(url, payload.toString());
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse.has("id")) {
            return jsonResponse.getString("id");
        } else {
            throw new IOException("Failed to create collection: " + response);
        }
    }

    private static List<List<Double>> generateEmbeddings(List<String> texts) throws IOException {
        List<List<Double>> embeddings = new ArrayList<>();

        for (String text : texts) {
            JSONObject payload = new JSONObject();
            payload.put("model", "nomic-embed-text");
            payload.put("prompt", text);

            String response = sendPostRequest(OLLAMA_URL, payload.toString());
            JSONObject jsonResponse = new JSONObject(response);

            if (!jsonResponse.has("embedding")) {
                System.err.println("Error: Embedding not found in response for text: " + text);
                continue;
            }

            JSONArray embeddingArray = jsonResponse.getJSONArray("embedding");
            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < embeddingArray.length(); i++) {
                embedding.add(embeddingArray.getDouble(i));
            }
            embeddings.add(embedding);
        }

        return embeddings;
    }

    private static void addDocuments(String collectionId, List<String> texts, List<String> ids, List<List<Double>> embeddings) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections/" + collectionId + "/add";

        JSONObject payload = new JSONObject();
        payload.put("ids", new JSONArray(ids));
        payload.put("documents", new JSONArray(texts));
        payload.put("embeddings", new JSONArray(embeddings));

        // Add metadata
        JSONArray metadatas = new JSONArray();
        for (String text : texts) {
            JSONObject metadata = new JSONObject();
            metadata.put("source", "document");
            metadatas.put(metadata);
        }
        payload.put("metadatas", metadatas);

        String response = sendPostRequest(url, payload.toString());
        System.out.println("Add Documents Response: " + response);
    }

    private static void queryCollection(String collectionId, String queryText) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections/" + collectionId + "/query";

        // Generate embedding for the query
        List<List<Double>> queryEmbedding = generateEmbeddings(Collections.singletonList(queryText));
        if (queryEmbedding.isEmpty()) {
            System.err.println("Error: No embeddings generated for query.");
            return;
        }

        JSONObject payload = new JSONObject();
        payload.put("query_embeddings", new JSONArray(queryEmbedding));

        String response = sendPostRequest(url, payload.toString());
        System.out.println("Query Response: " + response);
    }

    private static String sendPostRequest(String url, String jsonPayload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send JSON payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return handleHttpResponse(conn, url);
    }

    private static String sendGetRequest(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return handleHttpResponse(conn, url);
    }

    private static String handleHttpResponse(HttpURLConnection conn, String url) throws IOException {
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            System.err.println("HTTP Error: " + responseCode + " from " + url);
            if (conn.getErrorStream() != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    System.err.println("Error Response: " + errorResponse);
                }
            }
            return "{}";
        }
    }
}
