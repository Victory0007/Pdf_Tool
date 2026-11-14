package com.USL.PDF_Tool.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ImageToTextService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Value("${pdf-tool.ocr.tessdata-path:}")
    private String tessdataPath;

    public String imageToText(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            image.release();
            throw new IllegalArgumentException("The image could not be read: " + imagePath);
        }

        Mat grayImage = new Mat();
        Mat binaryImage = new Mat();
        Mat processedImage = new Mat();
        Path temporaryImage = null;

        try {
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(grayImage, binaryImage, 0, 255,
                    Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
            try {
                Imgproc.erode(binaryImage, processedImage, kernel);
            } finally {
                kernel.release();
            }

            temporaryImage = Files.createTempFile("pdf-tool-ocr-", ".png");
            if (!Imgcodecs.imwrite(temporaryImage.toString(), processedImage)) {
                throw new IllegalStateException("Could not write the preprocessed image.");
            }
            return extractTextFromImage(temporaryImage.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create a temporary image for OCR.", exception);
        } finally {
            image.release();
            grayImage.release();
            binaryImage.release();
            processedImage.release();
            if (temporaryImage != null) {
                try {
                    Files.deleteIfExists(temporaryImage);
                } catch (IOException ignored) {
                    // Temporary-file cleanup should not hide the OCR result.
                }
            }
        }
    }

    private String extractTextFromImage(File imageFile) {
        Tesseract tesseract = new Tesseract();
        if (tessdataPath != null && !tessdataPath.isBlank()) {
            tesseract.setDatapath(tessdataPath);
        }

        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException exception) {
            throw new IllegalStateException("OCR processing failed.", exception);
        }
    }
}
