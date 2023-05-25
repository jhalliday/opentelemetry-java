/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class provides a temporary global accessor for {@link ProfilerProvider} until the profiles
 * API is marked stable. It will eventually be merged into {@link GlobalOpenTelemetry}.
 */
// We intentionally assign to be used for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalProfilerProvider {

  private static final AtomicReference<ProfilerProvider> instance =
      new AtomicReference<>(ProfilerProvider.noop());

  @Nullable private static volatile Throwable setInstanceCaller;

  private GlobalProfilerProvider() {}

  /** Returns the globally registered {@link ProfilerProvider}. */
  // instance cannot be set to null
  @SuppressWarnings("NullAway")
  public static ProfilerProvider get() {
    return instance.get();
  }

  /**
   * Sets the global {@link ProfilerProvider}. Future calls to {@link #get()} will return the
   * provided {@link ProfilerProvider} instance. This should be called once as early as possible in
   * your application initialization logic.
   */
  public static void set(ProfilerProvider profilerProvider) {
    boolean changed = instance.compareAndSet(ProfilerProvider.noop(), profilerProvider);
    if (!changed && (profilerProvider != ProfilerProvider.noop())) {
      throw new IllegalStateException(
          "GlobalProfilerProvider.set has already been called. GlobalProfilerProvider.set "
              + "must be called only once before any calls to GlobalProfilerProvider.get. "
              + "Previous invocation set to cause of this exception.",
          setInstanceCaller);
    }
    setInstanceCaller = new Throwable();
  }

  /**
   * Unsets the global {@link ProfilerProvider}. This is only meant to be used from tests which need
   * to reconfigure {@link ProfilerProvider}.
   */
  public static void resetForTest() {
    instance.set(ProfilerProvider.noop());
  }
}
