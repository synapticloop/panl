package com.synapticloop.panl.util;

/*
 * Copyright (c) 2008-2025 synapticloop.
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PanlLPSEHelperTest {
	@Test public void testIsSubDelimiter() {
		char[] characters = { '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' };

		for (int character : characters) {
			assertTrue(PanlLPSEHelper.isSubDelimiter(character));
		}
	}

	@Test public void testIsGenericDelimiter() {
		char[] genericDelimiterCharacters = { ':', '/', '?', '#', '[', ']', '@' };

		for (int character : genericDelimiterCharacters) {
			assertTrue(PanlLPSEHelper.isGenericDelimiter(character));
		}
	}

	@Test public void IsNotGenericSubDelimiter() {
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		for (int i = 0; i < characters.length(); i++) {
			assertFalse(PanlLPSEHelper.isSubDelimiter(characters.charAt(i)));
			assertFalse(PanlLPSEHelper.isGenericDelimiter(characters.charAt(i)));
		}
	}
}
