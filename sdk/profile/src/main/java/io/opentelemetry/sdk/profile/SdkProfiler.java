/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.profile.ProfileBuilder;
import io.opentelemetry.api.profile.Profiler;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

/** SDK implementation of {@link Profiler}. */
final class SdkProfiler implements Profiler {

  private final ProfilerSharedState profilerSharedState;

  private final InstrumentationScopeInfo instrumentationScopeInfo;

  SdkProfiler(
      ProfilerSharedState profilerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    this.profilerSharedState = profilerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  @Override
  public ProfileBuilder profileBuilder() {
    return new SdkProfileBuilder(profilerSharedState, instrumentationScopeInfo);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
