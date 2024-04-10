/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.BuildIdKind;
import io.opentelemetry.sdk.profile.data.MappingData;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
@AutoValue
@AutoValue.CopyAnnotations // inherit deprecations
@SuppressWarnings("deprecation") // generated code uses deprecated methods in e.g. equals()
public abstract class ImmutableMappingData implements MappingData {

  @SuppressWarnings("TooManyParameters")
  public static MappingData create(
      long id,
      long memoryStart,
      long memoryLimit,
      long fileOffset,
      long filenameIndex,
      long buildIdIndex,
      BuildIdKind buildIdKind,
      List<Long> attributeIndices,
      boolean hasFunctions,
      boolean hasFilenames,
      boolean hasLineNumbers,
      boolean hasInlineFrames
  ) {
    return new AutoValue_ImmutableMappingData(id, memoryStart, memoryLimit, fileOffset,
        filenameIndex, buildIdIndex, buildIdKind, attributeIndices, hasFunctions,
        hasFilenames, hasLineNumbers, hasInlineFrames);
  }

  ImmutableMappingData() {}
}
