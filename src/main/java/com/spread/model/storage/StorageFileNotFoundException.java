package com.spread.model.storage;

public class StorageFileNotFoundException extends StorageException {

	private static final long serialVersionUID = -805602644096626529L;

	public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}