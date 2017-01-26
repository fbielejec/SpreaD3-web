package com.spread.utils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TestUtils {

	public static final String attributes = "[\"length_range\",\"trait.rate_range\",\"trait2\",\"length_95%_HPD\",\"trait1\",\"trait1_range\",\"trait1_80%HPD_3\",\"height_median\",\"trait1_80%HPD_2\",\"height_range\",\"trait1_80%HPD_1\",\"height_95%_HPD\",\"trait.rate_95%_HPD\",\"trait2_80%HPD_9\",\"length_median\",\"trait2_80%HPD_5\",\"trait_80%HPD_modality\",\"trait2_80%HPD_6\",\"trait2_80%HPD_7\",\"height\",\"trait2_80%HPD_8\",\"trait2_80%HPD_1\",\"trait2_80%HPD_2\",\"trait2_80%HPD_3\",\"trait2_80%HPD_4\",\"length\",\"trait2_median\",\"posterior\",\"trait1_median\",\"trait2_range\",\"trait.rate_median\",\"trait.rate\",\"trait1_80%HPD_7\",\"trait1_80%HPD_6\",\"trait1_80%HPD_5\",\"trait1_80%HPD_4\",\"trait1_80%HPD_9\",\"trait1_80%HPD_8\"]";

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
