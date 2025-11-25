package com.axonivy.utils.axonivypdf.enums;

import com.aspose.pdf.Rotation;

public enum RotateOption {
  ROTATE_90("90°", Rotation.on90), ROTATE_180("180°", Rotation.on180), ROTATE_270("270°",
      Rotation.on270), ROTATE_360("360°", Rotation.on360);

  private final String label;
  private final int value;

  RotateOption(String label, int value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return name();
  }
}
