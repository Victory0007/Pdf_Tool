package com.USL.PDF_Tool;

import com.USL.PDF_Tool.service.ImageToTextService;
import com.USL.PDF_Tool.service.TextExtractorService;
import com.USL.PDF_Tool.service.VectorDBService;
import com.USL.PDF_Tool.service.RAGService;

import java.io.IOException;

public class PdfToolApplication {
	public static void main(String[] args) throws IOException {
		// Initialize required services
		ImageToTextService imageToTextService = new ImageToTextService();
		TextExtractorService textExtractorService = new TextExtractorService(imageToTextService);
		VectorDBService vectorDBService = new VectorDBService(textExtractorService);
		RAGService ragService = new RAGService(); // Initialize RAG service

		String filePath = "C:\\Users\\Dev Mode\\Downloads\\Kachasi-brochure.pdf";

		// Step 1: Process the document (extract and store embeddings)
		System.out.println("Extracting text and processing document...");
		vectorDBService.processDocument(filePath);
		System.out.println("Processing completed.");

		// Step 2: Query the RAG system
		String question = "What is Kachasi?";
		System.out.println("Querying the RAG system...");
		String answer = ragService.getAnswer(question);

		// Step 3: Display the answer
		System.out.println("Answer: " + answer);
	}
}
