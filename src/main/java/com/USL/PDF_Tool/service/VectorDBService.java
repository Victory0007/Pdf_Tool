package com.USL.PDF_Tool.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VectorDBService {
    private static final String CHROMA_URL = "http://localhost:8000";
    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";

    protected final TextExtractorService textExtractorService;

    @Autowired
    public VectorDBService(TextExtractorService textExtractorService) {
        this.textExtractorService = textExtractorService;
    }

    public void processDocument(String filePath) {
        try {
            String extractedText = textExtractorService.extractText(filePath);
            if (extractedText.startsWith("Error")) {
                System.err.println(extractedText);
                return;
            }

            List<String> chunks = splitText(extractedText, 1000, 200);
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                ids.add(UUID.randomUUID().toString());
            }

            String collectionName = "knowledge-base";
            String collectionId = getCollectionId(collectionName);
            if (collectionId == null) {
                collectionId = createCollection(collectionName);
            }

            List<List<Double>> embeddings = generateEmbeddings(chunks);
            addDocuments(collectionId, chunks, ids, embeddings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += (chunkSize - overlap)) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    protected String getCollectionId(String collectionName) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections";
        String response = sendGetRequest(url);
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
        String url = CHROMA_URL + "/api/v1/collections";
        JSONObject payload = new JSONObject();
        payload.put("name", collectionName);
        String response = sendPostRequest(url, payload.toString());
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.optString("id", null);
    }

    protected List<List<Double>> generateEmbeddings(List<String> texts) throws IOException {
        List<List<Double>> embeddings = new ArrayList<>();
        for (String text : texts) {
            JSONObject payload = new JSONObject();
            payload.put("model", "nomic-embed-text");
            payload.put("prompt", text);
            String response = sendPostRequest(OLLAMA_URL, payload.toString());
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("embedding")) {
                JSONArray embeddingArray = jsonResponse.getJSONArray("embedding");
                List<Double> embedding = new ArrayList<>();
                for (int i = 0; i < embeddingArray.length(); i++) {
                    embedding.add(embeddingArray.getDouble(i));
                }
                embeddings.add(embedding);
            }
        }
        return embeddings;
    }

    protected void addDocuments(String collectionId, List<String> texts, List<String> ids, List<List<Double>> embeddings) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections/" + collectionId + "/add";
        JSONObject payload = new JSONObject();
        payload.put("ids", new JSONArray(ids));
        payload.put("documents", new JSONArray(texts));
        payload.put("embeddings", new JSONArray(embeddings));
        sendPostRequest(url, payload.toString());
    }

    private String sendPostRequest(String url, String jsonPayload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }
        return handleHttpResponse(conn);
    }

    private String sendGetRequest(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return handleHttpResponse(conn);
    }

    private String handleHttpResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
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

    protected List<String> queryCollection(String collectionId, String queryText) throws IOException {
        String url = CHROMA_URL + "/api/v1/collections/" + collectionId + "/query";
        List<List<Double>> queryEmbedding = generateEmbeddings(Collections.singletonList(queryText));

        if (queryEmbedding.isEmpty()) {
            System.err.println("Error: No embeddings generated for query.");
            return Collections.emptyList();
        }

        JSONObject payload = new JSONObject();
        payload.put("query_embeddings", new JSONArray(queryEmbedding));

        String response = sendPostRequest(url, payload.toString());
        JSONObject jsonResponse = new JSONObject(response);

        List<String> retrievedDocs = new ArrayList<>();

        if (jsonResponse.has("documents")) {
            JSONArray docsArray = jsonResponse.getJSONArray("documents");

            for (int i = 0; i < docsArray.length(); i++) {
                JSONArray innerArray = docsArray.optJSONArray(i);
                if (innerArray != null) {
                    for (int j = 0; j < innerArray.length(); j++) {
                        retrievedDocs.add(innerArray.getString(j));
                    }
                }
            }
        }

        return retrievedDocs;
    }

}
