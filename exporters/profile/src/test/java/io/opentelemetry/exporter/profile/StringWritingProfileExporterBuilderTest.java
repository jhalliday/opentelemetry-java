/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class StringWritingProfileExporterBuilderTest {

  private final StringWritingProfileExporterBuilder builder =
      new StringWritingProfileExporterBuilder();

  @Test
  @SuppressWarnings("SystemOut")
  void consumerFunction() {
    Consumer<String> consumer = System.out::println;
    StringWritingProfileExporter exporter = builder.setConsumer(consumer).build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              StringWritingProfileExporter.class,
              foo -> assertThat(foo).extracting("consumer").isEqualTo(consumer));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void foldDefault() {
    StringWritingProfileExporter exporter = builder.build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              StringWritingProfileExporter.class,
              foo -> assertThat(foo).extracting("fold").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void foldOff() {
    StringWritingProfileExporter exporter = builder.setFold(false).build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              StringWritingProfileExporter.class,
              foo -> assertThat(foo).extracting("fold").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void foldOn() {
    StringWritingProfileExporter exporter = builder.setFold(true).build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              StringWritingProfileExporter.class,
              foo -> assertThat(foo).extracting("fold").isEqualTo(true));
    } finally {
      exporter.shutdown();
    }
  }
}
