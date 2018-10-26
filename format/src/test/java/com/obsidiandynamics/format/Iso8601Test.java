package com.obsidiandynamics.format;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.format.Iso8601.*;

public final class Iso8601Test {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Iso8601.class);
  }

  private static final String TEST_DATE_Z = "2047-01-02T03:04:05.678Z";

  private static final String TEST_DATE_GMT_PLUS1000 = "2047-01-02T13:04:05.678+10:00";

  private static final String TEST_DATE_GMT_PLUS1030 = "2047-01-02T13:34:05.678+10:30";

  private static final String TEST_DATE_GMT_MINUS0200 = "2047-01-02T01:04:05.678-02:00";
  
  private static final LegacyDateTimeFormatter LEGACY_MILLIS = new LegacyDateTimeFormatter(Iso8601.DATE_TIME_MILLIS_FORMAT);
  
  private static final LegacyDateTimeFormatter LEGACY_SECONDS = new LegacyDateTimeFormatter("yyyy-MM-dd'T'HH:mm:ssX");
  
  private static Date buildDate(int year, int month, int date, int hourOfDay, int minute,
                                int second, int millis, String timeZone) {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone(timeZone));
    cal.set(year, month, date, hourOfDay, minute, second);
    cal.set(Calendar.MILLISECOND, millis);
    return cal.getTime();
  }

  private static Date getTestDateZ() {
    return buildDate(2047, 0, 2, 3, 4, 5, 678, "UTC");
  }
  
  private static Date getTestDateGmtPlus1000() {
    return buildDate(2047, 0, 2, 13, 4, 5, 678, "GMT+10");
  }
  
  private static Date getTestDateGmtPlus1030() {
    return buildDate(2047, 0, 2, 13, 34, 5, 678, "GMT+10:30");
  }
  
  private static Date getTestDateGmtMinus2() {
    return buildDate(2047, 0, 2, 1, 4, 5, 678, "GMT-2");
  }

  @Test
  public void testParseNewZ() throws Iso8601ParseException {
    final Date date = Iso8601.parse(TEST_DATE_Z);
    assertEquals(getTestDateZ(), date);
  }

  @Test
  public void testParseLegacyZ() throws Iso8601ParseException {
    final Date date = Iso8601.parse(LEGACY_MILLIS, TEST_DATE_Z);
    assertEquals(getTestDateZ(), date);
  }

  @Test
  public void testParseNewZMinutes() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 0, 0, "UTC"), date);
  }

  @Test
  public void testParseNewZSeconds() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04:05Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 0, "UTC"), date);
  }

  @Test
  public void testParseLegacyZSeconds() throws Iso8601ParseException {
    final Date date = Iso8601.parse(LEGACY_SECONDS, "2047-01-02T03:04:05Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 0, "UTC"), date);
  }

  @Test
  public void testParseNewZMicros() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04:05.123456Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 123, "UTC"), date);
  }

  @Test
  public void testParseNewZTicks() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04:05.1234567Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 123, "UTC"), date);
  }

  @Test
  public void testParseNewZNanos() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04:05.123456789Z");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 123, "UTC"), date);
  }

  @Test
  public void testParseNewZSecondsGmt0() throws Iso8601ParseException {
    final Date date = Iso8601.parse("2047-01-02T03:04:05+00:00");
    assertEquals(buildDate(2047, 0, 2, 3, 4, 5, 0, "UTC"), date);
  }

  @Test
  public void testParseNewZWithExplicitFormatter() throws Iso8601ParseException {
    final Date date = Iso8601.parse(Iso8601.DEFAULT_FORMATTER, TEST_DATE_Z);
    assertEquals(getTestDateZ(), date);
  }

  @Test
  public void testParseNewGmtPlus1000() throws Iso8601ParseException {
    final Date date = Iso8601.parse(TEST_DATE_GMT_PLUS1000);
    assertEquals(getTestDateGmtPlus1000(), date);
  }

  @Test
  public void testParseNewGmtPlus1030() throws Iso8601ParseException {
    final Date date = Iso8601.parse(TEST_DATE_GMT_PLUS1030);
    assertEquals(getTestDateGmtPlus1030(), date);
  }

  @Test
  public void testParseNewGmtMinus02() throws Iso8601ParseException {
    final Date date = Iso8601.parse(TEST_DATE_GMT_MINUS0200);
    assertEquals(getTestDateGmtMinus2(), date);
  }

  @Test
  public void testParseUncheckedGmtMinus02() {
    final Date date = Iso8601.parseUnchecked(TEST_DATE_GMT_MINUS0200);
    assertEquals(getTestDateGmtMinus2(), date);
  }

  @Test
  public void testParseNewUncheckedGmtMinus02WithExplicitFormatter() {
    final Date date = Iso8601.parseUnchecked(Iso8601.DEFAULT_FORMATTER, TEST_DATE_GMT_MINUS0200);
    assertEquals(getTestDateGmtMinus2(), date);
  }

  @Test
  public void testFormatNewZ() {
    final String dateStr = Iso8601.format(getTestDateZ(), TimeZone.getTimeZone("UTC"));
    assertEquals(TEST_DATE_Z, dateStr);
  }

  @Test
  public void testFormatNewZGmtPlus1000() {
    final String dateStr = Iso8601.format(getTestDateZ(), TimeZone.getTimeZone("GMT+10"));
    assertEquals(TEST_DATE_GMT_PLUS1000, dateStr);
  }

  @Test
  public void testFormatNewZGmtPlus1030() {
    final String dateStr = Iso8601.format(getTestDateZ(), TimeZone.getTimeZone("GMT+10:30"));
    assertEquals(TEST_DATE_GMT_PLUS1030, dateStr);
  }

  @Test
  public void testFormatLegacyZ() {
    final String dateStr = Iso8601.format(LEGACY_MILLIS, getTestDateZ(), TimeZone.getTimeZone("UTC"));
    assertEquals(TEST_DATE_Z, dateStr);
  }

  @Test
  public void testFormatNewZSeconds() {
    final String dateStr = Iso8601.format(buildDate(2047, 0, 2, 3, 4, 5, 0, "UTC"), TimeZone.getTimeZone("UTC"));
    assertEquals("2047-01-02T03:04:05Z", dateStr);
  }

  @Test
  public void testFormatLegacyZSeconds() {
    final String dateStr = Iso8601.format(LEGACY_SECONDS, buildDate(2047, 0, 2, 3, 4, 5, 0, "UTC"), TimeZone.getTimeZone("UTC"));
    assertEquals("2047-01-02T03:04:05Z", dateStr);
  }

  @Test
  public void testFormatNewZMinutes() {
    final String dateStr = Iso8601.format(buildDate(2047, 0, 2, 3, 4, 0, 0, "UTC"), TimeZone.getTimeZone("UTC"));
    assertEquals("2047-01-02T03:04:00Z", dateStr);
  }

  @Test
  public void testFormatParseNewLocalTimeZone() throws Iso8601ParseException {
    final Date testDate = getTestDateZ();
    final String encoded = Iso8601.format(testDate);
    final Date decoded = Iso8601.parse(encoded);
    assertEquals(testDate, decoded);
  }

  @Test
  public void testParseNewWithExceptionParseError() throws Iso8601ParseException {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      Iso8601.parse("2047-01-02T01:04:05_678-02");
    })
    .isInstanceOf(Iso8601ParseException.class);
  }

  @Test
  public void testParseUncheckedNewWithParseError() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      Iso8601.parseUnchecked("2047-01-02T01:04:05_678-02");
    })
    .isInstanceOf(RuntimeIso8601ParseException.class);
  }
}
