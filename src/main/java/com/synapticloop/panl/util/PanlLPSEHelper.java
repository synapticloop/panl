/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file was edited and updated by synapticlopp
 */
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

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Utility class for LPSE path helping.</p>
 *
 * TODO - this can probably be quicker if it was done as a lookup on a set
 * TODO - perhaps
 *
 * @author synapticloop
 * @author original authors
 */
public class PanlLPSEHelper {
	private static final Set<Integer> SUB_DELIMITER_SET = new HashSet<>();
	private static final Set<Integer> GENERIC_DELIMITER_SET = new HashSet<>();
	static {
		char[] subDelimiterCharacters = { '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' };
		char[] genericDelimiterCharacters = { ':', '/', '?', '#', '[', ']', '@' };

		for (char c : subDelimiterCharacters) {
			SUB_DELIMITER_SET.add((int)c);
		}

		for (char c : genericDelimiterCharacters) {
			GENERIC_DELIMITER_SET.add((int)c);
		}

	}

	/**
	 * <p>Encode a query Parameter</p>
	 * @param parameter The parameter to encode
	 *
	 * @return The encoded parameter in UTF-8
	 */
	public static String encodeURLParameter(String parameter) {
		return(URLEncoder.encode(parameter, StandardCharsets.UTF_8));
	}

	/**
	 * <p>Encode the given source into an encoded String using the rules specified
	 * by the given component and with the given options.</p>
	 *
	 * @param source the source String
	 * @return the encoded URI
	 *
	 * @throws IllegalArgumentException when the given value is not a valid URI component
	 */
	public static String encodeURIPath(String source) {
		if(source == null || source.isEmpty()) {
			return(source);
		}

		byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
		boolean original = true;
		for (byte b : bytes) {
			if (!isAllowed(b)) {
				original = false;
				break;
			}
		}
		if (original) {
			return source;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		for (byte b : bytes) {
			if (isAllowed(b)) {
				baos.write(b);
			}
			else {
				baos.write('%');
				char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
				char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
				baos.write(hex1);
				baos.write(hex2);
			}
		}
		return(baos.toString(StandardCharsets.UTF_8));
	}

	public static boolean isAllowed(int c) {
		return isPchar(c) || '/' == c;
	}

	protected static boolean isAlpha(int c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}

	/**
	 * Indicates whether the given character is in the {@code DIGIT} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isDigit(int c) {
		return (c >= '0' && c <= '9');
	}

	/**
	 * Indicates whether the given character is in the {@code gen-delims} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isGenericDelimiter(int c) {
		return(GENERIC_DELIMITER_SET.contains(c));
	}

	/**
	 * Indicates whether the given character is in the {@code sub-delims} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isSubDelimiter(int c) {
		return(SUB_DELIMITER_SET.contains(c));
	}

	/**
	 * Indicates whether the given character is in the {@code reserved} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isReserved(int c) {
		return (isGenericDelimiter(c) || isSubDelimiter(c));
	}

	/**
	 * Indicates whether the given character is in the {@code unreserved} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isUnreserved(int c) {
		return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
	}

	/**
	 * Indicates whether the given character is in the {@code pchar} set.
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	protected static boolean isPchar(int c) {
		return (isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c);
	}

}
