/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.AggregationTemporality;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.ValueType;
import io.opentelemetry.sdk.profile.data.ValueTypeData;
import java.io.IOException;

final class ValueTypeMarshaler extends MarshalerWithSize {

  private final long type;
  private final long unit;
  private final ProtoEnumInfo aggregationTemporality;

  static ValueTypeMarshaler create(ValueTypeData valueTypeData) {
    ProtoEnumInfo aggregationTemporality;
    switch (valueTypeData.aggregationTemporality()) {
      case DELTA:
        aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
        break;
      case CUMULATIVE:
        aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
        break;
      case UNSPECIFIED:
      default:
        aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;
        break;
    }
    return new ValueTypeMarshaler(
        valueTypeData.type(),
        valueTypeData.unit(),
        aggregationTemporality
    );
  }

  private ValueTypeMarshaler(
      long type,
      long unit,
      ProtoEnumInfo aggregationTemporality
  ) {
    super(calculateSize(
        type,
        unit,
        aggregationTemporality));
    this.type = type;
    this.unit = unit;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(ValueType.TYPE, type);
    output.serializeInt64(ValueType.UNIT, unit);
    output.serializeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  private static int calculateSize(
      long type,
      long unit,
      ProtoEnumInfo aggregationTemporality
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt64(ValueType.TYPE, type);
    size += MarshalerUtil.sizeInt64(ValueType.UNIT, unit);
    size += MarshalerUtil.sizeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
