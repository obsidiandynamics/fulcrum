package com.obsidiandynamics.format;

import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 *  The {@link OffsetDateTimeFormatter} allows for parsing ISO 8601 timestamps using
 *  the more contemporary, and thread-safe {@link DateTimeFormatter}.
 */
public final class OffsetDateTimeFormatter implements Iso8601Formatter {
  private final DateTimeFormatter formatter;
  
  public OffsetDateTimeFormatter(DateTimeFormatter formatter) {
    this.formatter = formatter;
  }
  
  @Override
  public Date parse(String encoded) {
    final OffsetDateTime offsetDateTime = OffsetDateTime.parse(encoded, formatter);
    return new Date(offsetDateTime.toInstant().toEpochMilli());
  }

  @Override
  public String format(Date date, TimeZone timeZone) {
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), 
                                                                   ZoneId.of(timeZone.getID()));
    return offsetDateTime.format(formatter);
  }
}
