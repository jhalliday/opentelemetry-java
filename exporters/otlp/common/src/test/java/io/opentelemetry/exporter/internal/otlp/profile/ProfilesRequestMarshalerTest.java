/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.proto.profiles.v1.ProfileContainer;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Function;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Label;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Line;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Location;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Mapping;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Profile;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Sample;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.ValueType;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.Link;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.AttributeUnit;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.AggregationTemporality;
import io.opentelemetry.sdk.profile.data.AttributeUnitData;
import io.opentelemetry.sdk.profile.data.BuildIdKind;
import io.opentelemetry.sdk.profile.data.FunctionData;
import io.opentelemetry.sdk.profile.data.LabelData;
import io.opentelemetry.sdk.profile.data.LineData;
import io.opentelemetry.sdk.profile.data.LinkData;
import io.opentelemetry.sdk.profile.data.LocationData;
import io.opentelemetry.sdk.profile.data.MappingData;
import io.opentelemetry.sdk.profile.data.ProfileContainerData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.data.SampleData;
import io.opentelemetry.sdk.profile.data.ValueTypeData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableAttributeUnitData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableFunctionData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableLabelData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableLineData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableLinkData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableLocationData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableMappingData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableProfileContainerData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableProfileData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableSampleData;
import io.opentelemetry.sdk.profile.internal.data.ImmutableValueTypeData;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;


public class ProfilesRequestMarshalerTest {

  @Test
  void compareAttributeUnitMarshaling() {
    AttributeUnitData input = ImmutableAttributeUnitData.create(1, 2);
    AttributeUnit builderResult = AttributeUnit.newBuilder()
            .setAttributeKey(1).setUnit(2).build();

    AttributeUnit roundTripResult = parse(AttributeUnit.getDefaultInstance(), AttributeUnitMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareFunctionMarshaling() {
    FunctionData input = ImmutableFunctionData.create(1, 2, 3, 4, 5);
    Function builderResult = Function.newBuilder()
        .setId(1).setName(2).setSystemName(3).setFilename(4).setStartLine(5)
        .build();

    Function roundTripResult = parse(Function.getDefaultInstance(), FunctionMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  @SuppressWarnings("deprecation") // even deprecated methods need testing
  void compareLabelMarshaling() {
    LabelData input = ImmutableLabelData.create(1, 2, 3, 4);
    Label builderResult = Label.newBuilder().setKey(1).setStr(2).setNum(3).setNumUnit(4).build();

    Label roundTripResult = parse(Label.getDefaultInstance(), LabelMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareLineMarshaling() {
    LineData input = ImmutableLineData.create(1, 2, 3);
    Line builderResult = Line.newBuilder()
        .setFunctionIndex(1).setLine(2).setColumn(3).build();

    Line roundTripResult = parse(Line.getDefaultInstance(), LineMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareLinkMarshaling() {
    LinkData input = ImmutableLinkData.create(new byte[] {1,2}, new byte[] {3,4});
    Link builderResult = Link.newBuilder()
        .setTraceId(ByteString.copyFrom(new byte[] {1,2}))
        .setSpanId(ByteString.copyFrom(new byte[] {3,4}))
        .build();

    Link roundTripResult = parse(Link.getDefaultInstance(), LinkMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareLocationMarshaling() {
    LocationData input = ImmutableLocationData.create(1, 2, 3, Collections.emptyList(), true, 4, listOf(5L,6L));
    Location builderResult = Location.newBuilder()
        .setId(1).setMappingIndex(2).setAddress(3).setIsFolded(true).setTypeIndex(4)
        .addAllAttributes(listOf(5L,6L)).build();

    Location roundTripResult = parse(Location.getDefaultInstance(), LocationMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareMappingMarshaling() {
    MappingData input = ImmutableMappingData.create(
        1, 2, 3, 4, 5,
        6, BuildIdKind.LINKER, listOf(7L,8L), true, true, true, true);
    Mapping builderResult = Mapping.newBuilder()
        .setId(1).setMemoryStart(2).setMemoryLimit(3).setFileOffset(4).setFilename(5)
        .setBuildId(6).setBuildIdKind(
            io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.BuildIdKind.BUILD_ID_LINKER)
        .addAllAttributes(listOf(7L,8L))
        .setHasFunctions(true).setHasFilenames(true).setHasLineNumbers(true).setHasInlineFrames(true)
        .build();

    Mapping roundTripResult = parse(Mapping.getDefaultInstance(), MappingMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareProfileContainerMarshaling() {
    ProfileContainerData input = ImmutableProfileContainerData.create(
        Resource.getDefault(),
        InstrumentationScopeInfo.empty(),
        new byte[] {1,2},
        3,
        4,
        Attributes.empty(),
        5,
        "format",
        new byte[] {6,7},
        ImmutableProfileData.create( // dedup
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            listOf(1L,2L),
            Collections.emptyList(),
            Attributes.empty(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            3,4,5,6,
            ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
            7,
            listOf(8L,9L),
            10
        )
    );

    ProfileContainer builderResult = ProfileContainer.newBuilder()
        .setProfileId(ByteString.copyFrom(new byte[] {1,2}))
        .setStartTimeUnixNano(3)
        .setEndTimeUnixNano(4)
        .setDroppedAttributesCount(5)
        .setOriginalPayloadFormat("format")
        .setOriginalPayload(ByteString.copyFrom(new byte[] {6,7}))
        .setProfile(
            Profile.newBuilder()
                .addAllLocationIndices(listOf(1L,2L))
                .setDropFrames(3)
                .setKeepFrames(4)
                .setTimeNanos(5)
                .setDurationNanos(6)
                .setPeriod(7)
                .setPeriodType(ValueType.newBuilder()
                    .setType(1).setUnit(2).setAggregationTemporality(
                        io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE).build())
                .addAllComment(listOf(8L,9L))
                .setDefaultSampleType(10)
                .build()
        )
        .build();

    ProfileContainer roundTripResult = parse(ProfileContainer.getDefaultInstance(), ProfileContainerMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);

  }

  @Test
  void compareProfileMarshaling() {
    ProfileData input = ImmutableProfileData.create(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        listOf(1L,2L),
        Collections.emptyList(),
        Attributes.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        3,4,5,6,
        ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
        7,
        listOf(8L,9L),
        10
    );
    Profile builderResult = Profile.newBuilder()
        .addAllLocationIndices(listOf(1L,2L))
        .setDropFrames(3)
        .setKeepFrames(4)
        .setTimeNanos(5)
        .setDurationNanos(6)
        .setPeriod(7)
        .setPeriodType(ValueType.newBuilder()
            .setType(1).setUnit(2).setAggregationTemporality(
                io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE).build())
        .addAllComment(listOf(8L,9L))
        .setDefaultSampleType(10)
        .build();

    Profile roundTripResult = parse(Profile.getDefaultInstance(), ProfileMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareSampleMarshaling() {
    SampleData input = ImmutableSampleData.create(
        listOf(1L,2L), 3, 4, 5, listOf(6L,7L),
        Collections.emptyList(),
        listOf(8L,9L), 10, listOf(11L,12L));
    Sample builderResult = Sample.newBuilder()
        .addAllLocationIndex(listOf(1L,2L))
        .setLocationsStartIndex(3)
        .setLocationsLength(4)
        .setStacktraceIdIndex(5)
        .addAllValue(listOf(6L,7L))
//        .setLabel(0, )
//        .setLabel(1, )
        .addAllAttributes(listOf(8L,9L))
        .setLink(10)
        .addAllTimestamps(listOf(11L,12L))
        .build();

    Sample roundTripResult = parse(Sample.getDefaultInstance(), SampleMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareValueTypeMarshaling() {
    ValueTypeData input = ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE);
    ValueType builderResult = ValueType.newBuilder()
            .setType(1).setUnit(2).setAggregationTemporality(
            io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
        .build();

    ValueType roundTripResult = parse(ValueType.getDefaultInstance(), ValueTypeMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }


  private static <T> List<T> listOf(T a, T b) {
    ArrayList<T> list = new ArrayList<>();
    list.add(a);
    list.add(b);
    return Collections.unmodifiableList(list);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // We don't compare JSON strings due to some differences (particularly serializing enums as
    // numbers instead of names). This may improve in the future but what matters is what we produce
    // can be parsed.
    String json = toJson(marshaler);
    Message.Builder builder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    assertThat(builder.build()).isEqualTo(result);

    return result;
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }

  private static String toJson(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }
}
