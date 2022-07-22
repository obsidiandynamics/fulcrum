package com.obsidiandynamics.launcher;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.launcher.Launcher.*;
import com.obsidiandynamics.resolver.*;

public final class LauncherTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Launcher.class);
  }

  @Test
  public void testMatchClass() {
    final String class0 = "com.obsidiandynamics.foo.Alpha";
    final String class1 = "com.obsidiandynamics.foo.Bravo";
    final String[] classes = { class0, class1 };
    assertEquals(class0, Launcher.matchClass("Alpha", classes));
    assertNull(Launcher.matchClass("Charlie", classes));
  }
  
  @Test
  public void testRunByPartialNameSuccess() throws Exception {
    final Options options = new Options() {{
      partialClassName = "Alpha";
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out).printf(isNotNull(), any());
    verify(options.runner).run(eq("foo.Alpha"));
    verifyNoMoreInteractions(options.err);
    verifyNoMoreInteractions(options.in);
  }
  
  @Test
  public void testRunByPartialNameNoMatch() throws Exception {
    final Options options = new Options() {{
      partialClassName = "Charlie";
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.err).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verifyNoMoreInteractions(options.out);
    verifyNoMoreInteractions(options.in);
  }
  
  @Test
  public void testRunByMenuSuccess() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    when(options.in.readLine()).thenReturn("1");
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verify(options.runner).run(eq("foo.Alpha"));
    verify(options.in).readLine();
    verifyNoMoreInteractions(options.err);
  }
  
  @Test
  public void testRunByMenuNoSelectionExit() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    
    // first case -- reader responds with a blank string
    when(options.in.readLine()).thenReturn("  ");
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verify(options.in).readLine();
    verifyNoMoreInteractions(options.err);
    
    // second case -- reader responds with 'no more lines'
    reset(options.out, options.err, options.in, options.runner);
    when(options.in.readLine()).thenReturn(null);
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verify(options.in).readLine();
    verifyNoMoreInteractions(options.err);
  }
  
  @Test
  public void testRunByMenuInvalidSelectionNotANumber() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    when(options.in.readLine()).thenReturn("x");
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verify(options.in).readLine();
    verify(options.err).printf(isNotNull(), any());
  }
  
  @Test
  public void testRunByMenuInvalidSelectionIndexTooLow() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    when(options.in.readLine()).thenReturn("0");
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verify(options.in).readLine();
    verify(options.err).printf(isNotNull(), any());
  }
  
  @Test
  public void testRunByMenuInvalidSelectionIndexTooHigh() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    when(options.in.readLine()).thenReturn("3");
    Launcher.run(options, "foo.Alpha", "foo.Bravo");
    verify(options.out, atLeast(1)).printf(isNotNull(), any());
    verifyNoMoreInteractions(options.runner);
    verify(options.in).readLine();
    verify(options.err).printf(isNotNull(), any());
  }
  
  private static class TestClassRun {
    // will be invoked reflectively
    @SuppressWarnings("unused")
    public static void main(String[] args) {
      Resolver.assign(TestClassRun.class, TestClassRun::new);
    }
  }
  
  @Test
  public void testRunClass() throws Exception {
    try {
      Launcher.run(TestClassRun.class.getName());
      assertNotNull(Resolver.lookup(TestClassRun.class, null));
    } finally {
      Resolver.reset();
    }
  }
  
  @Test
  public void testRunAndLogExceptionNoError() {
    final PrintStream err = mock(PrintStream.class);
    Launcher.runAndLogException(() -> {}, err);
    verifyNoMoreInteractions(err);
  }
  
  @Test
  public void testRunAndLogExceptionWithError() {
    final PrintStream err = mock(PrintStream.class);
    Launcher.runAndLogException(() -> {throw new Exception("simulated error");}, err);
    verify(err, atLeastOnce()).println((Exception) isNotNull());
  }
  
  @Test
  public void testMain() throws Exception {
    final Options options = new Options() {{
      out = mock(ConsoleWriter.class);
      err = mock(ConsoleWriter.class);
      in = mock(ConsoleReader.class);
      runner = mock(ClassRunner.class);
    }};
    when(options.in.readLine()).thenReturn("0");
    
    Resolver.assign(Options.class, Singleton.of(options));
    try {
      Launcher.main(new String[] {"foo.Alpha", "foo.Bravo"});
      verify(options.out, atLeast(1)).printf(isNotNull(), any());
      verifyNoMoreInteractions(options.runner);
      verify(options.in).readLine();
      verify(options.err).printf(isNotNull(), any());
    } finally {
      Resolver.reset();
    }
  }
}
