/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.profile;

import java.util.function.Consumer;

/** Builder for {@link StringWritingProfileExporter}s. */
public class StringWritingProfileExporterBuilder {

  @SuppressWarnings("SystemOut")
  private Consumer<String> consumer = System.out::println;

  private boolean fold = false;

  private boolean framesOnly = false;

  StringWritingProfileExporterBuilder() {}

  /**
   * Sets to String Consumer to which formatted output will be sent.
   *
   * @param consumer The sink which will receive formatted text.
   * @return this.
   */
  public StringWritingProfileExporterBuilder setConsumer(Consumer<String> consumer) {
    this.consumer = consumer;
    return this;
  }

  /**
   * Sets the output structure.
   *
   * @param fold true for folded format (one string per group with counter), false for non-folder
   *     (one string per profile)
   * @return this.
   */
  public StringWritingProfileExporterBuilder setFold(boolean fold) {
    this.fold = fold;
    return this;
  }

  /**
   * Sets the output verbosity.
   *
   * @param framesOnly true for a short format containing only the frame information, false for a
   *     more verbose format with metadata.
   * @return this.
   */
  public StringWritingProfileExporterBuilder setFramesOnly(boolean framesOnly) {
    this.framesOnly = framesOnly;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance.
   */
  public StringWritingProfileExporter build() {
    return StringWritingProfileExporter.create(consumer, fold, framesOnly);
  }
}
