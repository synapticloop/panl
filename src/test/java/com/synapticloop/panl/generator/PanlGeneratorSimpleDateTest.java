package com.synapticloop.panl.generator;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */


import com.synapticloop.panl.exception.PanlGenerateException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PanlGeneratorSimpleDateTest {
	public static final String GENERATED_PANL_PROPERTIES = "./src/test/resources/sample/simple-date/panl.properties";
	public static final String ORIGINAL_PANL_PROPERTIES = "./src/test/resources/sample/simple-date/panl.properties.original";
	public static final String GENERATED_COLLECTION_PANL_PROPERTIES = "./src/test/resources/sample/simple-date/simple-date.panl.properties";
	public static final String ORIGINAL_COLLECTION_PANL_PROPERTIES = "./src/test/resources/sample/simple-date/simple-date.panl.properties.original";

	@Test public void testGenerate() throws PanlGenerateException, IOException {
		PanlGenerator spy = Mockito.spy(new PanlGenerator(
			GENERATED_PANL_PROPERTIES,
			"./src/test/resources/sample/simple-date/managed-schema.xml",
			true));

		// mock the input from the command line
		Mockito.when(spy.getSystemInput()).thenReturn(new Scanner(new ByteArrayInputStream("\n\n\n\n\n\n".getBytes())));
		spy.generate();

		// now check the two files - simple sha256 should do the trick

		assertFilesSame(ORIGINAL_PANL_PROPERTIES, GENERATED_PANL_PROPERTIES);
		assertFilesSame(ORIGINAL_COLLECTION_PANL_PROPERTIES, GENERATED_COLLECTION_PANL_PROPERTIES);
	}

	/**
	 * <p>This ignores line endings</p>
	 *
	 * @param original The original file location
	 * @param generated the generated file location
	 *
	 * @throws IOException If there was an error reading the file
	 */
	private void assertFilesSame(String original, String generated) throws IOException {
		File originalFile = new File(original);
		String originalFileData = FileUtils.readFileToString(originalFile, StandardCharsets.UTF_8)
		                                   .replaceAll("(\r\n)", "\n");
		File generatedFile = new File(generated);
		String generatedFileData = FileUtils.readFileToString(generatedFile, StandardCharsets.UTF_8)
		                                    .replaceAll("(\r\n)", "\n");
		String[] originalSplit = originalFileData.split("\n");
		String[] generatedSplit = generatedFileData.split("\n");
		if(originalSplit.length != generatedSplit.length) {
			fail("Original file '" +
					originalFile.getAbsolutePath() +
					"' does not have the same number of lines as the generated file '" +
					generatedFile.getAbsolutePath() +
					"'.");
			fail();
		}

		int i = 0;
		for(String line: originalSplit) {
			// we do the line trim as EOL encoding may be different depending on the
			// operating system
			assertEquals(line.trim(), generatedSplit[i].trim());
			i++;
		}
	}
}
