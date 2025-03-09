package com.USL.PDF_Tool;

import com.USL.PDF_Tool.service.ImageToTextService;
import com.USL.PDF_Tool.service.TextExtractorService;
import com.USL.PDF_Tool.service.VectorDBService;

public class PdfToolApplication {
	public static void main(String[] args) {
		// Initialize required services
		ImageToTextService imageToTextService = new ImageToTextService();
		TextExtractorService textExtractorService = new TextExtractorService(imageToTextService);
		VectorDBService vectorDBService = new VectorDBService(textExtractorService); // Pass required dependency

		String filePath = "C:\\Users\\Dev Mode\\Downloads\\test-image2.png";

		// Process the document using the VectorDB service
		vectorDBService.processDocument(filePath);

		System.out.println("Processing completed.");
	}
}
