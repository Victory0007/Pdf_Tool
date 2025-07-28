
# 📄 PDF Tool

PDF Tool is a Spring Boot-based backend application designed for processing and analyzing PDF documents and images. It supports functionality such as image-to-text conversion, text extraction, Retrieval-Augmented Generation (RAG), and vector database operations.

## 🚀 Features

- 📄 Extract text from PDF documents and images
- 🧠 Retrieval-Augmented Generation (RAG) integration
- 🧾 OCR support for image-to-text conversion
- 🧠 Vector database service for intelligent querying
- 🧪 Basic unit tests for application components

## 📁 Project Structure

```
Pdf_Tool-main/
├── src/
│   ├── main/
│   │   ├── java/com/USL/PDF_Tool/
│   │   │   ├── PdfToolApplication.java
│   │   │   ├── controller/
│   │   │   │   └── DocumentController.java
│   │   │   └── service/
│   │   │       ├── ImageToTextService.java
│   │   │       ├── RAGService.java
│   │   │       ├── TextExtractorService.java
│   │   │       └── VectorDBService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── processed-image.png
│   │       ├── test-image.png
│   │       ├── test-image2.png
│   │       └── test-image3.jpg
│   └── test/
│       └── java/com/USL/PDF_Tool/
│           └── PdfToolApplicationTests.java
├── pom.xml
├── mvnw / mvnw.cmd
├── .gitignore
└── .gitattributes
```

## 🛠️ Prerequisites

- **Java 17+**
- **Maven 3.x**
- An IDE like IntelliJ IDEA or VSCode

## 🧪 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/Pdf_Tool-main.git
cd Pdf_Tool-main
```

### 2. Build the Application

```bash
./mvnw clean install
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The server should start on `http://localhost:8080`.

## 🔬 Running Tests

Run unit tests using:

```bash
./mvnw test
```

## 📦 API Overview

The backend exposes a controller named `DocumentController` where image and PDF processing routes are handled. You can extend the controller or refer to it to understand how to integrate with the services.

## 📂 Configuration

Edit `src/main/resources/application.properties` to configure app behavior, including file paths and database endpoints if applicable.

## 🧠 About the Services

- `ImageToTextService`: Converts images to text using OCR.
- `TextExtractorService`: Extracts raw text content from documents.
- `VectorDBService`: Indexes and retrieves documents based on vector embeddings.
- `RAGService`: Facilitates RAG pipeline for intelligent document querying.

