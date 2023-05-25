/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.profile;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.export.ProfileExporter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ProfileExporter} implementation that outputs profiles to a String Consumer. Suitable for
 * outputting profiles to System.out, loggers or other simple text sinks.
 */
public class StringWritingProfileExporter implements ProfileExporter {

  private static final Logger INTERNAL_LOGGER =
      Logger.getLogger(StringWritingProfileExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(INTERNAL_LOGGER);

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final Consumer<String> consumer;

  private final boolean fold;

  /** Returns a new {@link StringWritingProfileExporter}. */
  static StringWritingProfileExporter create(Consumer<String> consumer, boolean fold) {
    return new StringWritingProfileExporter(consumer, fold);
  }

  private StringWritingProfileExporter(Consumer<String> consumer, boolean fold) {
    this.consumer = consumer;
    this.fold = fold;
  }

  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    if (fold) {
      formatFoldedProfiles(profiles);
    } else {
      formatUnfoldedProfiles(profiles);
    }

    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static StringWritingProfileExporterBuilder builder() {
    return new StringWritingProfileExporterBuilder();
  }

  // output one String per profile
  protected void formatUnfoldedProfiles(Collection<ProfileData> profiles) {
    StringBuilder stringBuilder = new StringBuilder(60);
    for (ProfileData profile : profiles) {
      stringBuilder.setLength(0);
      formatProfile(stringBuilder, profile);
      consumer.accept(stringBuilder.toString());
    }
  }

  // output one String for each distinct profile: <profile><space><occurrence_count>
  protected void formatFoldedProfiles(Collection<ProfileData> profiles) {

    Map<String, Long> profileCounts = new LinkedHashMap<>();
    StringBuilder stringBuilder = new StringBuilder(60);
    // right now profileData doesn't have an equality semantic we can use here,
    // so we use equality of its formatted string form, which is not particularly efficient.
    for (ProfileData profileData : profiles) {
      stringBuilder.setLength(0);
      formatProfile(stringBuilder, profileData);
      String key = stringBuilder.toString();
      profileCounts.compute(key, (s, count) -> count == null ? 1 : count + 1);
    }

    for (Map.Entry<String, Long> entry : profileCounts.entrySet()) {
      stringBuilder.setLength(0);
      consumer.accept(entry.getKey() + " " + entry.getValue());
    }
  }

  protected void formatProfile(StringBuilder stringBuilder, ProfileData profile) {
    InstrumentationScopeInfo instrumentationScopeInfo = profile.getInstrumentationScopeInfo();
    stringBuilder
        .append("Profile from StringWritingProfileExporter")
        .append(": ")
        .append(profile.getSpanContext().getTraceId())
        .append(" ")
        .append(profile.getSpanContext().getSpanId())
        .append(" [scopeInfo: ")
        .append(instrumentationScopeInfo.getName())
        .append(":")
        .append(
            instrumentationScopeInfo.getVersion() == null
                ? ""
                : instrumentationScopeInfo.getVersion())
        .append("] ")
        .append(profile.getAttributes());
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return CompletableResultCode.ofSuccess();
  }
}
