package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FileInfoListTest {

	private static void assertDateValid(String date) {
		assertDoesNotThrow(() -> FileInfoList.checkDate(date), "\"" + date + "\" must be valid");
	}

	private static void assertDateInvalid(String date) {
		assertThrows(IllegalArgumentException.class, () -> FileInfoList.checkDate(date),
				"\"" + date + "\" must be invalid");
	}

	@Test
	public void checkDateAcceptsTheDocumentedPatterns() {
		assertDateValid(null);
		assertDateValid("");
		assertDateValid("01/01/2000");    // DD/MM/YYYY
		assertDateValid("15/06/1995");
		assertDateValid("31/12/1999");
		assertDateValid("29/02/2001");    // Leap years are deliberately ignored
		assertDateValid("31/12/1999 <?>");
		assertDateValid("01-05/03/1999"); // DD-DD/MM/YYYY
		assertDateValid("06/1999");       // MM/YYYY
		assertDateValid("03-07/1999");    // MM-MM/YYYY
		assertDateValid("1985");          // YYYY
		assertDateValid("1985-1987");     // YYYY-YYYY
	}

	@Test
	public void checkDateRejectsInvalidDays() {
		assertDateInvalid("00/01/2000");
		assertDateInvalid("32/01/2000");
		assertDateInvalid("30/02/2000");
		assertDateInvalid("31/02/2000");
		assertDateInvalid("31/04/2000");
		assertDateInvalid("31/06/1999"); // June has 30 days
		assertDateInvalid("31/09/1999");
		assertDateInvalid("31/11/1999");
	}

	@Test
	public void checkDateAcceptsDay31In31DayMonths() {
		assertDateValid("31/01/2000");
		assertDateValid("31/03/2000");
		assertDateValid("31/05/2000");
		assertDateValid("31/07/2000");
		assertDateValid("31/08/2000");
		assertDateValid("31/10/2000");
		assertDateValid("31/12/2000");
	}

	@Test
	public void checkDateRejectsInvalidMonthsAndFutureYears() {
		assertDateInvalid("15/13/2000");
		assertDateInvalid("15/00/2000");
		assertDateInvalid("01/01/2990");
		assertDateInvalid("13/2000"); // MM/YYYY with invalid month
		assertDateInvalid("2990");    // YYYY in the future
	}

	@Test
	public void checkDateRejectsInvertedRanges() {
		assertDateInvalid("05-03/06/1999"); // day-from after day-to
		assertDateInvalid("09-03/1999");    // month-from after month-to
		assertDateInvalid("1999-1995");     // year-from after year-to
	}

	@Test
	public void checkDateAcceptsTheExamplesOfTheSAPSpecification() {
		// https://asap.sourceforge.net/sap-format.html, DATE tag.
		assertDateValid("1986");
		assertDateValid("1993-1994");
		assertDateValid("28/08/1997");
		assertDateValid("12/2001");
		assertDateValid("23-26/07/2010");
		assertDateValid("199?"); // Some unknown year in the 1990s.
	}

	@Test
	public void checkDateAcceptsUnknownDigits() {
		assertDateValid("19??");
		assertDateValid("199? <?>");
		assertDateValid("1993-199?");
		assertDateValid("??/08/1997");
		assertDateValid("2?/02/1997");
		assertDateValid("3?/01/1997"); // 30 and 31 exist in January.
		assertDateValid("1?/2001");
		assertDateValid("??/??/19??");
		assertDateValid("0?-2?/07/2010"); // The order is unknown, so not checked.
	}

	@Test
	public void checkDateRejectsImpossibleUnknownDigits() {
		assertDateInvalid("3?/02/1997"); // February has no day 30-39.
		assertDateInvalid("4?/01/1997");
		assertDateInvalid("2?/2001"); // No month 20-29.
		assertDateInvalid("29??"); // Every reading is in the future.
		assertDateInvalid("1999-1995"); // Fully known ranges are still ordered.
	}

	@Test
	public void checkDateRejectsUnknownPatterns() {
		assertDateInvalid("1.1.2000");
		assertDateInvalid("January 2000");
	}
}
