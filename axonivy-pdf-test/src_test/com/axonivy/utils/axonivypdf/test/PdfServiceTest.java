package com.axonivy.utils.axonivypdf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.TextAbsorber;
import com.axonivy.utils.axonivypdf.service.PdfFactory;
import com.axonivy.utils.axonivypdf.service.PdfService;

import ch.ivyteam.ivy.environment.Ivy;

public class PdfServiceTest {
	private PdfService pdfService;

	@PostConstruct
	public void init() {
		PdfFactory.loadLicense();
	}

	@BeforeEach
	void setUp() throws Exception {
		pdfService = new PdfService();

		License license = new License();
		try (InputStream lic = getClass().getClassLoader().getResourceAsStream("aspose.test.lic")) {
			license.setLicense(lic);
		}
	}

	private byte[] createMockPdf() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document pdf = new Document();
		pdf.getPages().add();
		pdf.getPages().insert(2);
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
		// --- 1. Create a real minimal PDF ---
		byte[] pdfBytes = createMockPdf(); // THIS is your real PDF content

		// --- 2. Mock UploadedFile using the REAL PDF bytes ---
		UploadedFile uploadedFile = mock(UploadedFile.class);
		when(uploadedFile.getFileName()).thenReturn("a.pdf");
		when(uploadedFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

		String headerText = "HEADER_TEST";

		// --- 3. Execute ---
		DefaultStreamedContent result = pdfService.addHeader(uploadedFile, headerText);

		assertNotNull(result);
		assertEquals("a_with_header.pdf", result.getName());

		byte[] resultPdfBytes = result.getStream().get().readAllBytes();
		assertTrue(new String(resultPdfBytes, 0, 4).equals("%PDF"));

		// --- 4. Extract text to verify header was stamped ---
		Document checkDoc = new Document(new ByteArrayInputStream(resultPdfBytes));
		TextAbsorber absorber = new TextAbsorber();
		checkDoc.getPages().accept(absorber);
		String extractedText = absorber.getText();
		checkDoc.close();

		assertTrue(extractedText.contains(headerText), "Header text should be present in the resulting PDF");
	}
}
