package com.USL.PDF_Tool.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ImageToTextService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public String imageToText(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            return "Error: Could not load image.";
        }

        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat erodedImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.erode(binaryImage, erodedImage, kernel);

        String processedImagePath = imagePath.replace(".png", "-processed.png");
        Imgcodecs.imwrite(processedImagePath, erodedImage);

        return extractTextFromImage(processedImagePath);
    }

    private String extractTextFromImage(String imagePath) {
        File imageFile = new File(imagePath);
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\Dev Mode\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata");

        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            return "Error: " + e.getMessage();
        }
    }
}
