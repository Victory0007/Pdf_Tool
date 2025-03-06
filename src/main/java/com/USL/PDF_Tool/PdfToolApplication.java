package com.USL.PDF_Tool;

import net.sourceforge.tess4j.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.File;

public class PdfToolApplication {
	public static void main(String[] args) {

		OllamaChatModel model = OllamaChatModel.builder()
				.baseUrl("http://localhost:11434") // Default Ollama server
				.modelName("llama3.1") // Correct model specification
				.build();

		String response = model.generate("What is the capital of Nigeria?");
		System.out.println("Llama 3.1 says: " + response);
		/*
		// Load OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String imagePath = "C:\\Users\\Dev Mode\\IdeaProjects\\PDF_Tool\\src\\main\\resources\\test-image.png";

		// Load the image
		Mat image = Imgcodecs.imread(imagePath);
		if (image.empty()) {
			System.err.println("Error: Could not load image.");
			return;
		}


		// Convert to grayscale
		Mat grayImage = new Mat();
		Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

		// Apply Gaussian blur for denoising
		// Mat blurredImage = new Mat();
		// Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

		// Apply Otsu's binarization
		// I should have used adaptive threshold here
		Mat binaryImage = new Mat();
		Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

//		// Apply dilation to thicken text
//		Mat dilatedImage = new Mat();
//		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 2)); // Adjust kernel size for effect
//		Imgproc.dilate(binaryImage, dilatedImage, kernel);

		// Apply erosion to make black text bolder
		Mat erodedImage = new Mat();
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)); // Adjust size if needed
		Imgproc.erode(binaryImage, erodedImage, kernel);

		// Display the processed image
		HighGui.imshow("Processed Image", erodedImage);
		HighGui.waitKey(0);

		// Save the processed image (optional)
		String processedImagePath = "C:\\Users\\Dev Mode\\IdeaProjects\\PDF_Tool\\src\\main\\resources\\processed-image.png";
		Imgcodecs.imwrite(processedImagePath, binaryImage);

		// Path to Tesseract installation
		File imageFile = new File(processedImagePath);
		Tesseract tesseract = new Tesseract();

		// Set the path to the Tesseract installation folder
		tesseract.setDatapath("C:\\Users\\Dev Mode\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata");

		try {
			String text = tesseract.doOCR(imageFile);
			System.out.println("Extracted Text: \n" + text);
		} catch (TesseractException e) {
			System.err.println("Error: " + e.getMessage());
		}

		 */
	}
}
