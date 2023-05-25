/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/** Test assertions for {@link ProfileData}. */
public class ProfileDataAssert extends AbstractAssert<ProfileDataAssert, ProfileData> {
  protected ProfileDataAssert(ProfileData actual) {
    super(actual, ProfileDataAssert.class);
  }

  /** Asserts the {@link Resource} associated with a profile matches the expected value. */
  public ProfileDataAssert hasResource(Resource resource) {
    isNotNull();
    if (!actual.getResource().equals(resource)) {
      failWithActualExpectedAndMessage(
          actual,
          "resource: " + resource,
          "Expected profile to have resource <%s> but found <%s>",
          resource,
          actual.getResource());
    }
    return this;
  }

  /**
   * Asserts the {@link InstrumentationScopeInfo} associated with a profile matches the expected
   * value.
   */
  public ProfileDataAssert hasInstrumentationScope(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    isNotNull();
    if (!actual.getInstrumentationScopeInfo().equals(instrumentationScopeInfo)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation scope: " + instrumentationScopeInfo,
          "Expected profile to have scope <%s> but found <%s>",
          instrumentationScopeInfo,
          actual.getInstrumentationScopeInfo());
    }
    return this;
  }

  /** Asserts the profile has the given epoch {@code timestamp}. */
  public ProfileDataAssert hasTimestamp(long timestampEpochNanos) {
    isNotNull();
    if (actual.getTimestampEpochNanos() != timestampEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getTimestampEpochNanos(),
          timestampEpochNanos,
          "Expected profile to have timestamp <%s> nanos but was <%s>",
          timestampEpochNanos,
          actual.getTimestampEpochNanos());
    }
    return this;
  }

  /** Asserts the profile has the given epoch {@code observedTimestamp}. */
  public ProfileDataAssert hasObservedTimestamp(long observedEpochNanos) {
    isNotNull();
    if (actual.getObservedTimestampEpochNanos() != observedEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getObservedTimestampEpochNanos(),
          observedEpochNanos,
          "Expected profile to have observed timestamp <%s> nanos but was <%s>",
          observedEpochNanos,
          actual.getObservedTimestampEpochNanos());
    }
    return this;
  }

  /** Asserts the profile has the given span context. */
  public ProfileDataAssert hasSpanContext(SpanContext spanContext) {
    isNotNull();
    if (!actual.getSpanContext().equals(spanContext)) {
      failWithActualExpectedAndMessage(
          actual.getSpanContext(),
          spanContext,
          "Expected profile to have span context <%s> nanos but was <%s>",
          spanContext,
          actual.getSpanContext());
    }
    return this;
  }

  /** Asserts the profile has the given attributes. */
  public ProfileDataAssert hasAttributes(Attributes attributes) {
    isNotNull();
    if (!attributesAreEqual(attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected profile to have attributes <%s> but was <%s>",
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the profile has the given attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final ProfileDataAssert hasAttributes(Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasAttributes(attributes);
  }

  /** Asserts the profile has attributes satisfying the given condition. */
  public ProfileDataAssert hasAttributesSatisfying(Consumer<Attributes> attributes) {
    isNotNull();
    assertThat(actual.getAttributes()).as("attributes").satisfies(attributes);
    return this;
  }

  /**
   * Asserts the profile has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ProfileDataAssert hasAttributesSatisfying(AttributeAssertion... assertions) {
    return hasAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the profile has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ProfileDataAssert hasAttributesSatisfying(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getAttributes(), assertions);
    return myself;
  }

  /**
   * Asserts the profile has attributes matching all {@code assertions} and no more. Assertions can
   * be created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ProfileDataAssert hasAttributesSatisfyingExactly(AttributeAssertion... assertions) {
    return hasAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the profile has attributes matching all {@code assertions} and no more. Assertions can
   * be created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ProfileDataAssert hasAttributesSatisfyingExactly(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getAttributes(), assertions);
    return myself;
  }

  private boolean attributesAreEqual(Attributes attributes) {
    // compare as maps, since implementations do not have equals that work correctly across
    // implementations.
    return actual.getAttributes().asMap().equals(attributes.asMap());
  }

  /** Asserts the profile has the given total attributes. */
  public ProfileDataAssert hasTotalAttributeCount(int totalAttributeCount) {
    isNotNull();
    if (actual.getTotalAttributeCount() != totalAttributeCount) {
      failWithActualExpectedAndMessage(
          actual.getTotalAttributeCount(),
          totalAttributeCount,
          "Expected profile to have recorded <%s> total attributes but did not",
          totalAttributeCount);
    }
    return this;
  }
}
