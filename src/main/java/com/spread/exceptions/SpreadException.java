package com.spread.exceptions;

public class SpreadException extends Exception {
	
	private static final long serialVersionUID = -1258021836219278875L;
	private final String message;

	public SpreadException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
