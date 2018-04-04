package com.obsidiandynamics.io;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ResourceLoaderTest {
  private InputStream in;
  
  @After
  public void after() throws IOException {
    if (in != null) in.close();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(ResourceLoader.class);
  }
  
  @Test
  public void testFile() throws FileNotFoundException {
    in = ResourceLoader.stream(URI.create("file://src/test/resources/resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testFileNotFound() throws FileNotFoundException {
    ResourceLoader.stream(URI.create("file://src/test/resources/nonexistent"));
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testClasspathNotFound() throws FileNotFoundException {
    ResourceLoader.stream(URI.create("cp://src/test/resources/nonexistent"));
  }
  
  @Test
  public void testCP() throws FileNotFoundException {
    in = ResourceLoader.stream(URI.create("cp://resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test
  public void testClasspath() throws FileNotFoundException {
    in = ResourceLoader.stream(URI.create("cp://resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test
  public void testTryClasspathExists() {
    in = ResourceLoader.tryStream(URI.create("cp://resource-locator.test"));
    assertNotNull(in);
  }
  
  @Test
  public void testTryClasspathNotFound() {
    in = ResourceLoader.tryStream(URI.create("cp://resource-locator-nonexistent.test"));
    assertNull(in);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testUnsupported() throws FileNotFoundException {
    in = ResourceLoader.stream(URI.create("xxx://resource-locator.test"));
  }
}
