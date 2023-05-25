/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.testing.profile.TestProfileData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class StringWritingProfileExporterTest {

  private StringWritingProfileExporter exporter;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(StringWritingProfileExporter.class);

  @Test
  void exportUnfolded() {

    List<String> exportedLines = new ArrayList<>();
    StringWritingProfileExporter exporter =
        StringWritingProfileExporter.builder()
            .setFold(false)
            .setConsumer(exportedLines::add)
            .build();

    List<ProfileData> profiles = new ArrayList<>();
    TestProfileData profileOne =
        TestProfileData.builder()
            .setAttributes(Attributes.of(AttributeKey.stringKey("test"), "ProfileOne"))
            .build();
    profiles.add(profileOne);
    TestProfileData profileTwo =
        TestProfileData.builder()
            .setAttributes(Attributes.of(AttributeKey.stringKey("test"), "ProfileTwo"))
            .build();
    profiles.add(profileTwo);

    try {
      exporter.export(profiles);
    } finally {
      exporter.shutdown();
    }

    assertEquals(profiles.size(), exportedLines.size());
    assertThat(exportedLines.get(0).contains("ProfileOne")).isTrue();
    assertThat(exportedLines.get(1).contains("ProfileTwo")).isTrue();
  }

  @Test
  void exportFolded() {

    List<String> exportedLines = new ArrayList<>();
    StringWritingProfileExporter exporter =
        StringWritingProfileExporter.builder()
            .setFold(true)
            .setConsumer(exportedLines::add)
            .build();

    List<ProfileData> profiles = new ArrayList<>();
    TestProfileData profileA =
        TestProfileData.builder()
            .setAttributes(Attributes.of(AttributeKey.stringKey("test"), "ProfileA"))
            .build();
    profiles.add(profileA);
    TestProfileData profileB =
        TestProfileData.builder()
            .setAttributes(Attributes.of(AttributeKey.stringKey("test"), "ProfileB"))
            .build();
    profiles.add(profileB);
    profiles.add(profileB); // add twice

    try {
      exporter.export(profiles);
    } finally {
      exporter.shutdown();
    }

    assertEquals(2, exportedLines.size());
    assertThat(exportedLines.get(0).contains("ProfileA")).isTrue();
    assertThat(exportedLines.get(0).endsWith(" 1")).isTrue();
    assertThat(exportedLines.get(1).contains("ProfileB")).isTrue();
    assertThat(exportedLines.get(1).endsWith(" 2")).isTrue();
  }

  @Test
  @SuppressWarnings("SystemOut")
  void shutdown() {

    exporter = StringWritingProfileExporter.create(System.out::println, false);

    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(exporter.export(Collections.emptyList()).join(10, TimeUnit.SECONDS).isSuccess())
        .isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
