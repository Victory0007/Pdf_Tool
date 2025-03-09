package com.USL.PDF_Tool.service;
import dev.langchain4j.model.ollama.OllamaChatModel;



public class RAGService {

    public String Response(){
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Default Ollama server
                .modelName("llama3.1") // Correct model specification
                .build();

        String response = model.generate("who are you?");
        return "Llama 3.1 says: " + response;
    }
}
