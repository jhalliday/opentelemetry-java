/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.context.Context;

final class NoopProfileProcessor implements ProfileProcessor {
  private static final NoopProfileProcessor INSTANCE = new NoopProfileProcessor();

  static ProfileProcessor getInstance() {
    return INSTANCE;
  }

  private NoopProfileProcessor() {}

  @Override
  public void onEmit(Context context, ReadWriteProfile profile) {}
}
