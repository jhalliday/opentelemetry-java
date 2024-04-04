/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.Location;
import io.opentelemetry.sdk.profile.data.LocationData;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class LocationMarshaler extends MarshalerWithSize {

  private static final LocationMarshaler[] EMPTY_REPEATED = new LocationMarshaler[0];

  private final long id;
  private final int mappingIndex;
  private final long address;
  private final LineMarshaler[] lineMarshalers;
  private final boolean isFolded;
  private final int typeIndex;
  private final long[] attributes;

  static LocationMarshaler create(LocationData locationData) {
    @SuppressWarnings("deprecation") // getId retained for compatibility
    LocationMarshaler locationMarshaler = new LocationMarshaler(
        locationData.getId(),
        locationData.getMappingIndex(),
        locationData.getAddress(),
        LineMarshaler.createRepeated(locationData.getLines()),
        locationData.isFolded(),
        locationData.getTypeIndex(),
        locationData.getAttributes()
    );
    return locationMarshaler;
  }

  public static LocationMarshaler[] createRepeated(List<LocationData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LocationMarshaler[] locationMarshalers = new LocationMarshaler[items.size()];
    items.forEach(item -> new Consumer<LocationData>() {
      int index = 0;

      @Override
      public void accept(LocationData locationData) {
        locationMarshalers[index++] = LocationMarshaler.create(locationData);
      }
    });
    return locationMarshalers;
  }

  private LocationMarshaler(
      long id,
      int mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      int typeIndex,
      long[] attributes) {
    super(calculateSize(
        id,
        mappingIndex,
        address,
        lineMarshalers,
        isFolded,
        typeIndex,
        attributes));
    this.id = id;
    this.mappingIndex = mappingIndex;
    this.address = address;
    this.lineMarshalers = lineMarshalers;
    this.isFolded = isFolded;
    this.typeIndex = typeIndex;
    this.attributes = attributes;
  }


  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeUInt64(Location.ID, id);
    output.serializeUInt64(Location.MAPPING_INDEX, mappingIndex);
    output.serializeUInt64(Location.ADDRESS, address);
    output.serializeRepeatedMessage(Location.LINE, lineMarshalers);
    output.serializeBool(Location.IS_FOLDED, isFolded);
    output.serializeUInt32(Location.TYPE_INDEX, typeIndex);
    output.serializeRepeatedUInt64(Location.ATTRIBUTES, attributes);
  }

  private static int calculateSize(
      long id,
      int mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      int typeIndex,
      long[] attributes
  ) {
    int size = 0;
    size += MarshalerUtil.sizeUInt64(Location.ID, id);
    size += MarshalerUtil.sizeUInt64(Location.MAPPING_INDEX, mappingIndex);
    size += MarshalerUtil.sizeUInt64(Location.ADDRESS, address);
    size += MarshalerUtil.sizeRepeatedMessage(Location.LINE, lineMarshalers);
    size += MarshalerUtil.sizeBool(Location.IS_FOLDED, isFolded);
    size += MarshalerUtil.sizeUInt32(Location.TYPE_INDEX, typeIndex);
    size += MarshalerUtil.sizeRepeatedUInt64(Location.ATTRIBUTES, attributes);
    return size;
  }
}
