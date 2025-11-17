package com.axonivy.utils.axonivypdf.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import com.aspose.pdf.Annotation;
import com.aspose.pdf.Color;
import com.aspose.pdf.Document;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.FontStyles;
import com.aspose.pdf.HighlightAnnotation;
import com.aspose.pdf.HorizontalAlignment;
import com.aspose.pdf.Image;
import com.aspose.pdf.MarginInfo;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageNumberStamp;
import com.aspose.pdf.Rotation;
import com.aspose.pdf.TextAbsorber;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentCollection;
import com.aspose.pdf.TextStamp;
import com.aspose.pdf.VerticalAlignment;
import com.aspose.pdf.WatermarkArtifact;
import com.aspose.pdf.facades.EncodingType;
import com.aspose.pdf.facades.FontStyle;
import com.aspose.pdf.facades.FormattedText;
import com.aspose.pdf.facades.PdfFileEditor;
import com.axonivy.utils.axonivypdf.enums.FileExtension;
import com.axonivy.utils.axonivypdf.enums.TextExtractType;

public class PdfService {
	private static final String DOT = ".";
	private static final float DEFAULT_FONT_SIZE = 12;
	private static final float DEFAULT_PAGE_NUMBER_FONT_SIZE = 14.0F;
	private static final float DEFAULT_WATERMARK_FONT_SIZE = 36.0F;
	private static final double DEFAULT_WATERMARK_OPACITY = 0.5;
	private static final double DEFAULT_WATERMARK_ROTATION = 45;
	private static final String EXTRACTED_TEXT = "extracted_text";
	private static final String EXTRACTED_HIGHLIGHTED_TEXT = "extracted_highlighted_text";
	private static final String TIMES_NEW_ROMAN_FONT = "Times New Roman";
	private static final String TEMP_ZIP_FILE_NAME = "split_pages";
	private static final String PDF_CONTENT_TYPE = "application/pdf";
	private static final String SAMPLE_WATERMARK = "ASPOSE_WATERMARK";
	private static final String SPLIT_PAGE_NAME_PATTERN = "%s_page_%d";
	private static final String ROTATED_DOCUMENT_NAME_PATTERN = "%s_rotated" + FileExtension.PDF.getExtension();
	private static final String DOCUMENT_WITH_HEADER_NAME_PATTERN = "%s_with_header" + FileExtension.PDF.getExtension();
	private static final String DOCUMENT_WITH_FOOTER_NAME_PATTERN = "%s_with_footer" + FileExtension.PDF.getExtension();
	private static final String DOCUMENT_WITH_PAGE_NUMBER_NAME_PATTERN = "%s_numbered"
			+ FileExtension.PDF.getExtension();
	private static final String TXT_FILE_NAME_PATTERN = "%s_%s" + FileExtension.TXT.getExtension();
	private static final String MERGED_DOCUMENT_NAME = "merged_document" + FileExtension.PDF.getExtension();
	private static final String IMAGE_NAME_PATTERN = "%s_page_%d_image_%d" + FileExtension.PNG.getExtension();
	private static final String IMAGE_ZIP_NAME_PATTERN = "%s_images_zipped" + FileExtension.ZIP.getExtension();
	private static final String SPLIT_PAGE_ZIP_NAME_PATTERN = "%s_split_zipped" + FileExtension.ZIP.getExtension();
	private static final String RANGE_SPLIT_FILE_NAME_PATTERN = "%s_page_%d_to_%d" + FileExtension.PDF.getExtension();
	private static final String FILE_NAME_WITH_WATERMARK_PATTERN = "%s_with_watermark"
			+ FileExtension.PDF.getExtension();

	public DefaultStreamedContent addHeader(UploadedFile uploadedFile, String headerText) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Document pdfDocument = new Document(input);

		TextStamp textStamp = new TextStamp(headerText);
		textStamp.setTopMargin(10);
		textStamp.setHorizontalAlignment(HorizontalAlignment.Center);
		textStamp.setVerticalAlignment(VerticalAlignment.Top);

		for (Page page : pdfDocument.getPages()) {
			page.addStamp(textStamp);
		}
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateFileWithHeaderName(originalFileName));
	}

	public DefaultStreamedContent addFooter(UploadedFile uploadedFile, String footerText) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Document pdfDocument = new Document(input);

		TextStamp textStamp = new TextStamp(footerText);
		textStamp.setBottomMargin(10);
		textStamp.setHorizontalAlignment(HorizontalAlignment.Center);
		textStamp.setVerticalAlignment(VerticalAlignment.Bottom);

		for (Page page : pdfDocument.getPages()) {
			page.addStamp(textStamp);
		}
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateFileWithFooterName(originalFileName));
	}

	public DefaultStreamedContent addWatermark(UploadedFile uploadedFile, String watermarkText) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Document pdfDocument = new Document(input);

		FormattedText formattedText = new FormattedText(watermarkText, java.awt.Color.BLUE, FontStyle.TimesRoman,
				EncodingType.Identity_h, true, DEFAULT_WATERMARK_FONT_SIZE);
		WatermarkArtifact artifact = new WatermarkArtifact();
		artifact.setText(formattedText);
		artifact.setArtifactHorizontalAlignment(HorizontalAlignment.Center);
		artifact.setArtifactVerticalAlignment(VerticalAlignment.Center);
		artifact.setRotation(DEFAULT_WATERMARK_ROTATION);
		artifact.setOpacity(DEFAULT_WATERMARK_OPACITY);
		artifact.setBackground(false);

		for (Page page : pdfDocument.getPages()) {
			page.getArtifacts().add(artifact);
		}
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateFileNameWithWatermark(originalFileName));
	}

	public DefaultStreamedContent rotatePages(UploadedFile uploadedFile, int rotateOption) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Document pdfDocument = new Document(input);

		for (Page page : pdfDocument.getPages()) {
			page.setRotate(rotateOption);
		}
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateRotatedFileName(originalFileName));
	}

	public DefaultStreamedContent addPageNumbers(UploadedFile uploadedFile) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Document pdfDocument = new Document(input);

		PageNumberStamp pageNumberStamp = new PageNumberStamp();
		pageNumberStamp.setBackground(false);
		pageNumberStamp.setFormat("Page # of " + pdfDocument.getPages().size());
		pageNumberStamp.setBottomMargin(10);
		pageNumberStamp.setHorizontalAlignment(HorizontalAlignment.Center);
		pageNumberStamp.setStartingNumber(1);

		pageNumberStamp.getTextState().setFont(FontRepository.findFont(TIMES_NEW_ROMAN_FONT));
		pageNumberStamp.getTextState().setFontSize(DEFAULT_PAGE_NUMBER_FONT_SIZE);
		pageNumberStamp.getTextState().setFontStyle(FontStyles.Bold);
		pageNumberStamp.getTextState().setForegroundColor(Color.getBlack());

		for (Page page : pdfDocument.getPages()) {
			page.addStamp(pageNumberStamp);
		}
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateFileWithPageNumberName(originalFileName));
	}

	public DefaultStreamedContent extractHighlightedText(String originalFileName, InputStream input,
			ByteArrayOutputStream textStream, OutputStreamWriter writer, TextExtractType textExtractType)
			throws IOException {
		Document pdfDocument = new Document(input);
		StringBuilder highlightedText = new StringBuilder();

		for (Page page : pdfDocument.getPages()) {
			for (Annotation annotation : page.getAnnotations()) {
				if (annotation instanceof HighlightAnnotation) {
					HighlightAnnotation highlight = (HighlightAnnotation) annotation;

					TextFragmentCollection fragments = highlight.getMarkedTextFragments();
					for (TextFragment tf : fragments) {
						highlightedText.append(tf.getText()).append(System.lineSeparator());
					}
				}
			}
		}

		writer.write(highlightedText.toString());
		writer.flush();

		pdfDocument.close();

		return buildFileStream(textStream.toByteArray(), updateTxtFileName(originalFileName, textExtractType));
	}

	public DefaultStreamedContent extractAllText(String originalFileName, InputStream input,
			ByteArrayOutputStream textStream, OutputStreamWriter writer, TextExtractType textExtractType)
			throws IOException {
		Document pdfDocument = new Document(input);
		TextAbsorber textAbsorber = new TextAbsorber();

		pdfDocument.getPages().accept(textAbsorber);

		String extractedText = textAbsorber.getText();

		writer.write(extractedText);
		writer.flush();

		pdfDocument.close();

		return buildFileStream(textStream.toByteArray(), updateTxtFileName(originalFileName, textExtractType));
	}

	public DefaultStreamedContent merge(UploadedFiles uploadedFiles) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int uploadedFilesSize = uploadedFiles.getFiles().size();
		InputStream[] inputStreams = new InputStream[uploadedFilesSize];

		for (int i = 0; i < uploadedFilesSize; i++) {
			inputStreams[i] = uploadedFiles.getFiles().get(i).getInputStream();
		}

		PdfFileEditor editor = new PdfFileEditor();
		boolean result = editor.concatenate(inputStreams, output);
		if (!result) {
			return null;
		}
		return buildFileStream(output.toByteArray(), MERGED_DOCUMENT_NAME);
	}

	public DefaultStreamedContent convertHtmlToPdf(UploadedFile uploadedFile) throws IOException {
		InputStream input = uploadedFile.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String originalFileName = uploadedFile.getFileName();
		String fileName = originalFileName.toLowerCase();

		if (fileName.endsWith(FileExtension.HTML.getExtension())) {
			String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			Document pdfDoc = new Document();
			Page page = pdfDoc.getPages().add();
			TextFragment text = new TextFragment(html);
			text.getTextState().setFontSize(DEFAULT_FONT_SIZE);
			text.getTextState().setFont(FontRepository.findFont(TIMES_NEW_ROMAN_FONT));
			page.getParagraphs().add(text);
			pdfDoc.save(output);
			pdfDoc.close();
		} else if (fileName.endsWith(FileExtension.PDF.getExtension())) {
			Document pdfDoc = new Document(input);
			pdfDoc.save(output);
			pdfDoc.close();
		}

		return buildFileStream(output.toByteArray(), updateFileWithPdfExtension(originalFileName));
	}

	public DefaultStreamedContent convertImageToPdf(UploadedFile uploadedFile) throws IOException {
		String originalFileName = uploadedFile.getFileName();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(uploadedFile.getContent()));
		int widthPx = bufferedImage.getWidth();
		int heightPx = bufferedImage.getHeight();

		Document pdfDocument = new Document();
		Page page = pdfDocument.getPages().add();
		page.getPageInfo().setWidth(widthPx);
		page.getPageInfo().setHeight(heightPx);
		page.getPageInfo().setMargin(new MarginInfo(0, 0, 0, 0));

		Image image = new Image();
		image.setImageStream(uploadedFile.getInputStream());
		page.getParagraphs().add(image);
		pdfDocument.save(output);
		pdfDocument.close();

		return buildFileStream(output.toByteArray(), updateFileWithPdfExtension(originalFileName));
	}

	private DefaultStreamedContent buildFileStream(byte[] byteContent, String fileName) {
		return DefaultStreamedContent.builder().name(fileName).contentType(PDF_CONTENT_TYPE)
				.stream(() -> new ByteArrayInputStream(byteContent)).build();
	}

	private String getBaseName(String originalFileName, String substitudeName) {
		return StringUtils.isNotBlank(originalFileName) ? StringUtils.substringBeforeLast(originalFileName, DOT)
				: substitudeName;
	}

	private String updateFileWithPdfExtension(String originalFileName) {
		return getBaseName(originalFileName, "pdf") + FileExtension.PDF.getExtension();
	}

	private String updateFileWithZipExtension(String originalFileName) {
		return String.format(SPLIT_PAGE_ZIP_NAME_PATTERN, getBaseName(originalFileName, "zip"));
	}

	private String updateRangeSplitFileWithZipExtension(String originalFileName, int startPage, int endPage) {
		return String.format(RANGE_SPLIT_FILE_NAME_PATTERN, getBaseName(originalFileName, "split_zip"), startPage,
				endPage);
	}

	private String updateFileNameWithWatermark(String originalFileName) {
		return String.format(FILE_NAME_WITH_WATERMARK_PATTERN, getBaseName(originalFileName, "pdf_with_watermark"));
	}

	private String updateImageZipName(String originalFileName) {
		return String.format(IMAGE_ZIP_NAME_PATTERN, getBaseName(originalFileName, "images_zip"));
	}

	private String updateRotatedFileName(String originalFileName) {
		return String.format(ROTATED_DOCUMENT_NAME_PATTERN, getBaseName(originalFileName, "rotated"));
	}

	private String updateFileWithNewExtension(String originalFileName, FileExtension fileExtension) {
		return getBaseName(originalFileName, "converted") + fileExtension.getExtension();
	}

	private String updateFileWithPageNumberName(String originalFileName) {
		return String.format(DOCUMENT_WITH_PAGE_NUMBER_NAME_PATTERN, getBaseName(originalFileName, "numbered"));
	}

	private String updateFileWithHeaderName(String originalFileName) {
		return String.format(DOCUMENT_WITH_HEADER_NAME_PATTERN, getBaseName(originalFileName, "with_header"));
	}

	private String updateFileWithFooterName(String originalFileName) {
		return String.format(DOCUMENT_WITH_FOOTER_NAME_PATTERN, getBaseName(originalFileName, "with_footerer"));
	}

	private String updateTxtFileName(String originalFileName, TextExtractType textExtractType) {
		if (TextExtractType.ALL.equals(textExtractType)) {
			return String.format(TXT_FILE_NAME_PATTERN, getBaseName(originalFileName, EXTRACTED_TEXT), EXTRACTED_TEXT);
		}
		return String.format(TXT_FILE_NAME_PATTERN, getBaseName(originalFileName, EXTRACTED_HIGHLIGHTED_TEXT),
				EXTRACTED_HIGHLIGHTED_TEXT);
	}
}
