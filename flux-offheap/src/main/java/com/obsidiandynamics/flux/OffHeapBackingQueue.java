package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;
import java.util.concurrent.locks.*;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.esotericsoftware.kryo.util.*;
import com.obsidiandynamics.fslock.*;
import com.obsidiandynamics.format.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.io.*;
import com.obsidiandynamics.random.*;

import net.openhft.chronicle.queue.*;
import net.openhft.chronicle.queue.impl.single.*;
import net.openhft.chronicle.wire.*;

final class OffHeapBackingQueue<E> implements BackingQueue<E> {
  private static final long MAX_BLOCK_MILLIS = 10;
  private static final int MIN_KRYO_BUFFER_SIZE = 1 << 8;
  private static final int MAX_KRYO_BUFFER_SIZE = 1 << 20;

  private static final LockRoot lockRoot = 
      new LockRoot(new File(System.getProperty("java.io.tmpdir") + File.separator + OffHeapBackingQueue.class.getSimpleName()));

  private final Pool<Kryo> pool;

  private final int capacity;

  private int queued;

  private final Object mutex = new Object();

  private final ReentrantDirectoryLock nodeLock;

  private final SingleChronicleQueue queue;

  private final ReentrantReadWriteLock queueAccessLock = new ReentrantReadWriteLock();

  private volatile boolean closed;

  private final ExcerptAppender appender;

  private final ExcerptTailer tailer;

  OffHeapBackingQueue(Pool<Kryo> pool, int capacity) {
    mustExist(pool, "Kryo pool cannot be null");
    mustBeGreater(capacity, 0, illegalArgument("Capacity must be greater than 0"));
    this.pool = pool;
    this.capacity = capacity;

    Exceptions.wrapStrict(lockRoot::vacuumAll, RuntimeIOException::new);
    final String nodeName = Binary.toHex(Randomness.nextBytes(8));
    nodeLock = Exceptions.wrapStrict(() -> lockRoot.tryAcquire(nodeName), RuntimeIOException::new);
    mustExist(nodeLock, withMessage(SafeFormat.supply("Could not acquire node lock %s", nodeName), IllegalStateException::new));
    final File queueDir = new File(nodeLock.getLock().getAbsolutePath());
    queue = ChronicleQueue.singleBuilder(queueDir).build();
    appender = queue.acquireAppender();
    tailer = queue.createTailer();
  }

  @Override
  public E poll(int timeoutMillis) throws InterruptedException {
    final long pollExpiry = System.currentTimeMillis() + timeoutMillis;
    for (;;) {
      final E taken = tryReadFromQueue();
      if (taken != null) {
        synchronized (mutex) {
          queued--;
          mutex.notify();
        }
        return taken;
      } else if (! closed) {
        synchronized (mutex) {
          if (queued > 0) {
            // there's at least one pending item; don't bother going into a wait loop
            continue;
          }
        }
        
        for (;;) {
          final long remainingTimeMillis = pollExpiry - System.currentTimeMillis();
          if (remainingTimeMillis > 0) {
            final long waitTime = Math.min(MAX_BLOCK_MILLIS, remainingTimeMillis);
            synchronized (mutex) {
              if (queued == 0) {
                mutex.wait(waitTime);
              } else {
                break;
              }
            }
          } else {
            return null;
          }
        }
      } else {
        return null;
      }
    }
  }

  private E tryReadFromQueue() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    
    final class BytesHolder {
      byte[] bytes;
    }
    final BytesHolder bytesHolder = new BytesHolder();

    queueAccessLock.readLock().lock();
    try {
      if (! closed) {
        try (DocumentContext dc = tailer.readingDocument()) {
          if (dc.isPresent()) {
            dc.wire().readBytes(in -> {
              final int length = in.readInt();
              bytesHolder.bytes = new byte[length];
              in.read(bytesHolder.bytes);
            });
          }
        } 
      } else {
        return null;
      }
    } finally {
      queueAccessLock.readLock().unlock();
    }

    if (bytesHolder.bytes != null) {
      final ByteBufferInput input = new ByteBufferInput(bytesHolder.bytes);
      final Kryo kryo = pool.obtain();
      try {
        return Classes.cast(kryo.readClassAndObject(input));
      } finally {
        pool.free(kryo);
      }
    } else {
      return null;
    }
  }

  @Override
  public void put(E element) throws InterruptedException {
    for (;;) {
      final boolean atCapacity;
      synchronized (mutex) {
        atCapacity = queued == capacity;
        if (atCapacity) {
          if (! closed) {
            mutex.wait(MAX_BLOCK_MILLIS);
          } else {
            return;
          }
        } else {
          queued++;
          mutex.notify();
        }
      }

      if (! atCapacity) {
        writeToQueue(element);
        return;
      }
    }
  }

  private void writeToQueue(E element) {
    final byte[] bytes;
    try (ByteBufferOutput output = new ByteBufferOutput(MIN_KRYO_BUFFER_SIZE, MAX_KRYO_BUFFER_SIZE)) {
      final Kryo kryo = pool.obtain();
      try {
        kryo.writeClassAndObject(output, element);
      } finally {
        pool.free(kryo);
      }
  
      bytes = output.toBytes();
    }
    
    queueAccessLock.readLock().lock();
    try {
      if (! closed) {
        try (DocumentContext dc = appender.writingDocument()) {
          dc.wire().writeBytes(out -> {
            out.writeInt(bytes.length);
            out.write(bytes);
          });
        }
      }
    } finally {
      queueAccessLock.readLock().unlock();
    }
  }
  
  @Override
  public void dispose() {
    queueAccessLock.writeLock().lock();
    try {
      if (! closed) {
        closed = true;
        queue.close();
        Exceptions.wrapStrict(() -> nodeLock.release(false), RuntimeIOException::new);
      }
    } finally {
      queueAccessLock.writeLock().unlock();
    }
  }
}
