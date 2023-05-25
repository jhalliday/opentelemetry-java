/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class DefaultProfilerProviderTest {

  @Test
  void noopProfilerProvider_doesNotThrow() {
    ProfilerProvider provider = ProfilerProvider.noop();

    assertThat(provider).isSameAs(DefaultProfilerProvider.getInstance());
    assertThatCode(() -> provider.get("scope-name")).doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .profilerBuilder("scope-name")
                    .setInstrumentationVersion("1.0")
                    .setSchemaUrl("http://schema.com")
                    .build())
        .doesNotThrowAnyException();

    assertThatCode(() -> provider.profilerBuilder("scope-name").build().profileBuilder())
        .doesNotThrowAnyException();
  }
}
