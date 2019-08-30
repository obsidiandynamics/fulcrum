package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;

/**
 *  A reentrant lock that can be affiliated with an arbitrary object for the purpose of tracking
 *  ownership. <p>
 *  
 *  Unlike a conventional {@link java.util.concurrent.locks.ReentrantLock} that is owned by the
 *  acquiring thread, a {@link ReentrantAffiliateLock} allows the caller to specify any object that
 *  makes sense in the context of how the lock is being utilised. Naturally, the constraint is that
 *  the same affiliate that was used during {@link #tryLock(int, Object)} is also passed to the
 *  subsequent {@link #unlock(Object)} call. Equality is determined by invoking the {@code equals()}
 *  method on the affiliate pairs. <p>
 *  
 *  As a special case, the affiliate may be assigned to {@link Thread#currentThread()}. This makes
 *  an {@link ReentrantAffiliateLock} behave like a conventional thread-bound reentrant lock.
 */
public final class ReentrantAffiliateLock {
  private final Object monitor = new Object();

  /** The affiliate object that is bound to the lock; {@code null} being the equivalent of unlocked. */
  private Object affiliate;

  /** Present acquisition depth, {@code 0} being the equivalent of an unlocked state. */
  private int depth;

  /**
   *  Attempts to acquire the lock for the given {@code affiliate}, blocking up to the specified
   *  upper bound. <p>
   *  
   *  Once a lock has been successfully acquired, subsequent acquisition attempts using the same
   *  affiliate will succeed immediately without blocking (irrespective of the timeout value). The
   *  expectation is that the caller will invoke {@link #unlock(Object)} precisely as many times as
   *  it had acquired the lock.
   *  
   *  @param timeoutMillis The acquisition timeout, in milliseconds.
   *  @param affiliate The affiliate object to use.
   *  @return True if the lock was acquired within the time bound; false if the acquisition timed out.
   *  @throws InterruptedException If the thread is interrupted.
   */
  public boolean tryLock(int timeoutMillis, Object affiliate) throws InterruptedException {
    mustExist(affiliate, "Affiliate cannot be null");

    for (final long deadline = System.currentTimeMillis() + timeoutMillis;;) {
      synchronized (monitor) {
        if (this.affiliate != null && ! this.affiliate.equals(affiliate)) {
          final long timeRemaining = deadline - System.currentTimeMillis();
          if (timeRemaining > 0) {
            monitor.wait(timeRemaining);
          } else {
            return false;
          }
        }

        if (this.affiliate == null || this.affiliate.equals(affiliate)) {
          this.affiliate = affiliate;
          depth++;
          return true;
        }
      }
    }
  }

  /**
   *  Unlocks a previously acquired lock using a given {@code affiliate}. <p>
   *  
   *  The expectation is that the lock must be in a locked state for this method invocation to be permitted;
   *  furthermore, it must be bound to the same affiliate that was used during the earlier call
   *  to {@link #tryLock(int, Object)}. Equality is determined by calling {@code equals()} on the affiliates.
   *  Failure to follow this protocol will result in an {@link IllegalMonitorStateException}.
   *  
   *  @param affiliate The affiliate object to use.
   *  @throws IllegalMonitorStateException If the lock is not locked or is bound to a different affiliate.
   */
  public void unlock(Object affiliate) {
    mustExist(affiliate, "Affiliate cannot be null");
    synchronized (monitor) {
      mustExist(this.affiliate, withMessage("Not locked", IllegalMonitorStateException::new));
      mustBeEqual(this.affiliate, affiliate, 
                  withMessage(() -> "Lock affiliated with " + this.affiliate + " is being unlocked by " + affiliate, IllegalMonitorStateException::new));
      if (--depth == 0) {
        this.affiliate = null;
        monitor.notify();
      }
    }
  }
}
