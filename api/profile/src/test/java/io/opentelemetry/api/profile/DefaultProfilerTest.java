/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultProfilerTest {

  @Test
  void buildAndEmit() {
    assertThatCode(
            () ->
                DefaultProfiler.getInstance()
                    .profileBuilder()
                    .setTimestamp(100, TimeUnit.SECONDS)
                    .setTimestamp(Instant.now())
                    .setObservedTimestamp(100, TimeUnit.SECONDS)
                    .setObservedTimestamp(Instant.now())
                    .setContext(Context.root())
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
  }
}
