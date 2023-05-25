/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

/**
 * Builder class for creating {@link Profiler} instances.
 *
 * <p>{@link Profiler}s are identified by their scope name, version, and schema URL. These
 * identifying fields, along with attributes, combine to form the instrumentation scope, which is
 * attached to all profiles produced by the {@link Profiler}.
 */
public interface ProfilerBuilder {

  /**
   * Set the scope schema URL of the resulting {@link Profiler}. Schema URL is part of {@link
   * Profiler} identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  ProfilerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link Profiler}. Version is part of
   * {@link Profiler} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  ProfilerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Profiler} instance.
   *
   * @return a {@link Profiler} instance configured with the provided options.
   */
  Profiler build();
}
