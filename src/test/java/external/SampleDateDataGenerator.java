package external;

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
 * <p>Generate sample date data from 10 years ago, one date for every 10 days,
 * and 800 entries.</p>
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

		// we are to add about 800
		for(int i = 0; i < 1000; i++) {
			JSONObject jsonObject = new JSONObject();
			Date date = calendar.getTime();
			jsonObject.put("id", SDF_ID.format(date));
			jsonObject.put("solr_date", SDF_SOLR.format(date));
			jsonObject.put("text_date", SDF_TEXT.format(date));
			jsonObject.put("year", Integer.parseInt(SDF_YEAR.format(date)));
			jsonObject.put("month", SDF_MONTH.format(date));
			jsonObject.put("day", Integer.parseInt(SDF_DAY.format(date)));
			jsonObject.put("day_of_week", SDF_DAY_OF_WEEK.format(date));
			jsonArray.put(jsonObject);
			calendar.add(Calendar.DAY_OF_MONTH, 3 + RANDOM.nextInt(6) +1);
		}

		try {
			FileUtils.writeStringToFile(
					new File("src/dist/data/simple-date.json"),
					jsonArray.toString(2),
					Charset.defaultCharset(), false);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}