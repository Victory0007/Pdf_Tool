package com.USL.PDF_Tool;

import net.sourceforge.tess4j.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.highgui.HighGui;

import java.io.File;

public class PdfToolApplication {
	public static void main(String[] args) {
		// Load OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String imagePath = "C:\\Users\\Dev Mode\\IdeaProjects\\PDF_Tool\\src\\main\\resources\\test-image.png.png";

		// Load and display the image
		Mat image = Imgcodecs.imread(imagePath);
		if (image.empty()) {
			System.err.println("Error: Could not load image.");
			return;
		}
		HighGui.imshow("Test Image", image);
		HighGui.waitKey(0);

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
