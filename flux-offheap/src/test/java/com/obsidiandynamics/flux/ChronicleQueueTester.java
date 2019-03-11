package com.obsidiandynamics.flux;

import java.io.*;

import com.obsidiandynamics.threads.*;

import net.openhft.chronicle.queue.*;
import net.openhft.chronicle.queue.impl.single.*;
import net.openhft.chronicle.wire.*;

public final class ChronicleQueueTester {
  public static void main(String[] args) {
    final File dir = new File(System.getProperty("java.io.tmpdir") + "/chronicle");
    System.out.println("dir=" + dir);
    try (SingleChronicleQueue queue = ChronicleQueue.singleBuilder(dir).build()) {
      final ExcerptAppender appender = queue.acquireAppender();
      try (DocumentContext dc = appender.writingDocument()) {
        dc.wire().write().text("test");
      }
      
      final ExcerptTailer tailer = queue.createTailer();
      while (true) {
        try (DocumentContext dc = tailer.readingDocument()) {
          if (dc.isPresent()) {
            final String read = dc.wire().read().text();
            System.out.println("read: " + read);
          } else {
            Threads.sleep(10);
          }
        }
      }
    }
  }
}
