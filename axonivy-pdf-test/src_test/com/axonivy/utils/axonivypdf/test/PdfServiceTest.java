package com.axonivy.utils.axonivypdf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import com.aspose.pdf.Document;
import com.aspose.pdf.Font;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.HighlightAnnotation;
import com.aspose.pdf.Image;
import com.aspose.pdf.Page;
import com.aspose.pdf.Position;
import com.aspose.pdf.Rotation;
import com.aspose.pdf.TextAbsorber;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.axonivy.utils.axonivypdf.enums.FileExtension;
import com.axonivy.utils.axonivypdf.enums.TextExtractType;
import com.axonivy.utils.axonivypdf.service.PdfFactory;
import com.axonivy.utils.axonivypdf.service.PdfService;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class PdfServiceTest {
  private PdfService pdfService;
  private static final int TEST_IMAGE_FIX_HEIGHT = 50;
  private static final int TEST_IMAGE_FIX_WIDTH = 100;
  private static final String HIGHLIGHTED_TEXT = "This line of this document is highlighted for testing purpose.";;
  private static final String NORMAL_TEXT =
      "This line of this document is normal and not highlighted for testing purpose.";
  private static final String TIMES_NEW_ROMAN_FONT = "TimesRoman";

  @BeforeEach
  void setUp() throws Exception {
    pdfService = new PdfService();
    PdfFactory.loadLicense();
  }

  private byte[] createMockPdf() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document pdf = new Document();
    pdf.getPages().add();
    pdf.save(out);
    pdf.close();
    return out.toByteArray();
  }

  private byte[] createMockPdfWithImages() throws Exception {
    ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
    Document pdf = new Document();

    BufferedImage bufferedImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = bufferedImage.createGraphics();
    g.setColor(java.awt.Color.RED);
    g.fillRect(0, 0, 100, 50);
    g.dispose();

    ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", imgOut);
    byte[] imgBytes = imgOut.toByteArray();

    Page page1 = pdf.getPages().add();
    Image pdfImg1 = new Image();
    pdfImg1.setImageStream(new ByteArrayInputStream(imgBytes));
    pdfImg1.setFixHeight(TEST_IMAGE_FIX_HEIGHT);
    pdfImg1.setFixWidth(TEST_IMAGE_FIX_WIDTH);
    page1.getParagraphs().add(pdfImg1);

    Page page2 = pdf.getPages().add();
    Image pdfImg2 = new Image();
    pdfImg2.setImageStream(new ByteArrayInputStream(imgBytes));
    pdfImg2.setFixHeight(TEST_IMAGE_FIX_HEIGHT);
    pdfImg2.setFixWidth(TEST_IMAGE_FIX_WIDTH);
    page2.getParagraphs().add(pdfImg2);

    pdf.save(pdfOut);
    pdf.close();

    return pdfOut.toByteArray();
  }

  private byte[] createMockPdfWithTwoPages() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document doc = new Document();

    Font font = FontRepository.findFont(TIMES_NEW_ROMAN_FONT);

    Page page1 = doc.getPages().add();
    TextFragment text1 = new TextFragment("This is page 1.");
    text1.getTextState().setFont(font);
    text1.getTextState().setFontSize(12);
    text1.setPosition(new Position(100, 700));
    page1.getParagraphs().add(text1);

    Page page2 = doc.getPages().add();
    TextFragment text2 = new TextFragment("This is page 2.");
    text2.getTextState().setFont(font);
    text2.getTextState().setFontSize(12);
    text2.setPosition(new Position(100, 700));
    page2.getParagraphs().add(text2);

    doc.save(out);
    doc.close();

    return out.toByteArray();
  }

  private UploadedFile mockFile(String name, byte[] data) throws IOException {
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn(name);
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(data));
    return uploadedFile;
  }

  private UploadedFile mockUploadedFile() throws Exception {
    byte[] pdfBytes = createMockPdfWithNormalAndHighlightedText();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    return uploadedFile;
  }

  private byte[] createMockPdfWithNormalAndHighlightedText() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Document doc = new Document();
    Page page = doc.getPages().add();

    Font font = FontRepository.findFont(TIMES_NEW_ROMAN_FONT);

    TextFragment normal = new TextFragment(NORMAL_TEXT);
    normal.getTextState().setFont(font);
    normal.getTextState().setFontSize(12);
    normal.setPosition(new Position(100, 700));
    page.getParagraphs().add(normal);

    TextFragment highlighted = new TextFragment(HIGHLIGHTED_TEXT);
    highlighted.getTextState().setFont(font);
    highlighted.getTextState().setFontSize(12);
    highlighted.setPosition(new Position(100, 680));
    page.getParagraphs().add(highlighted);

    doc.save(out);

    Document reopened = new Document(new ByteArrayInputStream(out.toByteArray()));
    Page reopenedPage = reopened.getPages().get_Item(1);

    TextFragmentAbsorber absorber = new TextFragmentAbsorber(HIGHLIGHTED_TEXT);
    reopenedPage.accept(absorber);

    TextFragment realFragment = absorber.getTextFragments().get_Item(1);

    HighlightAnnotation highlight = new HighlightAnnotation(reopenedPage, realFragment.getRectangle());
    highlight.setColor(com.aspose.pdf.Color.getYellow());

    reopenedPage.getAnnotations().add(highlight);

    ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
    reopened.save(finalOutput);

    doc.close();
    reopened.close();

    return finalOutput.toByteArray();
  }

  @Test
  void testMerge() throws Exception {
    UploadedFiles uploadedFiles = mock(UploadedFiles.class);

    byte[] pdf1 = createMockPdf();
    byte[] pdf2 = createMockPdf();

    UploadedFile file1 = mockFile("a.pdf", pdf1);
    UploadedFile file2 = mockFile("b.pdf", pdf2);

    when(uploadedFiles.getFiles()).thenReturn(List.of(file1, file2));

    DefaultStreamedContent result = pdfService.merge(uploadedFiles);

    assertNotNull(result);
    assertEquals("merged_document.pdf", result.getName());

    byte[] merged = result.getStream().get().readAllBytes();
    assertTrue(new String(merged).contains("%PDF"));
  }

  @Test
  void testConvertHtmlToPdf() throws Exception {
    ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
    Document pdfDoc = new Document();
    pdfDoc.getPages().add();
    pdfDoc.save(pdfOut);

    UploadedFile uploadedFile = mockFile("sample.html", pdfOut.toByteArray());

    DefaultStreamedContent result = pdfService.convertHtmlToPdf(uploadedFile);
    pdfDoc.close();

    assertNotNull(result);
    assertEquals("sample.pdf", result.getName());
  }

  @Test
  void testConvertImageToPdf() throws Exception {
    BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.RED);
    g.fillRect(0, 0, 100, 50);
    g.dispose();

    ByteArrayOutputStream imageBytesOut = new ByteArrayOutputStream();
    ImageIO.write(image, "png", imageBytesOut);
    byte[] pngBytes = imageBytesOut.toByteArray();

    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("test.png");
    when(uploadedFile.getContent()).thenReturn(pngBytes);
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pngBytes));

    DefaultStreamedContent pdf = pdfService.convertImageToPdf(uploadedFile);

    assertNotNull(pdf);
    assertEquals("test.pdf", pdf.getName());
    assertEquals("application/pdf", pdf.getContentType());

    byte[] pdfBytes = pdf.getStream().get().readAllBytes();

    String pdfHeader = new String(pdfBytes, 0, 4);
    assertEquals("%PDF", pdfHeader);
  }

  @Test
  void testAddHeader() throws Exception {
    byte[] pdfBytes = createMockPdf();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    String headerText = "HEADER_TEST";
    DefaultStreamedContent result = pdfService.addHeader(uploadedFile, headerText);

    assertNotNull(result);
    assertEquals("a_with_header.pdf", result.getName());

    byte[] resultPdfBytes = result.getStream().get().readAllBytes();
    assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

    Document checkDoc = new Document(new ByteArrayInputStream(resultPdfBytes));
    TextAbsorber absorber = new TextAbsorber();
    checkDoc.getPages().accept(absorber);
    String extractedText = absorber.getText();
    checkDoc.close();

    assertTrue(extractedText.contains(headerText), "Header text should be present in the resulting PDF");
  }

  @Test
  void testAddFooter() throws Exception {
    byte[] pdfBytes = createMockPdf();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    String footerText = "FOOTER_TEST";
    DefaultStreamedContent result = pdfService.addFooter(uploadedFile, footerText);

    assertNotNull(result);
    assertEquals("a_with_footer.pdf", result.getName());

    byte[] resultPdfBytes = result.getStream().get().readAllBytes();
    assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

    Document checkDoc = new Document(new ByteArrayInputStream(resultPdfBytes));
    TextAbsorber absorber = new TextAbsorber();
    checkDoc.getPages().accept(absorber);
    String extractedText = absorber.getText();
    checkDoc.close();

    assertTrue(extractedText.contains(footerText), "Footer text should be present in the resulting PDF");
  }

  @Test
  void testAddWatermark() throws Exception {
    byte[] pdfBytes = createMockPdf();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    String watermarkText = "WATERMARK_TEST";
    DefaultStreamedContent result = pdfService.addWatermark(uploadedFile, watermarkText);

    assertNotNull(result);
    assertEquals("a_with_watermark.pdf", result.getName());

    byte[] resultPdfBytes = result.getStream().get().readAllBytes();
    assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

    Document checkDoc = new Document(new ByteArrayInputStream(resultPdfBytes));
    TextAbsorber absorber = new TextAbsorber();
    checkDoc.getPages().accept(absorber);
    String extractedText = absorber.getText();
    checkDoc.close();

    assertTrue(extractedText.contains(watermarkText), "Watermark text should be present in the resulting PDF");
  }

  @Test
  void testRotatePages() throws Exception {
    byte[] pdfBytes = createMockPdf();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    DefaultStreamedContent result = pdfService.rotatePages(uploadedFile, Rotation.on90);

    assertNotNull(result);
    assertEquals("a_rotated.pdf", result.getName());

    byte[] resultPdfBytes = result.getStream().get().readAllBytes();
    assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

    Document rotatedDoc = new Document(new ByteArrayInputStream(resultPdfBytes));
    for (Page page : rotatedDoc.getPages()) {
      assertEquals(Rotation.on90, page.getRotate(), "Page rotation should match the requested rotation");
    }
    rotatedDoc.close();
  }

  @Test
  void testAddPageNumbers() throws Exception {
    Document doc = new Document();
    for (int i = 0; i < 3; i++) {
      doc.getPages().add();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    doc.save(baos);
    doc.close();

    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(baos.toByteArray()));

    DefaultStreamedContent result = pdfService.addPageNumbers(uploadedFile);

    assertNotNull(result);
    assertEquals("a_numbered.pdf", result.getName());

    byte[] resultPdfBytes = result.getStream().get().readAllBytes();
    assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

    Document pdfWithNumbers = new Document(new ByteArrayInputStream(resultPdfBytes));
    int totalPages = pdfWithNumbers.getPages().size();

    for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
      String expectedText = "Page " + pageNum + " of " + totalPages;
      TextFragmentAbsorber absorber = new TextFragmentAbsorber(expectedText);

      pdfWithNumbers.getPages().get_Item(pageNum).accept(absorber);

      int found = absorber.getTextFragments().size();
      assertTrue(found > 0, "Expected page number text on page " + pageNum);
    }
    pdfWithNumbers.close();
  }

  @Test
  void testExtractHighlightedText() throws Exception {
    byte[] pdfBytes = createMockPdfWithNormalAndHighlightedText();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
    ByteArrayOutputStream textStream = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(textStream, StandardCharsets.UTF_8);

    DefaultStreamedContent result = pdfService.extractHighlightedText("highlight_test.pdf", inputStream, textStream,
        writer, TextExtractType.HIGHLIGHTED);

    assertNotNull(result);
    assertEquals("highlight_test_extracted_highlighted_text.txt", result.getName());

    String extracted = textStream.toString(StandardCharsets.UTF_8);
    assertFalse(extracted.contains(NORMAL_TEXT));
    assertTrue(extracted.contains(HIGHLIGHTED_TEXT));
  }

  @Test
  void testExtractAllText() throws Exception {
    byte[] pdfBytes = createMockPdfWithNormalAndHighlightedText();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
    ByteArrayOutputStream textStream = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(textStream, StandardCharsets.UTF_8);

    DefaultStreamedContent result =
        pdfService.extractAllText("extract_all_test.pdf", inputStream, textStream, writer, TextExtractType.ALL);

    assertNotNull(result);
    assertEquals("extract_all_test_extracted_text.txt", result.getName());

    String extracted = textStream.toString(StandardCharsets.UTF_8);
    assertTrue(extracted.contains(NORMAL_TEXT));
    assertTrue(extracted.contains(HIGHLIGHTED_TEXT));
  }

  @Test
  void testExtractImagesFromPdf() throws Exception {
    byte[] pdfBytes = createMockPdfWithImages();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    DefaultStreamedContent result = pdfService.extractImagesFromPdf(uploadedFile);

    assertNotNull(result);
    assertNotNull(result.getStream());

    ByteArrayInputStream zipBytes = new ByteArrayInputStream(result.getStream().get().readAllBytes());

    boolean foundPng = false;
    int totalFiles = 0;

    try (ZipInputStream zis = new ZipInputStream(zipBytes)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        totalFiles++;
        if (entry.getName().toLowerCase().endsWith(".png")) {
          foundPng = true;
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          zis.transferTo(bos);
          assertTrue(bos.size() > 50);
        }
      }
    }
    assertTrue(totalFiles > 0);
    assertTrue(foundPng);
  }

  @Test
  void testConvertPdfToImagesZip() throws Exception {
    byte[] pdfBytes = createMockPdfWithTwoPages();
    UploadedFile uploadedFile = mock(UploadedFile.class);
    when(uploadedFile.getFileName()).thenReturn("a.pdf");
    when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

    Document pdfDocument = new Document(pdfBytes);
    int pageCount = pdfDocument.getPages().size();

    String extension = ".jpg";
    DefaultStreamedContent result = pdfService.convertPdfToImagesZip(pdfDocument, "PDF_with_2_pages.pdf", extension);

    assertNotNull(result);
    assertNotNull(result.getStream());

    ByteArrayInputStream zipBytes = new ByteArrayInputStream(result.getStream().get().readAllBytes());
    int imageFileCount = 0;

    try (ZipInputStream zis = new ZipInputStream(zipBytes)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        assertTrue(entry.getName().endsWith(extension), "ZIP entry must have correct extension: " + entry.getName());
        imageFileCount++;
      }
    }

    assertEquals(pageCount, imageFileCount, "Number of images in ZIP must equal number of pages in PDF");
  }

  private void testConversion(FileExtension extension) throws Exception {
    UploadedFile uploadedFile = mockFile("a.pdf", createMockPdfWithTwoPages());

    DefaultStreamedContent result = pdfService.convertPdfToOtherDocumentTypes(uploadedFile, extension);

    assertNotNull(result, "Result should not be null");

    try (InputStream inputStream = result.getStream().get()) {
      byte[] bytes = inputStream.readAllBytes();
      assertTrue(bytes.length > 0, "Converted file should not be empty");
    }

    assertTrue(result.getName().endsWith("." + extension.name().toLowerCase()),
        "Filename should have the correct extension");
  }

  @Test
  void testConvertPdfToDocx() throws Exception {
    testConversion(FileExtension.DOCX);
  }

  @Test
  void testConvertPdfToXlsx() throws Exception {
    testConversion(FileExtension.XLSX);
  }

  @Test
  void testConvertPdfToPptx() throws Exception {
    testConversion(FileExtension.PPTX);
  }

  @Test
  void testConvertPdfToHtml() throws Exception {
    testConversion(FileExtension.HTML);
  }

  @Test
  void testHandleSplitIntoSinglePages() throws Exception {
    UploadedFile uploadedFile = mockFile("a.pdf", createMockPdfWithTwoPages());
    String originalFileName = uploadedFile.getFileName();

    try (InputStream inputStream = uploadedFile.getInputStream(); Document pdfDocument = new Document(inputStream)) {

      DefaultStreamedContent result = pdfService.handleSplitIntoSinglePages(pdfDocument, originalFileName);

      assertNotNull(result, "Resulting DefaultStreamedContent should not be null");
      assertTrue(result.getName().endsWith(".zip"), "Result file should have a .zip extension");

      try (InputStream zipInput = result.getStream().get()) {
        byte[] zipBytes = zipInput.readAllBytes();
        assertTrue(zipBytes.length > 0, "ZIP file should not be empty");
      }
    }
  }

  @Test
  void testHandleSplitByRangeValid() throws Exception {
    UploadedFile uploadedFile = mockUploadedFile();
    String originalFileName = uploadedFile.getFileName();

    try (InputStream inputStream = uploadedFile.getInputStream(); Document pdfDocument = new Document(inputStream)) {

      int totalPages = pdfDocument.getPages().size();
      int startPage = 1;
      int endPage = Math.min(2, totalPages); // take first 2 pages

      DefaultStreamedContent result = pdfService.handleSplitByRange(pdfDocument, originalFileName, startPage, endPage);

      assertNotNull(result, "Result should not be null");
      assertTrue(result.getName().endsWith(".pdf"));

      try (InputStream zipInput = result.getStream().get()) {
        byte[] zipBytes = zipInput.readAllBytes();
        assertTrue(zipBytes.length > 0, "ZIP content should not be empty");
      }
    }
  }
}
