package com.obsidiandynamics.version;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.version.AppVersion.*;

public final class AppVersionTest {
  @Test
  public void testValid() throws IOException {
    final String version = AppVersion.get("test");
    assertEquals("1.2.3_0", version);
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testInvalidWithException() throws IOException {
    AppVersion.getFile("wrong.file");
  }
  
  @Test
  public void testInvalidWithDefault() throws IOException {
    final String version = AppVersion.getFile("wrong.file", new Constant("default"));
    assertEquals("default", version);
  }
  
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(AppVersion.class);
  }
}