/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.FunctionData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
@AutoValue.CopyAnnotations // inherit deprecations
@SuppressWarnings("deprecation") // generated code uses deprecated methods in e.g. equals()
public abstract class ImmutableFunctionData implements FunctionData {

  public static FunctionData create(
      long id,
      long nameIndex,
      long systemNameIndex,
      long filenameIndex,
      long startLine
  ) {
    return new AutoValue_ImmutableFunctionData(
        id, nameIndex, systemNameIndex, filenameIndex, startLine);
  }

  ImmutableFunctionData() {}
}
