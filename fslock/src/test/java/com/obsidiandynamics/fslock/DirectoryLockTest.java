package com.obsidiandynamics.fslock;

import java.io.*;
import java.util.*;

import org.assertj.core.api.*;
import org.junit.*;

public final class DirectoryLockTest {
  private static final String SANDBOX_DIR = System.getProperty("java.io.tmpdir") + File.separator + "sandbox";
  
  @Test
  public void testEnsureIsFile() {
    final File sandboxDir = new File(SANDBOX_DIR);
    sandboxDir.mkdirs();
    DirectoryLock.ensureIsDirectory(sandboxDir);
    Assertions.assertThatThrownBy(() -> {
      DirectoryLock.ensureIsFile(sandboxDir);
    }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not a file");
  }  
  
  @Test
  public void testEnsureIsDirectory() throws IOException {
    final File fileInTmp = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID());
    fileInTmp.createNewFile();
    fileInTmp.deleteOnExit();
        
    DirectoryLock.ensureIsFile(fileInTmp);
    Assertions.assertThatThrownBy(() -> {
      DirectoryLock.ensureIsDirectory(fileInTmp);
    }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not a directory");
  }
}
