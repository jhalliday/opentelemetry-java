/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.profile.ProfileBuilder;
import io.opentelemetry.api.profile.Profiler;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Builder class for {@link SdkProfilerProvider} instances. */
public final class SdkProfilerProviderBuilder {

  private final List<ProfileProcessor> profileProcessors = new ArrayList<>();
  private Resource resource = Resource.getDefault();
  private Supplier<ProfileLimits> profileLimitsSupplier = ProfileLimits::getDefault;
  private Clock clock = Clock.getDefault();

  SdkProfilerProviderBuilder() {}

  /**
   * Assign a {@link Resource} to be attached to all {@link ProfileData} created by {@link
   * Profiler}s obtained from the {@link SdkProfilerProvider}.
   *
   * @param resource the resource
   * @return this
   */
  public SdkProfilerProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link ProfileLimits}. {@link ProfileLimits} will be retrieved
   * each time a {@link Profiler#profileBuilder()} is called.
   *
   * <p>The {@code profileLimitsSupplier} must be thread-safe and return immediately (no remote
   * calls, as contention free as possible).
   *
   * @param profileLimitsSupplier the supplier that will be used to retrieve the {@link
   *     ProfileLimits} for every {@link ProfileBuilder}.
   * @return this
   */
  public SdkProfilerProviderBuilder setProfileLimits(
      Supplier<ProfileLimits> profileLimitsSupplier) {
    requireNonNull(profileLimitsSupplier, "profileLimitsSupplier");
    this.profileLimitsSupplier = profileLimitsSupplier;
    return this;
  }

  /**
   * Add a profile processor. {@link ProfileProcessor#onEmit(Context, ReadWriteProfile)} will be
   * called each time a profile is emitted by {@link Profiler} instances obtained from the {@link
   * SdkProfilerProvider}.
   *
   * @param processor the profile processor
   * @return this
   */
  public SdkProfilerProviderBuilder addProfileProcessor(ProfileProcessor processor) {
    requireNonNull(processor, "processor");
    profileProcessors.add(processor);
    return this;
  }

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkProfilerProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Create a {@link SdkProfilerProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public SdkProfilerProvider build() {
    return new SdkProfilerProvider(resource, profileLimitsSupplier, profileProcessors, clock);
  }
}
