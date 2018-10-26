package com.obsidiandynamics.json.fieldpatch;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.*;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.obsidiandynamics.format.*;
import com.obsidiandynamics.func.*;

public final class Iso8601FieldPatch implements FieldPatch<Date> {
  @JsonProperty @JsonFormat(shape=STRING, pattern=Iso8601.DATE_TIME_MILLIS_FORMAT)
  private final Date value;
  
  private Iso8601FieldPatch(@JsonProperty("value") Date date) {
    this.value = date;
  }
  
  public static Iso8601FieldPatch of(Date date) {
    return new Iso8601FieldPatch(date);
  }
  
  @Override
  public Date get() {
    return value;
  }
  
  @Override
  public int hashCode() {
    return baseHashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Iso8601FieldPatch) {
      return baseEquals(Classes.cast(obj));
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return baseToString();
  }
}
