package com.obsidiandynamics.fslock;

import static com.obsidiandynamics.func.Functions.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.junit.*;

import com.obsidiandynamics.await.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.httpclient.*;
import com.obsidiandynamics.threads.*;

import io.undertow.server.*;

/**
 *  Tests the <em>FS.lock</em> protocol across multiple processes, using {@link AntFork} to spawn JVM forks.
 */
public final class LockRootIT {
  private static final String JDK9_ARGS = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED "
      + "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED "
      + "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED";

  private static final String PARENT_PORT_PROP = LockRootIT.class.getSimpleName() + ".parentPort";

  private static final File ROOT_DIR = new File(System.getProperty("java.io.tmpdir") + File.separator + LockRootIT.class.getSimpleName());

  private static final String NODE_NAME = "node";

  private enum Command {
    TERMINATE,
    ACQUIRE,
    RELEASE;
  }
  
  /**
   *  A forked process under test. Don't care about resource cleanup as the child fork will eventually
   *  get terminated (either explicitly by the parent, or after a timeout).
   */
  static final class ForkedProc {
    private static final int PARENT_PORT = Integer.parseInt(mustExist(System.getProperty(PARENT_PORT_PROP)));

    private static final LockRoot lockRoot = new LockRoot(ROOT_DIR);

    private static final CloseableHttpAsyncClient client = HttpClient.builder().buildAndStart();
    
    private static final ExecutorService ownerThread = Executors.newSingleThreadExecutor();

    private static ReentrantDirectoryLock lock;

    public static void main(String[] args) throws ResponseStatusException, IOException, InterruptedException {
      final NanoRpc rpc = new NanoRpc(ForkedProc::handler).start();
      System.out.format("Fork: starting; registering with parent port %d; listening on child port %d\n", PARENT_PORT, rpc.getBoundPort());
      registerWithParent(rpc.getBoundPort());
      
      // the parent should terminate the child explicitly; if this hasn't occurred, forcibly self-terminate
      Threads.sleep(30_000);
      System.exit(1);
    }

    private static void registerWithParent(int port) throws ResponseStatusException, IOException, InterruptedException {
      sendCommand(client, PARENT_PORT, port);
    }
    
    private static void executeAndWait(CheckedRunnable<Throwable> runnable) throws IOException {
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<IOException> errorRef = new AtomicReference<>();
      ownerThread.execute(() -> {
        try {
          runnable.run();
        } catch (IOException e) {
          errorRef.set(e);
        } catch (Throwable e) {
          errorRef.set(new IOException(e));
        } finally {
          latch.countDown();
        }
      });
      Threads.await(latch);
      
      if (errorRef.get() != null) {
        throw errorRef.get();
      }
    }

    private static void handler(HttpServerExchange exchange) throws IOException {
      final String entity = new String(readAll(exchange.getInputStream()));
      final Command command = Command.valueOf(entity);
      switch (command) {
        case TERMINATE:
          new Thread(() -> {
            Threads.sleep(100);
            System.exit(0);
          }).start();
          break;

        case ACQUIRE:
          mustBeNull(lock, IllegalStateException::new);
          executeAndWait(() -> {
            lock = lockRoot.tryAcquire(NODE_NAME);
          });
          exchange.getResponseSender().send(String.valueOf(lock != null));
          break;

        case RELEASE:
          mustExist(lock, IllegalStateException::new);
          executeAndWait(() -> {
            lock.release();
            lock = null;
          });
          break;

        default:
          throw new UnsupportedOperationException("Unsupported command " + command);
      }
    }
  }
  
  private static byte[] readAll(InputStream in) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      int read;
      while ((read = in.read()) != -1) {
        baos.write(read);
      }
      return baos.toByteArray();
    }
  }

  private static String sendCommand(CloseableHttpAsyncClient client, int port, Object entity) throws ResponseStatusException, IOException, InterruptedException {
    final HttpPost post = new HttpPost("http://localhost:" + port);
    post.setEntity(new StringEntity(String.valueOf(entity)));
    return HttpCall.withClient(client).invoke(post).ensureIsOk().getEntityString();
  }

  private static boolean sendCommandBoolean(CloseableHttpAsyncClient client, int port, Object entity) throws ResponseStatusException, IOException, InterruptedException {
    return Boolean.parseBoolean(sendCommand(client, port, entity));
  }

  private CloseableHttpAsyncClient client;

  @Before
  public void before() {
    client = HttpClient.builder().buildAndStart();
  }

  @After
  public void after() throws IOException {
    ifPresentVoid(client, Closeable::close);
  }

  /**
   *  Forks a pair of {@link ForkedProc} processes, provided the parent's HTTP port as a {@code -D} JVM argument. 
   *  Processes start their own web servers and register back with the parent by invoking a POST endpoint on the parent's
   *  web server. At this point, the parent can communicate with the child forks, and will issue acquire/release
   *  commands (as HTTP POSTs to the child processes) and assert results.
   *  
   *  @throws InterruptedException
   *  @throws ResponseStatusException
   *  @throws IOException
   */
  @Test
  public void testAcrossJvms() throws InterruptedException, ResponseStatusException, IOException {
    final List<Integer> forkPorts = new ArrayList<>(2);

    final NanoRpc rpc = new NanoRpc(exchange -> {
      final String entity = new String(readAll(exchange.getInputStream()));
      final int forkPort = Integer.parseInt(entity);
      System.out.format("Main: fork registered on port %d\n", forkPort);
      forkPorts.add(forkPort);
    }).start();
    System.out.format("Main: starting; listening on %d\n", rpc.getBoundPort());

    // fork a pair of child processes
    final Thread t0 = forkInNewThread(rpc.getBoundPort());
    final Thread t1 = forkInNewThread(rpc.getBoundPort());

    try {
      // wait for the forks to report back to the parent with their respective web ports
      Timesert.wait(30_000).untilTrue(() -> forkPorts.size() == 2);

      assertTrue(sendCommandBoolean(client, forkPorts.get(0), Command.ACQUIRE));
      assertFalse(sendCommandBoolean(client, forkPorts.get(1), Command.ACQUIRE));
      sendCommand(client, forkPorts.get(0), Command.RELEASE);
      assertTrue(sendCommandBoolean(client, forkPorts.get(1), Command.ACQUIRE));
    } finally {
      for (int forkPort : forkPorts) {
        sendCommand(client, forkPort, Command.TERMINATE);
      }
      t0.join();
      t1.join();
    }
  }

  private static Thread forkInNewThread(int parentPort) {
    final String javaVersion = System.getProperty("java.version");
    final String jvmArgs;
    if (javaVersion.startsWith("1.8.")) {
      jvmArgs = "";
    } else {
      jvmArgs = JDK9_ARGS;
    }
    final AntFork antFork = new AntFork(ForkedProc.class)
        .withJvmArgs(jvmArgs + " -D" + PARENT_PORT_PROP + "=" + parentPort);
    final Thread thread = new Thread(antFork::run);
    thread.start();
    return thread;
  }
}
