package com.obsidiandynamics.dyno;

import org.openjdk.jmh.infra.*;

final class BlackholeAbyss implements Abyss {
  Blackhole blackhole;

  @Override
  public void consume(Object obj) {
    blackhole.consume(obj);
  }

  @Override
  public void consume(byte b) {
    blackhole.consume(b);
  }

  @Override
  public void consume(boolean bool) {
    blackhole.consume(bool);
  }

  @Override
  public void consume(char c) {
    blackhole.consume(c);
  }

  @Override
  public void consume(short s) {
    blackhole.consume(s);
  }

  @Override
  public void consume(int i) {
    blackhole.consume(i);
  }

  @Override
  public void consume(long l) {
    blackhole.consume(l);
  }

  @Override
  public void consume(float f) {
    blackhole.consume(f);
  }

  @Override
  public void consume(double d) {
    blackhole.consume(d);
  }
}
