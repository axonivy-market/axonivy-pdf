package com.axonivy.utils.axonivypdf.enums;

public enum SplitOption {
	ALL("all"), RANGE("range");

	private final String option;

	private SplitOption(String option) {
		this.option = option;
	}

	public String getOption() {
		return option;
	}
}
