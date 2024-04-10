/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profiles.v1.internal.ProfileContainer;
import io.opentelemetry.sdk.profile.data.ProfileContainerData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ProfileContainerMarshaler extends MarshalerWithSize {

  private final byte[] profileId;
  private final long startTimeUnixNano;
  private final long endTimeUnixNano;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final byte[] originalPayloadFormatUtf8;
  private final byte[] originalPayload;
  private final ProfileMarshaler profileMarshaler;

  static ProfileContainerMarshaler create(ProfileContainerData profileContainerData) {
    return new ProfileContainerMarshaler(
        profileContainerData.getProfileId(),
        profileContainerData.getStartTimeUnixNano(),
        profileContainerData.getEndTimeUnixNano(),
        // TODO order
        KeyValueMarshaler.createRepeated(profileContainerData.getAttributes()),
        profileContainerData.getDroppedAttributesCount(),
        profileContainerData.getOriginalPayloadFormat() != null ? profileContainerData.getOriginalPayloadFormat().getBytes(StandardCharsets.UTF_8) :  new byte[0],
        profileContainerData.getOriginalPayload() != null ? profileContainerData.getOriginalPayload() : new byte[0],
        ProfileMarshaler.create(profileContainerData.getProfile())
    );
  }

  private ProfileContainerMarshaler(
      byte[] profileId,
      long startTimeUnixNano,
      long endTimeUnixNano,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      byte[] originalPayload,
      ProfileMarshaler profileMarshaler
  ) {
    super(calculateSize(
        profileId,
        startTimeUnixNano,
        endTimeUnixNano,
        attributeMarshalers,
        droppedAttributesCount,
        originalPayloadFormat,
        originalPayload,
        profileMarshaler
    ));
    this.profileId = profileId;
    this.startTimeUnixNano = startTimeUnixNano;
    this.endTimeUnixNano = endTimeUnixNano;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
    this.originalPayloadFormatUtf8 = originalPayloadFormat;
    this.originalPayload = originalPayload;
    this.profileMarshaler = profileMarshaler;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeBytes(ProfileContainer.PROFILE_ID, profileId);
    output.serializeFixed64(ProfileContainer.START_TIME_UNIX_NANO, startTimeUnixNano);
    output.serializeFixed64(ProfileContainer.END_TIME_UNIX_NANO, endTimeUnixNano);
    output.serializeRepeatedMessage(ProfileContainer.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(ProfileContainer.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeString(ProfileContainer.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormatUtf8);
    output.serializeBytes(ProfileContainer.ORIGINAL_PAYLOAD, originalPayload);
    output.serializeMessage(ProfileContainer.PROFILE, profileMarshaler);
  }

  private static int calculateSize(
      byte[] profileId,
      long startTimeUnixNano,
      long endTimeUnixNano,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      byte[] originalPayload,
      ProfileMarshaler profileMarshaler
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeBytes(ProfileContainer.PROFILE_ID, profileId);
    size += MarshalerUtil.sizeFixed64(ProfileContainer.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(ProfileContainer.END_TIME_UNIX_NANO, endTimeUnixNano);
    size += MarshalerUtil.sizeRepeatedMessage(ProfileContainer.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeUInt32(ProfileContainer.DROPPED_ATTRIBUTES_COUNT,
        droppedAttributesCount);
    size += MarshalerUtil.sizeBytes(ProfileContainer.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormat);
    size += MarshalerUtil.sizeBytes(ProfileContainer.ORIGINAL_PAYLOAD, originalPayload);
    size += MarshalerUtil.sizeMessage(ProfileContainer.PROFILE, profileMarshaler);
    return size;
  }
}
