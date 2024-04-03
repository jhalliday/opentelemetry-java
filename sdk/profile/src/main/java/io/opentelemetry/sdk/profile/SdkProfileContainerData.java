/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@AutoValue
@AutoValue.CopyAnnotations
@Immutable
abstract class SdkProfileData implements ProfileData {

  SdkProfileData() {}

  static SdkProfileData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      long observedEpochNanos,
      List<String> frames,
      SpanContext spanContext,
      Attributes attributes,
      int totalAttributeCount) {

    return new AutoValue_SdkProfileData(
        resource,
        instrumentationScopeInfo,
        epochNanos,
        observedEpochNanos,
        frames,
        spanContext,
        attributes,
        totalAttributeCount);
  }
}
