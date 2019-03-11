package com.obsidiandynamics.fslock;

import java.io.*;
import java.util.*;

public final class FSLockTester {
  public static void main(String[] args) throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(System.getProperty("java.io.tmpdir") + File.separator + "lock-tester"));
    System.out.println(UUID.randomUUID());
    System.out.print("Acquring...");
    try (ReentrantDirectoryLock lock = lockRoot.acquire("test")) {
      System.out.println("done");
      Thread.sleep(5_000);
    }
    System.out.println("Released");
  }
}
