# Axon Ivy PDF
*(formerly Docfactory)*

Axon Ivy PDF is a powerful utility library that provides comprehensive PDF manipulation capabilities for your Axon Ivy applications. This library enables you to perform a wide range of PDF operations including converting various formats to PDF, merging and splitting PDF files, extracting content, and performing advanced page operations.

**Key Features:**
- **Convert to PDF**: Transform HTML content and images into PDF documents
- **Convert from PDF**: Export PDF files to other document formats (DOCX, PPTX, XLSX, etc.)
- **Merge PDFs**: Combine multiple PDF files into a single document
- **Split PDFs**: Divide PDF files into separate documents by page ranges
- **Extract Content**: Extract text and images from PDF documents
- **Page Operations**: Add watermarks, rotate pages, and manipulate PDF structure

## Important: Use PdfFactory for All Operations
**All PDF operations must use the `PdfFactory` class** to ensure proper Aspose license handling. Direct use of Aspose.PDF APIs without going through `PdfFactory` may result in unlicensed operation (evaluation mode) with functional limitations.

### License Management
The `PdfFactory` automatically handles Aspose.PDF licensing through Axon Ivy's third-party license service. No manual license configuration is required when using the factory methods.

For specific usage examples, refer to the demo processes included in the `axonivy-pdf-demo` module.

**Example Usage:**
```java
import com.axonivy.utils.axonivypdf.service.PdfFactory;

// For operations that return a value
var result = PdfFactory.get(() -> {
    // Your PDF operation code here
    return yourPdfOperation();
});

// For operations that don't return a value
PdfFactory.run(() -> {
    // Your PDF operation code here
    yourPdfOperation();
});
```

## Demo

The Axon Ivy PDF library includes comprehensive demo processes showcasing all available functionality:

### PDF Conversion

#### Convert HTML to PDF
Transform HTML content into professional PDF documents:

1. Upload an HTML file:

   ![HTML file](images/test_html.png)

2. Press convert and download to get the file in PDF format:

   ![Downloaded PDF file](images/downloaded_test_html_pdf.png)

   ![PDF file](images/test_html_pdf.png)

#### Convert Images to PDF
Create PDF files from image formats (PNG, JPEG, etc.)

1. Upload an image:

   ![Test image](images/test_image.png)

2. Press convert and download to get the file in PDF format:

   ![Downloaded PDF file](images/downloaded_test_image_pdf.png)
   
3. You can upload multiple images and they will be merged into one file:

   ![Upload multiple images](images/upload_multiple_images.png)

#### Convert PDF to Other Formats
Export PDFs to various document types including DOCX, PPTX, and XLSX

### PDF Manipulation

#### Merge PDF Files
Combine multiple PDF documents into a single file

#### Split PDF
Divide PDF files by page ranges or extract specific pages

#### Page Operations
Add watermarks, rotate pages, and perform other page-level modifications

### Content Extraction

#### Extract Text from PDF
Retrieve text content from PDF documents

#### Extract Images from PDF
Export embedded images from PDF files

Each demo process includes practical examples and can be used as a starting point for implementing PDF operations in your own processes.