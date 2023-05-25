/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.ProfileProcessor;
import io.opentelemetry.sdk.profile.SdkProfilerProvider;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An exporter is responsible for taking a collection of {@link ProfileData}s and transmitting them
 * to their ultimate destination.
 */
public interface ProfileExporter extends Closeable {

  /**
   * Returns a {@link ProfileExporter} which delegates all exports to the {@code exporters} in
   * order.
   *
   * <p>Can be used to export to multiple backends using the same {@link ProfileProcessor} like a
   * {@link SimpleProfileProcessor} or a {@link BatchProfileProcessor}.
   */
  static ProfileExporter composite(ProfileExporter... exporters) {
    return composite(Arrays.asList(exporters));
  }

  /**
   * Returns a {@link ProfileExporter} which delegates all exports to the {@code exporters} in
   * order.
   *
   * <p>Can be used to export to multiple backends using the same {@link ProfileProcessor} like a
   * {@link SimpleProfileProcessor} or a {@link BatchProfileProcessor}.
   */
  static ProfileExporter composite(Iterable<ProfileExporter> exporters) {
    List<ProfileExporter> exportersList = new ArrayList<>();
    for (ProfileExporter exporter : exporters) {
      exportersList.add(exporter);
    }
    if (exportersList.isEmpty()) {
      return NoopProfileExporter.getInstance();
    }
    if (exportersList.size() == 1) {
      return exportersList.get(0);
    }
    return MultiProfileExporter.create(exportersList);
  }

  /**
   * Exports the collections of given {@link ProfileData}.
   *
   * @param profiles the collection of {@link ProfileData} to be exported
   * @return the result of the export, which is often an asynchronous operation
   */
  CompletableResultCode export(Collection<ProfileData> profiles);

  /**
   * Exports the collection of {@link ProfileData} that have not yet been exported.
   *
   * @return the result of the flush, which is often an asynchronous operation
   */
  CompletableResultCode flush();

  /**
   * Shutdown the profile exporter. Called when {@link SdkProfilerProvider#shutdown()} is called
   * when this exporter is registered to the provider via {@link BatchProfileProcessor} or {@link
   * SimpleProfileProcessor}.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes
   */
  CompletableResultCode shutdown();

  /** Closes this {@link ProfileExporter}, releasing any resources. */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
