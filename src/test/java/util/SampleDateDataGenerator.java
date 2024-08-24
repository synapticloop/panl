package util;

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

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * <p>Generate sample date data from 10 years ago, adding a random number of
 * days between each addition for 1,000 values.</p>
 *
 * @author synapticloop
 */
public class SampleDateDataGenerator {
	private static final SimpleDateFormat SDF_ID = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat SDF_TEXT = new SimpleDateFormat("EEEE d MMMM yyyy");
	private static final SimpleDateFormat SDF_SOLR = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	private static final SimpleDateFormat SDF_YEAR = new SimpleDateFormat("yyyy");
	private static final SimpleDateFormat SDF_MONTH = new SimpleDateFormat("MMMM");
	private static final SimpleDateFormat SDF_DAY = new SimpleDateFormat("dd");
	private static final SimpleDateFormat SDF_DAY_OF_WEEK = new SimpleDateFormat("EEEE");

	private static final Random RANDOM = new Random(System.currentTimeMillis());

	public static void main(String[] args) {
		JSONArray jsonArray = new JSONArray();

		// no go through the dates
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);

		// we are to add about 1000
		for(int i = 0; i < 1000; i++) {
			JSONObject jsonObject = new JSONObject();
			Date date = calendar.getTime();
			jsonObject.put("id", SDF_ID.format(date));
			jsonObject.put("solr_date", SDF_SOLR.format(date));
			jsonObject.put("text_date", SDF_TEXT.format(date));
			int year = Integer.parseInt(SDF_YEAR.format(date));
			int decade = year - year % 10;
			jsonObject.put("year", year);
			jsonObject.put("decade", decade);
			jsonObject.put("month", SDF_MONTH.format(date));
			jsonObject.put("day", Integer.parseInt(SDF_DAY.format(date)));
			jsonObject.put("day_of_week", SDF_DAY_OF_WEEK.format(date));
			jsonArray.put(jsonObject);
			calendar.add(Calendar.DAY_OF_MONTH, 3 + RANDOM.nextInt(6) +1);
		}

		try {
			FileUtils.writeStringToFile(
					new File("src/dist/sample/data/simple-date.json"),
					jsonArray.toString(2),
					Charset.defaultCharset(), false);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
