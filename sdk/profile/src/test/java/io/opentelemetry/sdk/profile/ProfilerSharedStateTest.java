/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ProfilerSharedStateTest {

  @Test
  void shutdown() {
    ProfileProcessor profileProcessor = mock(ProfileProcessor.class);
    CompletableResultCode code = new CompletableResultCode();
    when(profileProcessor.shutdown()).thenReturn(code);
    ProfilerSharedState state =
        new ProfilerSharedState(
            Resource.empty(), ProfileLimits::getDefault, profileProcessor, Clock.getDefault());
    state.shutdown();
    state.shutdown();
    verify(profileProcessor, times(1)).shutdown();
  }
}
