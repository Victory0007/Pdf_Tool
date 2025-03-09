package com.USL.PDF_Tool;


import com.USL.PDF_Tool.service.ImageToTextService;
import com.USL.PDF_Tool.service.TextExtractorService;

public class PdfToolApplication {
	public static void main(String[] args) {
		ImageToTextService imageToTextService = new ImageToTextService();
		TextExtractorService textExtractorService = new TextExtractorService(imageToTextService); // Pass dependency

		String result = textExtractorService.extractText("C:\\Users\\Dev Mode\\Downloads\\test-image2.png");
		System.out.println(result);
	}
}

