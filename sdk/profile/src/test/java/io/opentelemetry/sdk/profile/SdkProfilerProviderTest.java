/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static io.opentelemetry.sdk.testing.assertj.ProfileAssertions.assertThat;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.profile.ProfilerProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkProfilerProviderTest {

  @Mock private ProfileProcessor profileProcessor;

  private SdkProfilerProvider sdkProfilerProvider;

  @BeforeEach
  void setup() {
    sdkProfilerProvider =
        SdkProfilerProvider.builder()
            .setResource(Resource.empty().toBuilder().put("key", "value").build())
            .addProfileProcessor(profileProcessor)
            .build();
    when(profileProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(profileProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_defaultResource() {
    assertThat(SdkProfilerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getResource)
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void builder_resourceProvided() {
    Resource resource = Resource.create(Attributes.builder().put("key", "value").build());

    assertThat(SdkProfilerProvider.builder().setResource(resource).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getResource)
        .isEqualTo(resource);
  }

  @Test
  void builder_noProcessor() {
    assertThat(SdkProfilerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getProfileProcessor)
        .isSameAs(NoopProfileProcessor.getInstance());
  }

  @Test
  void builder_defaultProfileLimits() {
    assertThat(SdkProfilerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getProfileLimits)
        .isSameAs(ProfileLimits.getDefault());
  }

  @Test
  void builder_profileLimitsProvided() {
    ProfileLimits profileLimits =
        ProfileLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(1).build();
    assertThat(SdkProfilerProvider.builder().setProfileLimits(() -> profileLimits).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getProfileLimits)
        .isSameAs(profileLimits);
  }

  @Test
  void builder_defaultClock() {
    assertThat(SdkProfilerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getClock)
        .isSameAs(Clock.getDefault());
  }

  @Test
  void builder_clockProvided() {
    Clock clock = mock(Clock.class);
    assertThat(SdkProfilerProvider.builder().setClock(clock).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getClock)
        .isSameAs(clock);
  }

  @Test
  void builder_multipleProcessors() {
    assertThat(
            SdkProfilerProvider.builder()
                .addProfileProcessor(profileProcessor)
                .addProfileProcessor(profileProcessor)
                .build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(ProfilerSharedState.class)))
        .extracting(ProfilerSharedState::getProfileProcessor)
        .satisfies(
            activeProfileProcessor -> {
              assertThat(activeProfileProcessor).isInstanceOf(MultiProfileProcessor.class);
              assertThat(activeProfileProcessor)
                  .extracting(
                      "profileProcessors",
                      as(InstanceOfAssertFactories.list(ProfileProcessor.class)))
                  .hasSize(2);
            });
  }

  @Test
  void profilerBuilder_SameName() {
    assertThat(sdkProfilerProvider.profilerBuilder("test").build())
        .isSameAs(sdkProfilerProvider.get("test"))
        .isSameAs(sdkProfilerProvider.profilerBuilder("test").build())
        .isNotSameAs(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .build());
  }

  @Test
  void profilerBuilder_SameNameAndVersion() {
    assertThat(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .build())
        .isSameAs(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .build())
        .isNotSameAs(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void profilerBuilder_SameNameVersionAndSchema() {
    assertThat(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build())
        .isSameAs(
            sdkProfilerProvider
                .profilerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void profilerBuilder_PropagatesToProfiler() {
    InstrumentationScopeInfo expected =
        InstrumentationScopeInfo.builder("test")
            .setVersion("version")
            .setSchemaUrl("http://url")
            .build();
    assertThat(
            ((SdkProfiler)
                    sdkProfilerProvider
                        .profilerBuilder("test")
                        .setInstrumentationVersion("version")
                        .setSchemaUrl("http://url")
                        .build())
                .getInstrumentationScopeInfo())
        .isEqualTo(expected);
  }

  @Test
  void profilerBuilder_DefaultProfilerName() {
    assertThat(
            ((SdkProfiler) sdkProfilerProvider.profilerBuilder(null).build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkProfilerProvider.DEFAULT_PROFILER_NAME);

    assertThat(
            ((SdkProfiler) sdkProfilerProvider.profilerBuilder("").build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkProfilerProvider.DEFAULT_PROFILER_NAME);
  }

  @Test
  void profilerBuilder_NoProcessor_UsesNoop() {
    assertThat(SdkProfilerProvider.builder().build().profilerBuilder("test"))
        .isSameAs(ProfilerProvider.noop().profilerBuilder("test"));
  }

  @Test
  void profilerBuilder_WithProfileProcessor() {
    Resource resource = Resource.builder().put("r1", "v1").build();
    AtomicReference<ProfileData> profileData = new AtomicReference<>();
    sdkProfilerProvider =
        SdkProfilerProvider.builder()
            .setResource(resource)
            .addProfileProcessor(
                (unused, profile) -> {
                  profile.setAttribute(null, null);
                  // Overwrite k1
                  profile.setAttribute(AttributeKey.stringKey("k1"), "new-v1");
                  // Add new attribute k3
                  profile.setAttribute(AttributeKey.stringKey("k3"), "v3");
                  profileData.set(profile.toProfileData());
                })
            .build();

    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    sdkProfilerProvider
        .get("test")
        .profileBuilder()
        .setTimestamp(100, TimeUnit.NANOSECONDS)
        .setContext(Span.wrap(spanContext).storeInContext(Context.root()))
        .setAttribute(AttributeKey.stringKey("k1"), "v1")
        .setAttribute(AttributeKey.stringKey("k2"), "v2")
        .emit();

    assertThat(profileData.get())
        .hasResource(resource)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test"))
        .hasTimestamp(100)
        .hasSpanContext(spanContext)
        .hasAttributes(
            Attributes.builder().put("k1", "new-v1").put("k2", "v2").put("k3", "v3").build());
  }

  @Test
  void profilerBuilder_ProcessorWithContext() {
    ContextKey<String> contextKey = ContextKey.named("my-context-key");
    AtomicReference<ProfileData> profileData = new AtomicReference<>();

    sdkProfilerProvider =
        SdkProfilerProvider.builder()
            .addProfileProcessor(
                (context, profile) ->
                    profile.setAttribute(
                        AttributeKey.stringKey("my-context-key"),
                        Optional.ofNullable(context.get(contextKey)).orElse("")))
            .addProfileProcessor((unused, profile) -> profileData.set(profile.toProfileData()))
            .build();

    // With implicit context
    try (Scope unused = Context.current().with(contextKey, "context-value1").makeCurrent()) {
      sdkProfilerProvider.profilerBuilder("test").build().profileBuilder().emit();
    }
    assertThat(profileData.get())
        .hasAttributes(entry(AttributeKey.stringKey("my-context-key"), "context-value1"));

    // With explicit context
    try (Scope unused = Context.current().with(contextKey, "context-value2").makeCurrent()) {
      sdkProfilerProvider
          .profilerBuilder("test")
          .build()
          .profileBuilder()
          .setContext(Context.current())
          .emit();
    }
    assertThat(profileData.get())
        .hasAttributes(entry(AttributeKey.stringKey("my-context-key"), "context-value2"));
  }

  @Test
  void forceFlush() {
    sdkProfilerProvider.forceFlush();
    verify(profileProcessor).forceFlush();
  }

  @Test
  @SuppressLogger(SdkProfilerProvider.class)
  void shutdown() {
    sdkProfilerProvider.shutdown();
    sdkProfilerProvider.shutdown();
    verify(profileProcessor, times(1)).shutdown();
  }

  @Test
  void close() {
    sdkProfilerProvider.close();
    verify(profileProcessor).shutdown();
  }

  @Test
  void toString_Valid() {
    when(profileProcessor.toString()).thenReturn("MockProfileProcessor");
    assertThat(sdkProfilerProvider.toString())
        .isEqualTo(
            "SdkProfilerProvider{"
                + "clock=SystemClock{}, "
                + "resource=Resource{schemaUrl=null, attributes={key=\"value\"}}, "
                + "profileLimits=ProfileLimits{maxNumberOfAttributes=128, maxAttributeValueLength=2147483647}, "
                + "profileProcessor=MockProfileProcessor"
                + "}");
  }
}
