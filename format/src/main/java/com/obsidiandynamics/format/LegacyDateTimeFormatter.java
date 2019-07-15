package com.obsidiandynamics.format;

import java.text.*;
import java.util.*;

import com.obsidiandynamics.threads.*;

/**
 *  The {@link LegacyDateTimeFormatter} allows for parsing ISO 8601 timestamps using
 *  the legacy {@link SimpleDateFormat}, with internal synchronized blocks to guard
 *  the otherwise non-thread-safe {@link DateFormat}.
 */
public final class LegacyDateTimeFormatter extends LazyReference<DateFormat, RuntimeException> implements Iso8601Formatter {
  public LegacyDateTimeFormatter(String format) {
    super(() -> new SimpleDateFormat(format, Locale.ENGLISH));
  }

  @Override
  public synchronized Date parse(String encoded) throws ParseException {
    return get().parse(encoded);
  }

  @Override
  public synchronized String format(Date date, TimeZone timeZone) {
    final DateFormat dateFormat = get();
    final TimeZone existingTimeZone = dateFormat.getTimeZone();
    dateFormat.setTimeZone(timeZone);
    try {
      return dateFormat.format(date);
    } finally {
      dateFormat.setTimeZone(existingTimeZone);
    }
  }
}
