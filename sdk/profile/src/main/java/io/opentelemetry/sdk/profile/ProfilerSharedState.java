/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link SdkProfiler}s created by the same {@link
 * SdkProfilerProvider}.
 */
final class ProfilerSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final Supplier<ProfileLimits> profileLimitsSupplier;
  private final ProfileProcessor profileProcessor;
  private final Clock clock;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  ProfilerSharedState(
      Resource resource,
      Supplier<ProfileLimits> profileLimitsSupplier,
      ProfileProcessor profileProcessor,
      Clock clock) {
    this.resource = resource;
    this.profileLimitsSupplier = profileLimitsSupplier;
    this.profileProcessor = profileProcessor;
    this.clock = clock;
  }

  Resource getResource() {
    return resource;
  }

  ProfileLimits getProfileLimits() {
    return profileLimitsSupplier.get();
  }

  ProfileProcessor getProfileProcessor() {
    return profileProcessor;
  }

  Clock getClock() {
    return clock;
  }

  boolean hasBeenShutdown() {
    return shutdownResult != null;
  }

  CompletableResultCode shutdown() {
    synchronized (lock) {
      if (shutdownResult != null) {
        return shutdownResult;
      }
      shutdownResult = profileProcessor.shutdown();
      return shutdownResult;
    }
  }
}
