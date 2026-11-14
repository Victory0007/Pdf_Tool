package com.USL.PDF_Tool.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VectorDBService {
    private final String chromaUrl;
    private final String ollamaEmbeddingUrl;
    private final String embeddingModel;
    protected final TextExtractorService textExtractorService;

    public VectorDBService(
            TextExtractorService textExtractorService,
            @Value("${pdf-tool.chroma.base-url:http://localhost:8000}") String chromaUrl,
            @Value("${pdf-tool.ollama.base-url:http://localhost:11434}") String ollamaUrl,
            @Value("${pdf-tool.ollama.embedding-model:nomic-embed-text}") String embeddingModel) {
        this.textExtractorService = textExtractorService;
        this.chromaUrl = removeTrailingSlash(chromaUrl);
        this.ollamaEmbeddingUrl = removeTrailingSlash(ollamaUrl) + "/api/embeddings";
        this.embeddingModel = embeddingModel;
    }

    public void processDocument(String filePath) {
        String extractedText = textExtractorService.extractText(filePath);
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("No text could be extracted from the document.");
        }

        List<String> chunks = splitText(extractedText, 1000, 200);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("The document did not produce any text chunks.");
        }

        try {
            String collectionId = getCollectionId("knowledge-base");
            if (collectionId == null) {
                collectionId = createCollection("knowledge-base");
            }

            List<List<Double>> embeddings = generateEmbeddings(chunks);
            if (embeddings.size() != chunks.size()) {
                throw new IOException("Embedding count did not match the number of document chunks.");
            }

            List<String> ids = new ArrayList<>();
            for (String chunk : chunks) {
                ids.add(UUID.nameUUIDFromBytes(chunk.getBytes(StandardCharsets.UTF_8)).toString());
            }
            addDocuments(collectionId, chunks, ids, embeddings);
        } catch (IOException exception) {
            throw new IllegalStateException("Document ingestion failed while communicating with Ollama or ChromaDB.", exception);
        }
    }

    protected List<String> splitText(String text, int chunkSize, int overlap) {
        if (chunkSize <= 0 || overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("Chunk size must be positive and overlap must be between zero and chunk size.");
        }

        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int step = chunkSize - overlap;
        for (int start = 0; start < text.length(); start += step) {
            chunks.add(text.substring(start, Math.min(start + chunkSize, text.length())));
        }
        return chunks;
    }

    protected String getCollectionId(String collectionName) throws IOException {
        String response = sendGetRequest(chromaUrl + "/api/v1/collections");
        JSONArray collectionsArray = new JSONArray(response);
        for (int i = 0; i < collectionsArray.length(); i++) {
            JSONObject collection = collectionsArray.getJSONObject(i);
            if (collection.getString("name").equals(collectionName)) {
                return collection.getString("id");
            }
        }
        return null;
    }

    protected String createCollection(String collectionName) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("name", collectionName);
        JSONObject response = new JSONObject(sendPostRequest(
                chromaUrl + "/api/v1/collections", payload.toString()));
        String id = response.optString("id", null);
        if (id == null || id.isBlank()) {
            throw new IOException("ChromaDB did not return a collection ID.");
        }
        return id;
    }

    protected List<List<Double>> generateEmbeddings(List<String> texts) throws IOException {
        List<List<Double>> embeddings = new ArrayList<>();
        for (String text : texts) {
            JSONObject payload = new JSONObject();
            payload.put("model", embeddingModel);
            payload.put("prompt", text);
            JSONObject response = new JSONObject(sendPostRequest(ollamaEmbeddingUrl, payload.toString()));
            JSONArray values = response.optJSONArray("embedding");
            if (values == null || values.isEmpty()) {
                throw new IOException("Ollama returned an empty embedding.");
            }

            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < values.length(); i++) {
                embedding.add(values.getDouble(i));
            }
            embeddings.add(embedding);
        }
        return embeddings;
    }

    protected void addDocuments(String collectionId, List<String> texts,
                                List<String> ids, List<List<Double>> embeddings) throws IOException {
        if (texts.size() != ids.size() || texts.size() != embeddings.size()) {
            throw new IllegalArgumentException("Documents, IDs and embeddings must have matching sizes.");
        }

        String url = chromaUrl + "/api/v1/collections/" + collectionId + "/upsert";
        JSONObject payload = new JSONObject();
        payload.put("ids", new JSONArray(ids));
        payload.put("documents", new JSONArray(texts));
        payload.put("embeddings", new JSONArray(embeddings));
        sendPostRequest(url, payload.toString());
    }

    private String sendPostRequest(String url, String jsonPayload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }
            return handleHttpResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String sendGetRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            return handleHttpResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String handleHttpResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();

        StringBuilder body = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
        }

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " from " + connection.getURL() + ": " + body);
        }
        return body.toString();
    }

    protected List<String> queryCollection(String collectionId, String queryText) throws IOException {
        List<List<Double>> queryEmbedding = generateEmbeddings(Collections.singletonList(queryText));
        JSONObject payload = new JSONObject();
        payload.put("query_embeddings", new JSONArray(queryEmbedding));
        payload.put("n_results", 5);

        String response = sendPostRequest(
                chromaUrl + "/api/v1/collections/" + collectionId + "/query", payload.toString());
        JSONObject jsonResponse = new JSONObject(response);
        List<String> retrievedDocs = new ArrayList<>();

        JSONArray docsArray = jsonResponse.optJSONArray("documents");
        if (docsArray != null && docsArray.length() > 0) {
            JSONArray firstResult = docsArray.optJSONArray(0);
            if (firstResult != null) {
                for (int i = 0; i < firstResult.length(); i++) {
                    String document = firstResult.optString(i, "");
                    if (!document.isBlank()) {
                        retrievedDocs.add(document);
                    }
                }
            }
        }
        return retrievedDocs;
    }

    private static String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
