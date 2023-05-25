/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoopProfileProcessorTest {

  @Mock private ReadWriteProfile profile;

  @Test
  void noCrash() {
    ProfileProcessor profileProcessor = NoopProfileProcessor.getInstance();
    profileProcessor.onEmit(Context.current(), profile);
    assertThat(profileProcessor.forceFlush().isSuccess()).isEqualTo(true);
    assertThat(profileProcessor.shutdown().isSuccess()).isEqualTo(true);
  }
}
