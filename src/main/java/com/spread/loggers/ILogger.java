package com.spread.loggers;

public interface ILogger {

	public static final int INFO = 1;
	public static final int DEBUG = 2;
	public static final int ERROR = 3;

	void log(String message, Integer level);
	
}
