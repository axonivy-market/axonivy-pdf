package com.axonivy.utils.axonivypdf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import com.aspose.pdf.Document;
import com.aspose.pdf.HighlightAnnotation;
import com.aspose.pdf.License;
import com.aspose.pdf.Page;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.Rotation;
import com.aspose.pdf.TextAbsorber;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.axonivy.utils.axonivypdf.enums.FileExtension;
import com.axonivy.utils.axonivypdf.enums.TextExtractType;
import com.axonivy.utils.axonivypdf.service.PdfFactory;
import com.axonivy.utils.axonivypdf.service.PdfService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class PdfServiceTest {
	private PdfService pdfService;

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

	private UploadedFile mockFile(String name, byte[] data) throws IOException {
		UploadedFile file = mock(UploadedFile.class);
		when(file.getFileName()).thenReturn(name);
		when(file.getInputStream()).thenReturn(new ByteArrayInputStream(data));
		return file;
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
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PDF_with_highlighted_text.pdf");
		ByteArrayOutputStream textStream = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(textStream, StandardCharsets.UTF_8);

		DefaultStreamedContent result = pdfService.extractHighlightedText("PDF_with_highlighted_text.pdf", inputStream,
				textStream, writer, TextExtractType.HIGHLIGHTED);
		inputStream.close();

		assertNotNull(result);
		assertEquals("PDF_with_highlighted_text_extracted_highlighted_text.txt", result.getName());

		String extracted = textStream.toString(StandardCharsets.UTF_8);
		assertTrue(extracted.contains("This line of this document is highlighted for testing purpose."));
	}

	@Test
	void testExtractAllText() throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PDF_with_plain_text.pdf");
		ByteArrayOutputStream textStream = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(textStream, StandardCharsets.UTF_8);

		DefaultStreamedContent result = pdfService.extractAllText("PDF_with_plain_text.pdf", inputStream, textStream,
				writer, TextExtractType.ALL);
		inputStream.close();

		assertNotNull(result);
		assertEquals("PDF_with_plain_text_extracted_text.txt", result.getName());

		String extracted = textStream.toString(StandardCharsets.UTF_8);
		assertTrue(extracted.contains("This is a sample PDF with plain text."));
	}

	@Test
	void testExtractImagesFromPdf() throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PDF_with_images.pdf");

		byte[] pdfBytes = inputStream.readAllBytes();
		inputStream.close();

		UploadedFile uploadedFile = mock(UploadedFile.class);
		when(uploadedFile.getFileName()).thenReturn("PDF_with_images.pdf");
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
	void testConvertPdfToImagesZip() throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PDF_with_2_pages.pdf");
		Document pdfDocument = new Document(inputStream);
		int pageCount = pdfDocument.getPages().size();

		byte[] pdfBytes = inputStream.readAllBytes();
		inputStream.close();

		UploadedFile uploadedFile = mock(UploadedFile.class);
		when(uploadedFile.getFileName()).thenReturn("PDF_with_2_pages.pdf");
		when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

		String extension = ".jpg";
		DefaultStreamedContent result = pdfService.convertPdfToImagesZip(pdfDocument, "PDF_with_2_pages.pdf",
				extension);

		assertNotNull(result);
		assertNotNull(result.getStream());

		ByteArrayInputStream zipBytes = new ByteArrayInputStream(result.getStream().get().readAllBytes());
		int imageFileCount = 0;

		try (ZipInputStream zis = new ZipInputStream(zipBytes)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				assertTrue(entry.getName().endsWith(extension),
						"ZIP entry must have correct extension: " + entry.getName());
				imageFileCount++;
			}
		}

		assertEquals(pageCount, imageFileCount, "Number of images in ZIP must equal number of pages in PDF");
	}

	private UploadedFile mockUploadedFile() throws Exception {
		UploadedFile uploadedFile = mock(UploadedFile.class);
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PDF_with_2_pages.pdf");

		when(uploadedFile.getFileName()).thenReturn("PDF_with_2_pages.pdf");
		when(uploadedFile.getInputStream()).thenReturn(inputStream);

		return uploadedFile;
	}

	private void testConversion(FileExtension extension) throws Exception {
		UploadedFile uploadedFile = mockUploadedFile();

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
		UploadedFile uploadedFile = mockUploadedFile();
		String originalFileName = uploadedFile.getFileName();

		try (InputStream inputStream = uploadedFile.getInputStream();
				Document pdfDocument = new Document(inputStream)) {

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

		try (InputStream inputStream = uploadedFile.getInputStream();
				Document pdfDocument = new Document(inputStream)) {

			int totalPages = pdfDocument.getPages().size();
			int startPage = 1;
			int endPage = Math.min(2, totalPages); // take first 2 pages

			DefaultStreamedContent result = pdfService.handleSplitByRange(pdfDocument, originalFileName, startPage,
					endPage);

			assertNotNull(result, "Result should not be null");
			assertTrue(result.getName().endsWith(".pdf"));

			try (InputStream zipInput = result.getStream().get()) {
				byte[] zipBytes = zipInput.readAllBytes();
				assertTrue(zipBytes.length > 0, "ZIP content should not be empty");
			}
		}
	}
}
