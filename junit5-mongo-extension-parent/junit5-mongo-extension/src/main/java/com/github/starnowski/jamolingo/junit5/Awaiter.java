package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Utility class that polls a boolean condition at a specified interval up to a maximum wait
 * duration.
 */
public class Awaiter {

  private long maxWaitDuration = 30;
  private TimeUnit maxWaitUnit = SECONDS;
  private long pollInterval = 1;
  private TimeUnit pollIntervalUnit = SECONDS;

  /**
   * Creates a new Awaiter instance with default settings (atMost 30 seconds, pollInterval 1
   * second).
   *
   * @return a new Awaiter instance
   */
  public static Awaiter await() {
    return new Awaiter();
  }

  /**
   * Sets the maximum duration to wait for the condition to become true.
   *
   * @param duration the maximum wait duration
   * @param unit the time unit of the maximum wait duration
   * @return this Awaiter instance
   */
  public Awaiter atMost(long duration, TimeUnit unit) {
    this.maxWaitDuration = duration;
    this.maxWaitUnit = unit;
    return this;
  }

  /**
   * Sets the polling interval between condition evaluations.
   *
   * @param interval the polling interval
   * @param unit the time unit of the polling interval
   * @return this Awaiter instance
   */
  public Awaiter pollInterval(long interval, TimeUnit unit) {
    this.pollInterval = interval;
    this.pollIntervalUnit = unit;
    return this;
  }

  /**
   * Repeatedly evaluates the given condition at the configured poll interval until it returns true
   * or the maximum wait duration is reached.
   *
   * @param condition the condition to evaluate
   * @return true if the condition returned true within the maximum wait duration, false otherwise
   */
  public boolean until(BooleanSupplier condition) {
    long timeoutNanos = maxWaitUnit.toNanos(maxWaitDuration);
    long pollNanos = pollIntervalUnit.toNanos(pollInterval);
    long deadline = System.nanoTime() + timeoutNanos;
    while (true) {
      if (condition.getAsBoolean()) {
        return true;
      }
      long remainingNanos = deadline - System.nanoTime();
      if (remainingNanos <= 0) {
        return false;
      }
      try {
        TimeUnit.NANOSECONDS.sleep(Math.min(pollNanos, remainingNanos));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while waiting for condition", e);
      }
    }
  }
}
