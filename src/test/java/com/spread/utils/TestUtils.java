package com.spread.utils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TestUtils {

	public static String getFileContent(File file) {
		StringBuilder result = new StringBuilder("");
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

}
