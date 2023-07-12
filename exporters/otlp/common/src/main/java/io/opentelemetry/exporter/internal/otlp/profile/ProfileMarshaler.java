/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profile.v1.internal.Profile;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.io.IOException;
import javax.annotation.Nullable;

final class ProfileMarshaler extends MarshalerWithSize {
  private static final String INVALID_TRACE_ID = TraceId.getInvalid();
  private static final String INVALID_SPAN_ID = SpanId.getInvalid();

  private final long timeUnixNano;
  private final long observedTimeUnixNano;
  private final KeyValueMarshaler[] attributeMarshalers;
  @Nullable private final String traceId;
  @Nullable private final String spanId;

  static ProfileMarshaler create(ProfileData profileData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(profileData.getAttributes());

    SpanContext spanContext = profileData.getSpanContext();
    return new ProfileMarshaler(
        profileData.getTimestampEpochNanos(),
        profileData.getObservedTimestampEpochNanos(),
        attributeMarshalers,
        spanContext.getTraceId().equals(INVALID_TRACE_ID) ? null : spanContext.getTraceId(),
        spanContext.getSpanId().equals(INVALID_SPAN_ID) ? null : spanContext.getSpanId());
  }

  private ProfileMarshaler(
      long timeUnixNano,
      long observedTimeUnixNano,
      KeyValueMarshaler[] attributeMarshalers,
      @Nullable String traceId,
      @Nullable String spanId) {
    super(calculateSize(timeUnixNano, observedTimeUnixNano, attributeMarshalers, traceId, spanId));
    this.timeUnixNano = timeUnixNano;
    this.observedTimeUnixNano = observedTimeUnixNano;
    this.traceId = traceId;
    this.spanId = spanId;
    this.attributeMarshalers = attributeMarshalers;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(Profile.TIME_UNIX_NANO, timeUnixNano);

    output.serializeFixed64(Profile.OBSERVED_TIME_UNIX_NANO, observedTimeUnixNano);

    output.serializeRepeatedMessage(Profile.ATTRIBUTES, attributeMarshalers);

    output.serializeTraceId(Profile.TRACE_ID, traceId);
    output.serializeSpanId(Profile.SPAN_ID, spanId);
  }

  private static int calculateSize(
      long timeUnixNano,
      long observedTimeUnixNano,
      KeyValueMarshaler[] attributeMarshalers,
      @Nullable String traceId,
      @Nullable String spanId) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(Profile.TIME_UNIX_NANO, timeUnixNano);

    size += MarshalerUtil.sizeFixed64(Profile.OBSERVED_TIME_UNIX_NANO, observedTimeUnixNano);

    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTES, attributeMarshalers);

    size += MarshalerUtil.sizeTraceId(Profile.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(Profile.SPAN_ID, spanId);
    return size;
  }
}
