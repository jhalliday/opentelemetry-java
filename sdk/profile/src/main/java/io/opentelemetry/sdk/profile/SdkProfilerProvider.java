/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.profile.Profiler;
import io.opentelemetry.api.profile.ProfilerBuilder;
import io.opentelemetry.api.profile.ProfilerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** SDK implementation for {@link ProfilerProvider}. */
public final class SdkProfilerProvider implements ProfilerProvider, Closeable {

  static final String DEFAULT_PROFILER_NAME = "unknown";
  private static final Logger LOGGER = Logger.getLogger(SdkProfilerProvider.class.getName());

  private final ProfilerSharedState sharedState;
  private final ComponentRegistry<SdkProfiler> profilerComponentRegistry;
  private final boolean isNoopProfileProcessor;

  /**
   * Returns a new {@link SdkProfilerProviderBuilder} for {@link SdkProfilerProvider}.
   *
   * @return a new builder instance
   */
  public static SdkProfilerProviderBuilder builder() {
    return new SdkProfilerProviderBuilder();
  }

  SdkProfilerProvider(
      Resource resource,
      Supplier<ProfileLimits> profileLimitsSupplier,
      List<ProfileProcessor> processors,
      Clock clock) {
    ProfileProcessor profileProcessor = ProfileProcessor.composite(processors);
    this.sharedState =
        new ProfilerSharedState(resource, profileLimitsSupplier, profileProcessor, clock);
    this.profilerComponentRegistry =
        new ComponentRegistry<>(
            instrumentationScopeInfo -> new SdkProfiler(sharedState, instrumentationScopeInfo));
    this.isNoopProfileProcessor = profileProcessor instanceof NoopProfileProcessor;
  }

  @Override
  public Profiler get(String instrumentationScopeName) {
    return profilerComponentRegistry.get(
        instrumentationNameOrDefault(instrumentationScopeName), null, null, Attributes.empty());
  }

  @Override
  public ProfilerBuilder profilerBuilder(String instrumentationScopeName) {
    if (isNoopProfileProcessor) {
      return ProfilerProvider.noop().profilerBuilder(instrumentationScopeName);
    }
    return new SdkProfilerBuilder(
        profilerComponentRegistry, instrumentationNameOrDefault(instrumentationScopeName));
  }

  private static String instrumentationNameOrDefault(@Nullable String instrumentationScopeName) {
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      LOGGER.fine("Profiler requested without instrumentation scope name.");
      return DEFAULT_PROFILER_NAME;
    }
    return instrumentationScopeName;
  }

  /**
   * Request the active profile processor to process all profiles that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which is completed when the flush is finished
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getProfileProcessor().forceFlush();
  }

  /**
   * Attempt to shut down the active profile processor.
   *
   * @return a {@link CompletableResultCode} which is completed when the active processor has been
   *     shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      LOGGER.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "SdkProfilerProvider{"
        + "clock="
        + sharedState.getClock()
        + ", resource="
        + sharedState.getResource()
        + ", profileLimits="
        + sharedState.getProfileLimits()
        + ", profileProcessor="
        + sharedState.getProfileProcessor()
        + '}';
  }
}
