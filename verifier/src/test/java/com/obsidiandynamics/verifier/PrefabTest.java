package com.obsidiandynamics.verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.*;

import nl.jqno.equalsverifier.internal.prefabvalues.*;

public final class PrefabTest {
  @Test
  public void testSingleton() {
    assertSame(Prefab.getInstance(), Prefab.getInstance());
  }
  
  @Test
  public void testRedBlack() {
    final String red = Prefab.getInstance().red(String.class);
    final String black = Prefab.getInstance().black(String.class);
    assertNotEquals(red, black);
  }
  
  @Test
  public void testShallowCopyArray() {
    final int[] numbers = { 0, 1, 2 };
    final int[] numbersCopy = Prefab.shallowCopy(numbers);
    assertSame(numbers, numbersCopy);
  }
  
  @Test
  public void testShallowCopyNonArray() {
    final String str = "hello";
    final String strCopy = Prefab.shallowCopy(str);
    assertEquals(str, strCopy);
    assertNotSame(str, strCopy);
  }
  
  @Test
  public void testLock() {
    final Object lock = Prefab.getInstance().getLock();
    assertNotNull(lock);
    assertSame(lock, Prefab.getInstance().getLock());
  }
  
  @Test
  public void testGetFactoryCache() {
    assertThat(Prefab.getInstance().getFactoryCache()).isInstanceOf(FactoryCache.class);
  }
  
  @Test
  public void testGetPrefabValues() {
    assertThat(Prefab.getInstance().getPrefabValues()).isInstanceOf(PrefabValues.class);
  }
  
  @Test
  public void testRegister() {
    class TestClass {}
    final Prefab prefab = new Prefab();
    final TestClass red = new TestClass();
    final TestClass black = new TestClass();
    
    prefab.register(TestClass.class, red, black);
    assertSame(red, prefab.red(TestClass.class));
    assertSame(black, prefab.black(TestClass.class));
  }
}
