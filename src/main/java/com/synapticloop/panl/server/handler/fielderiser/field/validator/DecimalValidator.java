package com.synapticloop.panl.server.handler.fielderiser.field.validator;

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


import com.synapticloop.panl.server.handler.properties.PanlProperties;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class DecimalValidator {
	private static final NumberFormat DECIMAL_FORMAT_POINT = NumberFormat.getInstance(Locale.UK);
	private static final NumberFormat DECIMAL_FORMAT_COMMA = NumberFormat.getInstance(Locale.FRANCE);

	public static String validate(String value) {
		String replaced = value.replaceAll("[^0-9.,]", "");
		if (replaced.isBlank()) {
			return (null);
		} else {
			try {
				if(PanlProperties.getIsDecimalPoint()) {
					DECIMAL_FORMAT_POINT.parse(replaced);
				} else {
					DECIMAL_FORMAT_COMMA.parse(replaced);
				}
			} catch(NumberFormatException | ParseException ex) {
				return(null);
			}
			return replaced;
		}
	}
}
