/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.util.Collection;

final class NoopProfileExporter implements ProfileExporter {

  private static final ProfileExporter INSTANCE = new NoopProfileExporter();

  static ProfileExporter getInstance() {
    return INSTANCE;
  }

  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
