/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalProfilerProviderTest {

  @BeforeAll
  static void beforeClass() {
    GlobalProfilerProvider.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalProfilerProvider.resetForTest();
  }

  @Test
  void setAndGet() {
    assertThat(GlobalProfilerProvider.get()).isEqualTo(ProfilerProvider.noop());
    ProfilerProvider profilerProvider =
        instrumentationScopeName ->
            ProfilerProvider.noop().profilerBuilder(instrumentationScopeName);
    GlobalProfilerProvider.set(profilerProvider);
    assertThat(GlobalProfilerProvider.get()).isEqualTo(profilerProvider);
  }

  @Test
  void setThenSet() {
    GlobalProfilerProvider.set(
        instrumentationScopeName ->
            ProfilerProvider.noop().profilerBuilder(instrumentationScopeName));
    assertThatThrownBy(
            () ->
                GlobalProfilerProvider.set(
                    instrumentationScopeName ->
                        ProfilerProvider.noop().profilerBuilder(instrumentationScopeName)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalProfilerProvider.set has already been called")
        .hasStackTraceContaining("setThenSet");
  }
}
