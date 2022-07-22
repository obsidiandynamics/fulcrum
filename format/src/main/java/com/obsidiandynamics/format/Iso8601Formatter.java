package com.obsidiandynamics.format;

import java.util.*;

/**
 *  Defines the parsing and formatting behaviour to be used by {@link Iso8601}.
 */
public interface Iso8601Formatter {
  /**
   *  Parses a given ISO 8601 encoded string, producing a {@link Date}.
   *  
   *  @param encoded The encoded string.
   *  @return A {@link Date} object.
   *  @throws Throwable If a parsing error occurs.
   */
  Date parse(String encoded) throws Throwable;
  
  /**
   *  Formats a given date to ISO 8601 form, using the provided time zone.
   *  
   *  @param date The date to format.
   *  @param timeZone The time zone to use.
   *  @return The formatted {@link String}.
   */
  String format(Date date, TimeZone timeZone);
}
