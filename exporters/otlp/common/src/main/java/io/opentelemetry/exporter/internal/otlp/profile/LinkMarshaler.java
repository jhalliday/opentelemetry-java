/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.Link;
import io.opentelemetry.sdk.profile.data.LinkData;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class LinkMarshaler extends MarshalerWithSize {

  private static final LinkMarshaler[] EMPTY_REPEATED = new LinkMarshaler[0];

  private final byte[] traceId;
  private final byte[] spanId;

  static LinkMarshaler create(LinkData linkData) {
    LinkMarshaler linkMarshaler = new LinkMarshaler(
        linkData.getTraceId(), linkData.getSpanId());
    return linkMarshaler;
  }

  public static LinkMarshaler[] createRepeated(List<LinkData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LinkMarshaler[] linkMarshalers = new LinkMarshaler[items.size()];
    items.forEach(item -> new Consumer<LinkData>() {
      int index = 0;

      @Override
      public void accept(LinkData linkData) {
        linkMarshalers[index++] = LinkMarshaler.create(linkData);
      }
    });
    return linkMarshalers;
  }

  private LinkMarshaler(
      byte[] traceId,
      byte[] spanId
  ) {
    super(calculateSize(traceId, spanId));
    this.traceId = traceId;
    this.spanId = spanId;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeBytes(Link.TRACE_ID, traceId);
    output.serializeBytes(Link.SPAN_ID, spanId);
  }

  private static int calculateSize(
      byte[] traceId,
      byte[] spanId
  ) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(Link.TRACE_ID, traceId);
    size += MarshalerUtil.sizeBytes(Link.SPAN_ID, spanId);
    return size;
  }
}
