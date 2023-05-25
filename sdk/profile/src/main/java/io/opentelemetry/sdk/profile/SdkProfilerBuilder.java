/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.profile.ProfilerBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

final class SdkProfilerBuilder implements ProfilerBuilder {

  private final ComponentRegistry<SdkProfiler> registry;
  private final String instrumentationScopeName;
  @Nullable private String instrumentationScopeVersion;
  @Nullable private String schemaUrl;

  SdkProfilerBuilder(ComponentRegistry<SdkProfiler> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.instrumentationScopeName = instrumentationScopeName;
  }

  @Override
  public SdkProfilerBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public SdkProfilerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    this.instrumentationScopeVersion = instrumentationScopeVersion;
    return this;
  }

  @Override
  public SdkProfiler build() {
    return registry.get(
        instrumentationScopeName, instrumentationScopeVersion, schemaUrl, Attributes.empty());
  }
}
