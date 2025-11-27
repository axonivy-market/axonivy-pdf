# Axon Ivy PDF
*(formerly Docfactory)*

Axon Ivy PDF is a powerful utility library that provides comprehensive PDF manipulation capabilities for your Axon Ivy applications. This library enables you to perform a wide range of PDF operations including converting various formats to PDF, merging and splitting PDF files, extracting content, and performing advanced page operations.

**Key Features:**
- **Convert to PDF**: Transform HTML content and images into PDF documents. *If you want to convert from other formats, see [PDF Conversion](#pdf-conversion).*
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

> **Note:** This utility supports converting PDF files to various document formats (DOCX, PPTX, XLSX, etc.). To convert other document types (DOC, DOCX, Excel) **to** PDF, please use:
> - [Axon Ivy Words](https://market.axonivy.com/axonivy-words) for Word documents
> - [Axon Ivy Cells](https://market.axonivy.com/axonivy-cells) for Excel spreadsheets

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

3. You can upload multiple images, and they will be merged into one file:

   ![Upload multiple images](images/upload_multiple_images.png)

#### Convert PDF to Other Formats
Export PDFs to various document types including DOCX, PPTX, and XLSX

1. Upload a PDF file.

2. Choose the result type:

   ![Choose type](images/choose_type.png)

3. Press convert and download to get the file in the desired format:

   ![Downloaded PPTX file](images/downloaded_pptx.png)

### PDF Manipulation

#### Merge PDF Files
Combine multiple PDF documents into a single file

1. Upload PDF files:

   ![PDF files](images/multiple_pdf_files.png)

2. Press merge and download to get the merged result file:

   ![Downloaded merged document](images/downloaded_merged_document.png)

#### Split PDF
Divide PDF files by page ranges or extract specific pages

1. Upload a PDF file.

2. Select split criteria:

   ![Split criteria](images/split_criteria.png)

3. If **Split into single-page files** is chosen, the result file will be a zip file containing all the pages of the original document:

   ![Downloaded single page zip](images/downloaded_cat_document_zip.png)

   ![Single page zip content](images/single_pages_zip.png)

4. If **Split by page range** is chosen, determine the start page and end page of the result document:

   ![Page range](images/page_range.png)

   ![Downloaded page range document](images/downloaded_page_range.png)

#### Page Operations
Add watermarks, rotate pages, and perform other page-level modifications

1. Upload a PDF file.

2. Choose which operation you want to perform:

   ![Page operations](images/page_operations.png)

3. Rotate pages by selecting which rotate degree you want:

   ![Rotate degree](images/rotate_degree.png)

4. **Add page number** will add numbering to pages:

   ![Numbered document](images/cats_document_numbered.png)

   ![Numbered document page 1](images/cats_document_page_1.png)

5. **Add header/footer** will add your desired text in the header/footer:

   ![Header footer](images/header_footer.png)

6. **Add watermark** will add your desired text as a watermark:

   ![Watermark document](images/watermark_document.png)

### Content Extraction

#### Extract Text from PDF
Retrieve text content from PDF documents

1. Upload a PDF file.

2. Select extract criteria:

   ![Extract criteria](images/extract_criteria.png)

3. If **Extract all text** is chosen, all text from the document will be extracted:

   ![Downloaded extracted all text](images/downloaded_extract_all_text.png)

   ![Extracted all text](images/extracted_all_text.png)

4. If **Extract highlighted text** is chosen, the highlighted text from the document will be extracted:

   ![Highlighted text document](images/highlighted_text.png)

   ![Downloaded extracted highlighted text](images/downloaded_extracted_highlighted_text.png)

   ![Extracted highlighted text](images/extracted_highlighted_text.png)

#### Extract Images from PDF
Export embedded images from PDF files

1. Upload a PDF file.

2. Press extract images and download to get a zip file containing all the images from the PDF file:

   ![Downloaded extracted images](images/downloaded_extracted_images.png)

   ![Images zip](images/images_zip.png)
