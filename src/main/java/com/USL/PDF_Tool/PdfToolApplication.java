package com.USL.PDF_Tool;

import com.USL.PDF_Tool.service.RAGService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.ApplicationContext;

public class PdfToolApplication {
	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext("com.USL.PDF_Tool.service");

		RAGService ragService = context.getBean(RAGService.class);
		ragService.processDocument("C:\\Users\\Dev Mode\\Downloads\\kachasi-brochure.pdf");

		String response = ragService.queryRAG("What is kachasi?");
		System.out.println("Response: " + response);
	}
}
