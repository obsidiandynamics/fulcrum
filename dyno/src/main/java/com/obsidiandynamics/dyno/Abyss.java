package com.obsidiandynamics.dyno;

/**
 *  The abyss 'consumes' the values, yielding no information to JIT whether the
 *  value is actually used afterwards. This can save from the dead-code elimination
 *  of the computations resulting in the given values.
 */
public interface Abyss {
  void consume(Object obj);

  void consume(byte b);

  void consume(boolean bool);

  void consume(char c);

  void consume(short s);

  void consume(int i);

  void consume(long l);

  void consume(float f);

  void consume(double d);
}
