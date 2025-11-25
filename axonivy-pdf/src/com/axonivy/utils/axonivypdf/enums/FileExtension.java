package com.axonivy.utils.axonivypdf.enums;

import java.util.Arrays;
import java.util.List;

public enum FileExtension {
  DOC(".doc"), DOCX(".docx"), ODT(".odt"), TXT(".txt"), MD(".md"), XLS(".xls"), XLSX(".xlsx"), HTML(".html"), PDF(".pdf"),
  ZIP(".zip"), PPTX(".pptx"), JPG(".jpg"), JPEG(".jpeg"), PNG(".png");

  private final String extension;

  private FileExtension(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  public static List<FileExtension> getOtherDocumentTypes() {
    return Arrays.asList(DOC, DOCX, XLSX, PPTX, JPG, JPEG, PNG);
  }
}
