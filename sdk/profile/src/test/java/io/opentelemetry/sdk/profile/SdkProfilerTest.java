/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.ProfileAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.profile.ProfileBuilder;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkProfilerTest {

  @Test
  void profileBuilder() {
    ProfilerSharedState state = mock(ProfilerSharedState.class);
    InstrumentationScopeInfo info = InstrumentationScopeInfo.create("foo");
    AtomicReference<ReadWriteProfile> seenProfile = new AtomicReference<>();
    ProfileProcessor profileProcessor = (context, profile) -> seenProfile.set(profile);
    Clock clock = mock(Clock.class);

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getProfileProcessor()).thenReturn(profileProcessor);
    when(state.getClock()).thenReturn(clock);

    SdkProfiler profiler = new SdkProfiler(state, info);
    ProfileBuilder profileBuilder = profiler.profileBuilder();

    // Have to test through the builder
    profileBuilder.emit();
    assertThat(seenProfile.get().toProfileData()).hasTimestamp(0);
  }

  @Test
  void profileBuilder_maxAttributeLength() {
    int maxLength = 25;
    AtomicReference<ReadWriteProfile> seenProfile = new AtomicReference<>();
    SdkProfilerProvider profilerProvider =
        SdkProfilerProvider.builder()
            .addProfileProcessor((context, profile) -> seenProfile.set(profile))
            .setProfileLimits(
                () -> ProfileLimits.builder().setMaxAttributeValueLength(maxLength).build())
            .build();
    ProfileBuilder profileBuilder = profilerProvider.get("test").profileBuilder();
    String strVal = StringUtils.padLeft("", maxLength);
    String tooLongStrVal = strVal + strVal;

    profileBuilder
        .setAllAttributes(
            Attributes.builder()
                .put("string", tooLongStrVal)
                .put("boolean", true)
                .put("long", 1L)
                .put("double", 1.0)
                .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
                .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
                .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
                .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
                .build())
        .emit();

    Attributes attributes = seenProfile.get().toProfileData().getAttributes();

    assertThat(attributes)
        .containsEntry("string", strVal)
        .containsEntry("boolean", true)
        .containsEntry("long", 1L)
        .containsEntry("double", 1.0)
        .containsEntry("stringArray", strVal, strVal)
        .containsEntry("booleanArray", true, false)
        .containsEntry("longArray", 1L, 2L)
        .containsEntry("doubleArray", 1.0, 2.0);
  }

  @Test
  void profileBuilder_maxAttributes() {
    int maxNumberOfAttrs = 8;
    AtomicReference<ReadWriteProfile> seenProfile = new AtomicReference<>();
    SdkProfilerProvider profilerProvider =
        SdkProfilerProvider.builder()
            .addProfileProcessor((context, profile) -> seenProfile.set(profile))
            .setProfileLimits(
                () -> ProfileLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttrs).build())
            .build();

    ProfileBuilder builder = profilerProvider.get("test").profileBuilder();
    AttributesBuilder expectedAttributes = Attributes.builder();
    for (int i = 0; i < 2 * maxNumberOfAttrs; i++) {
      AttributeKey<Long> key = AttributeKey.longKey("key" + i);
      builder.setAttribute(key, (long) i);
      if (i < maxNumberOfAttrs) {
        expectedAttributes.put(key, (long) i);
      }
    }
    builder.emit();

    assertThat(seenProfile.get().toProfileData())
        .hasAttributes(expectedAttributes.build())
        .hasTotalAttributeCount(maxNumberOfAttrs * 2);
  }

  @Test
  void profileBuilder_AfterShutdown() {
    ProfileProcessor profileProcessor = mock(ProfileProcessor.class);
    when(profileProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    SdkProfilerProvider profilerProvider =
        SdkProfilerProvider.builder().addProfileProcessor(profileProcessor).build();

    profilerProvider.shutdown().join(10, TimeUnit.SECONDS);
    profilerProvider.get("test").profileBuilder().emit();

    verify(profileProcessor, never()).onEmit(any(), any());
  }
}
