/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.profile.v1.internal.ScopeProfiles;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeProfileMarshaler extends MarshalerWithSize {
  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<Marshaler> profileMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeProfileMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> profileMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, profileMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.profileMarshalers = profileMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeProfiles.SCOPE, instrumentationScope);
    output.serializeRepeatedMessage(ScopeProfiles.PROFILES, profileMarshalers);
    output.serializeString(ScopeProfiles.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> profileMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeProfiles.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeProfiles.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeProfiles.PROFILES, profileMarshalers);
    return size;
  }
}
