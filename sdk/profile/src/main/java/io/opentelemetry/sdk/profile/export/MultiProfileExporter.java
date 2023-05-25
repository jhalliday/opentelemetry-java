/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ProfileExporter} that forwards all received profiles to a list of {@link ProfileExporter}.
 *
 * <p>Can be used to export to multiple backends using the same {@link ProfileExporter} like a
 * {@link SimpleProfileProcessor} or a {@link BatchProfileProcessor}.
 */
final class MultiProfileExporter implements ProfileExporter {
  private static final Logger LOGGER = Logger.getLogger(MultiProfileExporter.class.getName());

  private final ProfileExporter[] profileExporters;

  private MultiProfileExporter(ProfileExporter[] profileExporters) {
    this.profileExporters = profileExporters;
  }

  /**
   * Constructs and returns an instance of this class.
   *
   * @param profileExporters the exporters profiles should be sent to
   * @return the aggregate profile exporter
   */
  static ProfileExporter create(List<ProfileExporter> profileExporters) {
    return new MultiProfileExporter(profileExporters.toArray(new ProfileExporter[0]));
  }

  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    List<CompletableResultCode> results = new ArrayList<>(profileExporters.length);
    for (ProfileExporter profileExporter : profileExporters) {
      CompletableResultCode exportResult;
      try {
        exportResult = profileExporter.export(profiles);
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        LOGGER.log(Level.WARNING, "Exception thrown by the export.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(exportResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  /**
   * Flushes the data of all registered {@link ProfileExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    List<CompletableResultCode> results = new ArrayList<>(profileExporters.length);
    for (ProfileExporter profileExporter : profileExporters) {
      CompletableResultCode flushResult;
      try {
        flushResult = profileExporter.flush();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        LOGGER.log(Level.WARNING, "Exception thrown by the flush.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(flushResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode shutdown() {
    List<CompletableResultCode> results = new ArrayList<>(profileExporters.length);
    for (ProfileExporter profileExporter : profileExporters) {
      CompletableResultCode shutdownResult;
      try {
        shutdownResult = profileExporter.shutdown();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        LOGGER.log(Level.WARNING, "Exception thrown by the shutdown.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(shutdownResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public String toString() {
    return "MultiProfileExporter{" + "profileExporters=" + Arrays.toString(profileExporters) + '}';
  }
}
