package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.zlg.Zlg.*;

public final class NopLevelChainTest {
  @Test
  public void test() {
    final NopLevelChain chain = NopLevelChain.getInstance();
    final LogChain end = chain
        .tag("tag")
        .format("format")
        .arg(true)
        .arg((byte) 0x01)
        .arg('c')
        .arg(3.14d)
        .arg(3.14)
        .arg(42)
        .arg(42L)
        .arg("string")
        .arg((short) 42)
        .stack(null);
    assertSame(chain, end);
  }
}
