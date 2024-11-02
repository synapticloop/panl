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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class PanlGeneratorTest {
	@Test public void testGenerate() throws PanlGenerateException {
//		PanlGenerator panlGeneratorMock = Mockito.mock(
		PanlGenerator spy = Mockito.spy(new PanlGenerator(
			"./src/test/resources/sample/panl.properties",
			"./src/test/resources/sample/managed-schema.xml",
			true));
		InputStream stdin = System.in;
		Mockito.when(spy.getSystemInput()).thenReturn(new Scanner(new ByteArrayInputStream("\n\n\n\n\n\n".getBytes())));
		spy.generate();
	}
}
