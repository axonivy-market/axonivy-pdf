package com.axonivy.utils.axonivypdf.demo.managedBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import com.aspose.pdf.Document;
import com.axonivy.utils.axonivypdf.AxonivyPdfException;
import com.axonivy.utils.axonivypdf.enums.FileExtension;
import com.axonivy.utils.axonivypdf.enums.RotateOption;
import com.axonivy.utils.axonivypdf.enums.SplitOption;
import com.axonivy.utils.axonivypdf.enums.TextExtractType;
import com.axonivy.utils.axonivypdf.service.PdfFactory;
import com.axonivy.utils.axonivypdf.service.PdfService;

@ManagedBean
@ViewScoped
public class PdfFactoryBean {
  private PdfService pdfService;
  private SplitOption splitOption = SplitOption.ALL;
  private TextExtractType textExtractType = TextExtractType.ALL;
  private RotateOption selectedRotateOption = RotateOption.ROTATE_90;
  private Integer startPage;
  private Integer endPage;
  private String headerText = "HEADER";
  private String footerText = "FOOTER";
  private String watermarkText = "ASPOSE_WATERMARK";
  private UploadedFile uploadedFile;
  private UploadedFiles uploadedFiles;
  private DefaultStreamedContent fileForDownload;
  private List<RotateOption> rotateOptions =
      Arrays.asList(RotateOption.ROTATE_90, RotateOption.ROTATE_180, RotateOption.ROTATE_270, RotateOption.ROTATE_360);
  private List<FileExtension> otherDocumentTypes =
      Arrays.asList(FileExtension.DOCX, FileExtension.XLSX, FileExtension.PPTX, FileExtension.JPG, FileExtension.JPEG);
  private FileExtension selectedFileExtension = FileExtension.DOCX;

  @PostConstruct
  public void init() {
    pdfService = new PdfService();
    PdfFactory.loadLicense();
  }

  public void onSplitOptionChange() {
    if (SplitOption.RANGE.equals(splitOption)) {
      initPageRange();
    } else {
      setStartPage(1);
      setEndPage(1);
    }
  }

  public void initPageRange() {
    if (uploadedFile == null) {
      return;
    }
    try (InputStream input = uploadedFile.getInputStream()) {
      Document pdfDocument = new Document(input);
      setStartPage(1);
      setEndPage(pdfDocument.getPages().size());
      pdfDocument.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addHeader() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.addHeader(uploadedFile, headerText));
  }

  public void addFooter() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.addFooter(uploadedFile, footerText));
  }

  public void addWatermark() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.addWatermark(uploadedFile, watermarkText));
  }

  public void rotatePages() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.rotatePages(uploadedFile, selectedRotateOption.getValue()));
  }

  public void addPageNumbers() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.addPageNumbers(uploadedFile));
  }

  public void extractTextFromPdf() {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    String originalFileName = uploadedFile.getFileName();

    try (InputStream input = uploadedFile.getInputStream();
        ByteArrayOutputStream textStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(textStream, StandardCharsets.UTF_8)) {
      if (TextExtractType.ALL.equals(textExtractType)) {
        DefaultStreamedContent result =
            pdfService.extractAllText(originalFileName, input, textStream, writer, textExtractType);
        setFileForDownload(result);
      } else {
        DefaultStreamedContent result =
            pdfService.extractHighlightedText(originalFileName, input, textStream, writer, textExtractType);
        setFileForDownload(result);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void extractImagesFromPdf() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.extractImagesFromPdf(uploadedFile));
  }

  public void convertPdfToOtherDocumentTypes() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.convertPdfToOtherDocumentTypes(uploadedFile, getSelectedFileExtension()));
  }

  public void splitAndDownloadZipPdf() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    String originalFileName = uploadedFile.getFileName();
    InputStream input = uploadedFile.getInputStream();
    Document pdfDocument = new Document(input);

    if (SplitOption.ALL.equals(splitOption)) {
      setFileForDownload(pdfService.handleSplitIntoSinglePages(pdfDocument, originalFileName));
    } else {
      setFileForDownload(pdfService.handleSplitByRange(pdfDocument, originalFileName, getStartPage(), getEndPage()));
    }
    pdfDocument.close();
  }

  public void convertImageToPdf() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.convertImageToPdf(uploadedFile));
  }

  public void merge() throws IOException {
    if (uploadedFiles == null || uploadedFiles.getFiles().isEmpty()) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }
    setFileForDownload(pdfService.merge(uploadedFiles));
  }

  public void convertHtmlToPdf() throws IOException {
    if (uploadedFile == null) {
      throw new AxonivyPdfException("No file uploaded. Please upload a workbook file first.");
    }

    setFileForDownload(pdfService.convertHtmlToPdf(uploadedFile));
  }

  public UploadedFile getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  public UploadedFiles getUploadedFiles() {
    return uploadedFiles;
  }

  public void setUploadedFiles(UploadedFiles uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }

  public SplitOption getSplitOption() {
    return splitOption;
  }

  public void setSplitOption(SplitOption splitOption) {
    this.splitOption = splitOption;
  }

  public Integer getStartPage() {
    return startPage;
  }

  public void setStartPage(Integer startPage) {
    this.startPage = startPage;
  }

  public Integer getEndPage() {
    return endPage;
  }

  public void setEndPage(Integer endPage) {
    this.endPage = endPage;
  }

  public DefaultStreamedContent getFileForDownload() {
    return fileForDownload;
  }

  public void setFileForDownload(DefaultStreamedContent fileForDownload) {
    this.fileForDownload = fileForDownload;
  }

  public List<FileExtension> getOtherDocumentTypes() {
    return otherDocumentTypes;
  }

  public void setOtherDocumentTypes(List<FileExtension> otherDocumentTypes) {
    this.otherDocumentTypes = otherDocumentTypes;
  }

  public FileExtension getSelectedFileExtension() {
    return selectedFileExtension;
  }

  public void setSelectedFileExtension(FileExtension selectedFileExtension) {
    this.selectedFileExtension = selectedFileExtension;
  }

  public TextExtractType getTextExtractType() {
    return textExtractType;
  }

  public void setTextExtractType(TextExtractType textExtractType) {
    this.textExtractType = textExtractType;
  }

  public String getHeaderText() {
    return headerText;
  }

  public void setHeaderText(String headerText) {
    this.headerText = headerText;
  }

  public String getFooterText() {
    return footerText;
  }

  public void setFooterText(String footerText) {
    this.footerText = footerText;
  }

  public String getWatermarkText() {
    return watermarkText;
  }

  public void setWatermarkText(String watermarkText) {
    this.watermarkText = watermarkText;
  }

  public RotateOption getSelectedRotateOption() {
    return selectedRotateOption;
  }

  public void setSelectedRotateOption(RotateOption selectedRotateOption) {
    this.selectedRotateOption = selectedRotateOption;
  }

  public List<RotateOption> getRotateOptions() {
    return rotateOptions;
  }

  public void setRotateOptions(List<RotateOption> rotateOptions) {
    this.rotateOptions = rotateOptions;
  }
}
