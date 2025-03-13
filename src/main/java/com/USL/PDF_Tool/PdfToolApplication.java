package com.USL.PDF_Tool;

import com.USL.PDF_Tool.controller.DocumentController;
import com.USL.PDF_Tool.service.ImageToTextService;
import com.USL.PDF_Tool.service.RAGService;
import com.USL.PDF_Tool.service.TextExtractorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PdfToolApplication {
	public static void main(String[] args) {
		SpringApplication.run(PdfToolApplication.class, args);
	}
}
