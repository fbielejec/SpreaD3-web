package com.spread.controller;

public class ControllerException extends Exception {
	private static final long serialVersionUID = -1258021836219278875L;
	private final String message;

	public ControllerException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
