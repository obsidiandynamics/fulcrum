package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;
import org.slf4j.*;

import com.obsidiandynamics.testmark.*;

public final class Slf4jDisabledVolumeTest extends AbstractDisabledVolumeTest {
  @Test
  public void testBenchmark() {
    final String name = "SLF4J";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, cycle()));
  }
  
  private static TestCycle cycle() {
    final Logger logger = LoggerFactory.getLogger(AbstractDisabledVolumeTest.class);
    assertFalse(logger.isTraceEnabled());
    
    return (f, d, i, l) -> {
      if (l < 0) throw new AssertionError();
      logger.trace("float: {}, double: {}, int: {}, long: {}", f, d, i, l);
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(Slf4jDisabledVolumeTest.class);
  }
}
