package com.spread.model.storage;

public class StorageException extends RuntimeException {
	
 	private static final long serialVersionUID = -5672539256429927073L;

	public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
