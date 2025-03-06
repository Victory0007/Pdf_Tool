package com.USL.PDF_Tool;

import net.sourceforge.tess4j.*;

import java.io.File;

public class PdfToolApplication {
	public static void main(String[] args) {
		String imagePath = "C:\\Users\\Dev Mode\\IdeaProjects\\PDF_Tool\\src\\main\\resources\\test-image.png.png";

		// Path to Tesseract installation
		File imageFile = new File(imagePath);
		Tesseract tesseract = new Tesseract();

		// Set the path to the Tesseract installation folder
		tesseract.setDatapath("C:\\Users\\Dev Mode\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata");

		try {
			String text = tesseract.doOCR(imageFile);
			System.out.println("Extracted Text: \n" + text);
		} catch (TesseractException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}



/*
package com.USL.PDF_Tool;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfToolApplication {

	public static void main(String[] args) {

		SpringApplication.run(PdfToolApplication.class, args);

	}

}
*/