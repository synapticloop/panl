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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class LiveRandomURITester {
	private static final String DEFAULT_URL = "http://localhost:8181/mechanical-pencils/brandandname/";
	private static final String AVAILABLE = "ab+cdef/g-hij/kl+mn[opq/rs]tuv-wx/yz+AB[CDEFG/HI-J]KLM/N+O[PQR/S/TU]V-WX/Y+Z[0123/45]67-89/+-";
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static String getRandomURI() {
		// length
		int uriLength = RANDOM.nextInt(400);

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < uriLength; i++) {
			sb.append(AVAILABLE.charAt(RANDOM.nextInt(AVAILABLE.length())));
		}

		return(DEFAULT_URL + sb.toString());
	}



	public static void main(String[] args) {
		for(int i = 0; i <= 10000; i++) {
			URL url = null;
			try {
				if(i % 1000 == 0) {
					System.out.println();
				}

				if(i % 100 == 0) {
					System.out.print(i + " ");
				}

				String randomURI = getRandomURI();
				url = new URL(randomURI);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				int status = con.getResponseCode();
				if(status != 200) {
					System.out.println("\n" + status + " // " +  randomURI);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
