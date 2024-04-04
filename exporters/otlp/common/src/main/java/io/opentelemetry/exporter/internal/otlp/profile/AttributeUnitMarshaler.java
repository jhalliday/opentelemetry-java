/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.AttributeUnit;
import io.opentelemetry.sdk.profile.data.AttributeUnitData;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class AttributeUnitMarshaler extends MarshalerWithSize {

  private static final AttributeUnitMarshaler[] EMPTY_REPEATED = new AttributeUnitMarshaler[0];

  private final long attributeKey;
  private final int unitIndex;

  static AttributeUnitMarshaler create(AttributeUnitData attributeUnitData) {
    return new AttributeUnitMarshaler(
        attributeUnitData.getAttributeKey(),
        attributeUnitData.getUnitIndex()
    );
  }

  public static AttributeUnitMarshaler[] createRepeated(List<AttributeUnitData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    AttributeUnitMarshaler[] attributeUnitMarshalers = new AttributeUnitMarshaler[items.size()];
    items.forEach(item -> new Consumer<AttributeUnitData>() {
      int index = 0;

      @Override
      public void accept(AttributeUnitData attributeUnitData) {
        attributeUnitMarshalers[index++] = AttributeUnitMarshaler.create(attributeUnitData);
      }
    });
    return attributeUnitMarshalers;
  }

  private AttributeUnitMarshaler(
      long attributeKey,
      int unitIndex
  ) {
    super(calculateSize(attributeKey, unitIndex));
    this.attributeKey = attributeKey;
    this.unitIndex = unitIndex;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(AttributeUnit.ATTRIBUTE_KEY, attributeKey);
    output.serializeInt64(AttributeUnit.UNIT, unitIndex);
  }

  private static int calculateSize(
      long attributeKey,
      int unitIndex
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt64(AttributeUnit.ATTRIBUTE_KEY, attributeKey);
    size += MarshalerUtil.sizeInt64(AttributeUnit.UNIT, unitIndex);
    return size;
  }
}
