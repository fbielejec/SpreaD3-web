package com.spread;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestUtils {

    public static final Set<String> expectedAttributes = new HashSet<String>(Arrays.asList("length_range", "trait2",
                                                                                           "trait.rate_range", "length_95%_HPD", "trait1", "trait1_range", "trait1_80%HPD_3", "height_median",
                                                                                           "trait1_80%HPD_2", "height_range", "trait1_80%HPD_1", "height_95%_HPD", "trait.rate_95%_HPD",
                                                                                           "trait2_80%HPD_9", "length_median", "trait_80%HPD_modality", "trait2_80%HPD_5", "trait2_80%HPD_6",
                                                                                           "trait2_80%HPD_7", "height", "trait2_80%HPD_8", "trait2_80%HPD_1", "trait2_80%HPD_2", "trait2_80%HPD_3",
                                                                                           "trait2_80%HPD_4", "length", "trait2_median", "posterior", "trait1_median", "trait2_range",
                                                                                           "trait.rate_median", "trait.rate", "trait1_80%HPD_7", "trait1_80%HPD_6", "trait1_80%HPD_5",
                                                                                           "trait1_80%HPD_4", "trait1_80%HPD_9", "trait1_80%HPD_8"));

    public static final Set<String> expectedHpdLevels = new HashSet<String>(Arrays.asList("80"));

    public static final String yCoordinate = "trait1";
    public static final String xCoordinate = "trait2";

    public static Set<String> jsonArrayToSet(String json) {
        Type setType = new TypeToken<Set<String>>(){}.getType();
        Set<String> set = new Gson().fromJson(json, setType);
        return set;
    }

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
