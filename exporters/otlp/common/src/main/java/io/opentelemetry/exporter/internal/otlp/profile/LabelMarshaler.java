/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.Label;
import io.opentelemetry.sdk.profile.data.LabelData;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Deprecated
final class LabelMarshaler extends MarshalerWithSize {

  private static final LabelMarshaler[] EMPTY_REPEATED = new LabelMarshaler[0];

  private final long keyIndex;
  private final long strIndex;
  private final long num;
  private final long numUnitIndex;

  @Deprecated
  static LabelMarshaler create(LabelData labelData) {
    return new LabelMarshaler(
        labelData.getKeyIndex(),
        labelData.getStrIndex(),
        labelData.getNum(),
        labelData.getNumUnitIndex()
    );
  }

  public static LabelMarshaler[] createRepeated(List<LabelData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LabelMarshaler[] labelMarshalers = new LabelMarshaler[items.size()];
    items.forEach(item -> new Consumer<LabelData>() {
      int index = 0;

      @Override
      public void accept(LabelData labelData) {
        labelMarshalers[index++] = LabelMarshaler.create(labelData);
      }
    });
    return labelMarshalers;
  }

  private LabelMarshaler(
      long keyIndex,
      long strIndex,
      long num,
      long numUnitIndex
  ) {
    super(calculateSize(keyIndex, strIndex, num, numUnitIndex));
    this.keyIndex = keyIndex;
    this.strIndex = strIndex;
    this.num = num;
    this.numUnitIndex = numUnitIndex;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(Label.KEY, keyIndex);
    output.serializeInt64(Label.STR, strIndex);
    output.serializeInt64(Label.NUM, num);
    output.serializeInt64(Label.NUM_UNIT, numUnitIndex);
  }

  private static int calculateSize(
      long keyIndex,
      long strIndex,
      long num,
      long numUnitIndex
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt64(Label.KEY, keyIndex);
    size += MarshalerUtil.sizeInt64(Label.STR, strIndex);
    size += MarshalerUtil.sizeInt64(Label.NUM, num);
    size += MarshalerUtil.sizeInt64(Label.NUM_UNIT, numUnitIndex);
    return size;
  }
}
