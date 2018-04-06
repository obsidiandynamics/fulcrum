package com.obsidiandynamics.dyno;

import org.junit.*;
import org.openjdk.jmh.infra.*;

public final class BlackholeAbyssTest {
  @Test
  public void testCoverage() {
    final BlackholeAbyss abyss = new BlackholeAbyss();
    abyss.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    
    abyss.consume(true);
    abyss.consume((byte) 0x01);
    abyss.consume('c');
    abyss.consume(3.14d);
    abyss.consume(3.14f);
    abyss.consume(42);
    abyss.consume(42L);
    abyss.consume("object");
    abyss.consume((short) 42);
  }
}
