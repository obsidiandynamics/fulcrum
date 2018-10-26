package com.obsidiandynamics.format;

import java.text.*;
import java.time.format.*;
import java.util.*;

import com.obsidiandynamics.func.*;

/**
 *  Helpers for parsing and formatting ISO 8601 types, converting to and from {@link java.util.Date}. <p>
 *  
 *  By default, {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME} is used to parse ISO 8601
 *  dates/times in a broad variety of formatting arrangements. <p>
 *  
 *  Alternate formats can be recognised by passing a custom {@link Iso8601Formatter} instance
 *  to the {@link #parse(Iso8601Formatter, String)} and {@link #format(Iso8601Formatter, Date, TimeZone)}
 *  methods. Use {@link LegacyDateTimeFormatter} for working with the legacy {@link SimpleDateFormat}, or
 *  {@link OffsetDateTimeFormatter} for the more contemporary {@link DateTimeFormatter}.
 */
public final class Iso8601 {
  /** The default time-aware output representation using the 'extended' format (with colons 
   *  separating hours, minutes and seconds) with a period-delimited fraction of a second to 
   *  millisecond precision. Both UTC ('Z') and GMT offsets are supported. */
  public static final String DATE_TIME_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  
  public static final Iso8601Formatter DEFAULT_FORMATTER = new OffsetDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  
  private Iso8601() {}
  
  /**
   *  Thrown if a supposedly ISO 8601 encoded timestamp could not be parsed.
   */
  public static final class Iso8601ParseException extends Exception {
    private static final long serialVersionUID = 1L;

    Iso8601ParseException(Throwable cause) { super(cause); }
  }
  
  /**
   *  An unchecked variant of an {@link Iso8601ParseException}.
   */
  public static final class RuntimeIso8601ParseException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    
    RuntimeIso8601ParseException(Throwable cause) { super(cause); }
  }
  
  /**
   *  Parses an ISO 8601 encoded date/time using the default formatter.
   *  
   *  @param encoded The ISO 8601 encoded date/time.
   *  @return The parsed {@link Date}.
   *  @throws Iso8601ParseException If parsing failed with an error.
   */
  public static Date parse(String encoded) throws Iso8601ParseException {
    return parse(DEFAULT_FORMATTER, encoded);
  }
  
  /**
   *  Parses an ISO 8601 encoded date/time using a given formatter.
   *  
   *  @param formatter The formatter to use.
   *  @param encoded The ISO 8601 encoded date/time.
   *  @return The parsed {@link Date}.
   *  @throws Iso8601ParseException If parsing failed with an error.
   */
  public static Date parse(Iso8601Formatter formatter, String encoded) throws Iso8601ParseException {
    return Exceptions.wrap(() -> formatter.parse(encoded), Iso8601ParseException::new);
  }
  
  /**
   *  An unchecked variant of {@link #parse(String)}, throwing a {@link RuntimeIso8601ParseException} in the
   *  event of a parse error.
   *  
   *  @param encoded The ISO 8601 encoded date/time.
   *  @return The parsed {@link Date}.
   */
  public static Date parseUnchecked(String encoded) {
    return parseUnchecked(DEFAULT_FORMATTER, encoded);
  }
  
  /**
   *  An unchecked variant of {@link #parse(Iso8601Formatter, String)} throwing a 
   *  {@link RuntimeIso8601ParseException} in the event of a parse error.
   *  
   *  @param formatter The formatter to use.
   *  @param encoded The ISO 8601 encoded date/time.
   *  @return The parsed {@link Date}.
   */
  public static Date parseUnchecked(Iso8601Formatter formatter, String encoded) {
    return Exceptions.wrap(() -> formatter.parse(encoded), RuntimeIso8601ParseException::new);
  }
  
  /**
   *  Formats a given {@link Date} object in ISO 8601 using the default formatter, targeting the 
   *  default time zone.
   *  
   *  @param date The date to format.
   *  @return A formatted ISO 8601 string.
   */
  public static String format(Date date) {
    return format(date, TimeZone.getDefault());
  }
  
  /**
   *  Formats a given {@link Date} object in ISO 8601 using the default formatter, targeting the 
   *  specified {@link TimeZone}.
   *  
   *  @param date The date to format.
   *  @param timeZone The target time zone.
   *  @return A formatted ISO 8601 string.
   */
  public static String format(Date date, TimeZone timeZone) {
    return format(DEFAULT_FORMATTER, date, timeZone);
  }
  
  /**
   *  Formats a given {@link Date} object in ISO 8601 using a given formatter, targeting the 
   *  specified {@link TimeZone}.
   *  
   *  @param formatter The formatter to use.
   *  @param date The date to format.
   *  @param timeZone The target time zone.
   *  @return A formatted ISO 8601 string.
   */
  public static String format(Iso8601Formatter formatter, Date date, TimeZone timeZone) {
    return formatter.format(date, timeZone);
  }
}
