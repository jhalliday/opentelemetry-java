/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.ProfileLimits;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * TODO point to profile spec once it exists... Profile definition as described in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/...md">OpenTelemetry
 * Profiling Data Model</a>.
 */
@Immutable
public interface ProfileData {

  /** Returns the resource of this profile. */
  Resource getResource();

  /** Returns the instrumentation scope that generated this profile. */
  InstrumentationScopeInfo getInstrumentationScopeInfo();

  /** Returns the timestamp at which the profile occurred, in epoch nanos. */
  long getTimestampEpochNanos();

  /** Returns the timestamp at which the profile was observed, in epoch nanos. */
  long getObservedTimestampEpochNanos();

  /** Return the span context for this profile, or {@link SpanContext#getInvalid()} if unset. */
  SpanContext getSpanContext();

  /** Returns the attributes for this profile, or {@link Attributes#empty()} if unset. */
  Attributes getAttributes();

  /**
   * Returns the total number of attributes that were recorded on this profile.
   *
   * <p>This number may be larger than the number of attributes that are attached to this profile,
   * if the total number recorded was greater than the configured maximum value. See {@link
   * ProfileLimits#getMaxNumberOfAttributes()}.
   */
  int getTotalAttributeCount();
}
