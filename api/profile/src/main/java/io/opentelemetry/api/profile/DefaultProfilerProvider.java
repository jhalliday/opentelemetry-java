/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

class DefaultProfilerProvider implements ProfilerProvider {

  private static final ProfilerProvider INSTANCE = new DefaultProfilerProvider();
  private static final ProfilerBuilder NOOP_BUILDER = new NoopProfilerBuilder();

  private DefaultProfilerProvider() {}

  static ProfilerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public ProfilerBuilder profilerBuilder(String instrumentationScopeName) {
    return NOOP_BUILDER;
  }

  private static class NoopProfilerBuilder implements ProfilerBuilder {

    @Override
    public ProfilerBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public ProfilerBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public Profiler build() {
      return DefaultProfiler.getInstance();
    }
  }
}
