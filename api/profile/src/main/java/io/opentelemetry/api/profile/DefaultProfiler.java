/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultProfiler implements Profiler {

  private static final Profiler INSTANCE = new DefaultProfiler();
  private static final ProfileBuilder NOOP_PROFILE_BUILDER = new NoopProfileBuilder();

  private DefaultProfiler() {}

  static Profiler getInstance() {
    return INSTANCE;
  }

  @Override
  public ProfileBuilder profileBuilder() {
    return NOOP_PROFILE_BUILDER;
  }

  private static final class NoopProfileBuilder implements ProfileBuilder {

    private NoopProfileBuilder() {}

    @Override
    public ProfileBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public ProfileBuilder setTimestamp(Instant instant) {
      return this;
    }

    @Override
    public ProfileBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public ProfileBuilder setObservedTimestamp(Instant instant) {
      return this;
    }

    @Override
    public ProfileBuilder setContext(Context context) {
      return this;
    }

    @Override
    public <T> ProfileBuilder setAttribute(AttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
