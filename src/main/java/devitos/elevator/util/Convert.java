package devitos.elevator.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class Convert {

	final static Logger logger = Logger.getLogger(Convert.class);

	static public Date fromString(String date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate;
		try {
			startDate = df.parse(date);
			return startDate;
		} catch (ParseException e) {
			logger.error("Parse date error", e);
			return null;
		}
	}

	static public String fromDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

}
