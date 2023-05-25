/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Profiler} is the entry point into a profiling pipeline.
 *
 * <p>Obtain a {@link #profileBuilder()}, add properties using the setters, and emit it via {@link
 * ProfileBuilder#emit()}.
 */
@ThreadSafe
public interface Profiler {

  /**
   * Return a {@link ProfileBuilder} to emit a profile.
   *
   * <p>Build the profile using the {@link ProfileBuilder} setters, and emit via {@link
   * ProfileBuilder#emit()}.
   */
  ProfileBuilder profileBuilder();
}
