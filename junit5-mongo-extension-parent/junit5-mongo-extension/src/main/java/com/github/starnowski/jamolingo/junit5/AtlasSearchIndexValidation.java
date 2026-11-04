package com.github.starnowski.jamolingo.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation to configure wait duration and polling interval for Atlas Search Index synchronization
 * validation at the test class or test method level.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AtlasSearchIndexValidation {

  /**
   * The maximum duration to wait for index synchronization. Defaults to 30.
   *
   * @return the maximum wait duration
   */
  long maxWaitDuration() default AtlasSearchIndexValidationConfig.DEFAULT_MAX_WAIT_DURATION;

  /**
   * The time unit for the maximum wait duration. Defaults to SECONDS.
   *
   * @return the maximum wait time unit
   */
  TimeUnit maxWaitUnit() default TimeUnit.SECONDS;

  /**
   * The polling interval between synchronization checks. Defaults to 1.
   *
   * @return the polling interval
   */
  long pollInterval() default AtlasSearchIndexValidationConfig.DEFAULT_POLL_INTERVAL;

  /**
   * The time unit for the polling interval. Defaults to SECONDS.
   *
   * @return the polling interval time unit
   */
  TimeUnit pollIntervalUnit() default TimeUnit.SECONDS;
}
