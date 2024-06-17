package live;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class RandomURITester {
	private static final String DEFAULT_URL = "http://localhost:8181/mechanical-pencils/brandandname/";
	private static final String AVAILABLE = "ab+cdef/g-hij/kl+mn[opq/rs]tuv-wx/yz+AB[CDEFG/HI-J]KLM/N+O[PQR/S/TU]V-WX/Y+Z[0123/45]67-89/+-";
	private static Random RANDOM = new Random(System.currentTimeMillis());
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
