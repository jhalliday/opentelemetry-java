/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating scoped {@link Profiler}s. The name <i>Provider</i> is for consistency
 * with other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see Profiler
 */
@ThreadSafe
public interface ProfilerProvider {

  /**
   * Gets or creates a named Profiler instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Profiler instance.
   */
  default Profiler get(String instrumentationScopeName) {
    return profilerBuilder(instrumentationScopeName).build();
  }

  /**
   * Creates a ProfilerBuilder for a named Profiler instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a ProfilerBuilder instance.
   */
  ProfilerBuilder profilerBuilder(String instrumentationScopeName);

  /**
   * Returns a no-op {@link ProfilerProvider} which provides Profilers which do not record or emit.
   */
  static ProfilerProvider noop() {
    return DefaultProfilerProvider.getInstance();
  }
}
