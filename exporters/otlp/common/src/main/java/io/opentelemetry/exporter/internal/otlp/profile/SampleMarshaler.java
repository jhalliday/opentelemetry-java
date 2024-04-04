/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.Sample;
import io.opentelemetry.sdk.profile.data.SampleData;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("deprecation") // LabelMarshaler retained for compatibility
final class SampleMarshaler extends MarshalerWithSize {

  private static final SampleMarshaler[] EMPTY_REPEATED = new SampleMarshaler[0];

  private final long[] locationIndices;
  private final long locationsStartIndex;
  private final long locationsLength;
  private final int stacktraceIdIndex;
  private final long[] values;
  private final LabelMarshaler[] labelMarshalers;
  private final long[] attributes;
  private final long link;
  private final long[] timestamps;

  static SampleMarshaler create(SampleData sampleData) {

    LabelMarshaler[] labelMarshalers = LabelMarshaler.createRepeated(sampleData.getLabels());

    return new SampleMarshaler(
        sampleData.getLocationIndices(),
        sampleData.getLocationsStartIndex(),
        sampleData.getLocationsLength(),
        sampleData.getStacktraceIdIndex(),
        sampleData.getValues(),
        labelMarshalers,
        sampleData.getAttributes(),
        sampleData.getLink(),
        sampleData.getTimestamps()
    );
  }

  public static SampleMarshaler[] createRepeated(List<SampleData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    SampleMarshaler[] sampleMarshalers = new SampleMarshaler[items.size()];
    items.forEach(item -> new Consumer<SampleData>() {
      int index = 0;

      @Override
      public void accept(SampleData sampleData) {
        sampleMarshalers[index++] = SampleMarshaler.create(sampleData);
      }
    });
    return sampleMarshalers;
  }

  private SampleMarshaler(
      long[] locationIndices,
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      long[] values,
      LabelMarshaler[] labelMarshalers,
      long[] attributes,
      long link,
      long[] timestamps) {
    super(calculateSize(
        locationIndices,
        locationsStartIndex,
        locationsLength,
        stacktraceIdIndex,
        values,
        labelMarshalers,
        attributes,
        link,
        timestamps
    ));
    this.locationIndices = locationIndices;
    this.locationsStartIndex = locationsStartIndex;
    this.locationsLength = locationsLength;
    this.stacktraceIdIndex = stacktraceIdIndex;
    this.values = values;
    this.labelMarshalers = labelMarshalers;
    this.attributes = attributes;
    this.link = link;
    this.timestamps = timestamps;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedUInt64(Sample.LOCATION_INDEX, locationIndices);
    output.serializeUInt64(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    output.serializeUInt64(Sample.LOCATIONS_LENGTH, locationsLength);
    output.serializeUInt32(Sample.STACKTRACE_ID_INDEX, stacktraceIdIndex);
    output.serializeRepeatedInt64(Sample.VALUE, values);
    output.serializeRepeatedMessage(Sample.LABEL, labelMarshalers);
    output.serializeRepeatedUInt64(Sample.ATTRIBUTES, attributes);
    output.serializeUInt64(Sample.LINK, link);
    output.serializeRepeatedUInt64(Sample.TIMESTAMPS, timestamps);
  }

  private static int calculateSize(
      long[] locationIndices,
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      long[] values,
      LabelMarshaler[] labelMarshalers,
      long[] attributes,
      long link,
      long[] timestamps
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.LOCATION_INDEX, locationIndices);
    size += MarshalerUtil.sizeUInt64(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    size += MarshalerUtil.sizeUInt64(Sample.LOCATIONS_LENGTH, locationsLength);
    size += MarshalerUtil.sizeUInt32(Sample.STACKTRACE_ID_INDEX, stacktraceIdIndex);
    size += MarshalerUtil.sizeRepeatedInt64(Sample.VALUE, values);
    size += MarshalerUtil.sizeRepeatedMessage(Sample.LABEL, labelMarshalers);
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.ATTRIBUTES, attributes);
    size += MarshalerUtil.sizeUInt64(Sample.LINK, link);
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.TIMESTAMPS, timestamps);
    return size;
  }
}
