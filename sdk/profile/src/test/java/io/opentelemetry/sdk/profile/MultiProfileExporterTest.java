/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiProfileExporterTest {

  @Mock private ProfileProcessor profileProcessor1;
  @Mock private ProfileProcessor profileProcessor2;
  @Mock private ReadWriteProfile profile;

  @BeforeEach
  void setup() {
    when(profileProcessor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(profileProcessor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(profileProcessor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(profileProcessor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void empty() {
    ProfileProcessor multiProfileProcessor = ProfileProcessor.composite();
    assertThat(multiProfileProcessor).isInstanceOf(NoopProfileProcessor.class);
    multiProfileProcessor.onEmit(Context.current(), profile);
    multiProfileProcessor.shutdown();
  }

  @Test
  void oneProfileProcessor() {
    ProfileProcessor multiProfileProcessor = ProfileProcessor.composite(profileProcessor1);
    assertThat(multiProfileProcessor).isSameAs(profileProcessor1);
  }

  @Test
  void twoProfileProcessor() {
    ProfileProcessor multiProfileProcessor =
        ProfileProcessor.composite(profileProcessor1, profileProcessor2);
    Context context = Context.current();
    multiProfileProcessor.onEmit(context, profile);
    verify(profileProcessor1).onEmit(same(context), same(profile));
    verify(profileProcessor2).onEmit(same(context), same(profile));

    multiProfileProcessor.forceFlush();
    verify(profileProcessor1).forceFlush();
    verify(profileProcessor2).forceFlush();

    multiProfileProcessor.shutdown();
    verify(profileProcessor1).shutdown();
    verify(profileProcessor2).shutdown();
  }
}
