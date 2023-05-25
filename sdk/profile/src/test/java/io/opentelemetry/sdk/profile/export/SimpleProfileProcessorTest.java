/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.ProfileProcessor;
import io.opentelemetry.sdk.profile.ReadWriteProfile;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.testing.profile.TestProfileData;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleProfileProcessorTest {

  private static final ProfileData PROFILE_DATA = TestProfileData.builder().build();

  @Mock private ProfileExporter profileExporter;
  @Mock private ReadWriteProfile readWriteProfile;

  private ProfileProcessor profileProcessor;

  @BeforeEach
  void setUp() {
    profileProcessor = SimpleProfileProcessor.create(profileExporter);
    when(profileExporter.export(anyCollection())).thenReturn(CompletableResultCode.ofSuccess());
    when(profileExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(readWriteProfile.toProfileData()).thenReturn(PROFILE_DATA);
  }

  @Test
  void create_NullExporter() {
    assertThatThrownBy(() -> SimpleProfileProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void onEmit() {
    profileProcessor.onEmit(Context.current(), readWriteProfile);
    verify(profileExporter).export(Collections.singletonList(PROFILE_DATA));
  }

  @Test
  @SuppressLogger(SimpleProfileProcessor.class)
  void onEmit_ExporterError() {
    when(profileExporter.export(any())).thenThrow(new RuntimeException("Exporter error!"));
    profileProcessor.onEmit(Context.current(), readWriteProfile);
    profileProcessor.onEmit(Context.current(), readWriteProfile);
    verify(profileExporter, times(2)).export(anyList());
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(profileExporter.export(any())).thenReturn(export1, export2);

    profileProcessor.onEmit(Context.current(), readWriteProfile);
    profileProcessor.onEmit(Context.current(), readWriteProfile);

    verify(profileExporter, times(2)).export(Collections.singletonList(PROFILE_DATA));

    CompletableResultCode flush = profileProcessor.forceFlush();
    assertThat(flush.isDone()).isFalse();

    export1.succeed();
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void shutdown() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(profileExporter.export(any())).thenReturn(export1, export2);

    profileProcessor.onEmit(Context.current(), readWriteProfile);
    profileProcessor.onEmit(Context.current(), readWriteProfile);

    verify(profileExporter, times(2)).export(Collections.singletonList(PROFILE_DATA));

    CompletableResultCode shutdown = profileProcessor.shutdown();
    assertThat(shutdown.isDone()).isFalse();

    export1.succeed();
    assertThat(shutdown.isDone()).isFalse();
    verify(profileExporter, never()).shutdown();

    export2.succeed();
    assertThat(shutdown.isDone()).isTrue();
    assertThat(shutdown.isSuccess()).isTrue();
    verify(profileExporter).shutdown();
  }

  @Test
  void toString_Valid() {
    when(profileExporter.toString()).thenReturn("MockProfileExporter");
    assertThat(profileProcessor.toString())
        .isEqualTo("SimpleProfileProcessor{profileExporter=MockProfileExporter}");
  }
}
