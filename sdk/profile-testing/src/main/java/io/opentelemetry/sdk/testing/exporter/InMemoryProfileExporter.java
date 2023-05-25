/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.export.ProfileExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** A {@link ProfileExporter} implementation that can be used to test OpenTelemetry integration. */
public final class InMemoryProfileExporter implements ProfileExporter {
  private final Queue<ProfileData> finishedProfileItems = new ConcurrentLinkedQueue<>();
  private boolean isStopped = false;

  private InMemoryProfileExporter() {}

  /**
   * Returns a new instance of the {@link InMemoryProfileExporter}.
   *
   * @return a new instance of the {@link InMemoryProfileExporter}.
   */
  public static InMemoryProfileExporter create() {
    return new InMemoryProfileExporter();
  }

  /**
   * Returns a {@code List} of the finished profiles.
   *
   * @return a {@code List} of the finished profiles.
   */
  public List<ProfileData> getFinishedProfileItems() {
    return Collections.unmodifiableList(new ArrayList<>(finishedProfileItems));
  }

  /**
   * Clears the internal {@code List} of finished profiles.
   *
   * <p>Does not reset the state of this exporter if already shutdown.
   */
  public void reset() {
    finishedProfileItems.clear();
  }

  /**
   * Exports the collection of profiles into the in-memory queue.
   *
   * <p>If this is called after {@code shutdown}, this will return {@code ResultCode.FAILURE}.
   */
  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    if (isStopped) {
      return CompletableResultCode.ofFailure();
    }
    finishedProfileItems.addAll(profiles);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Clears the internal {@code List} of finished profiles.
   *
   * <p>Any subsequent call to export() function on this exporter, will return {@code
   * CompletableResultCode.ofFailure()}
   */
  @Override
  public CompletableResultCode shutdown() {
    isStopped = true;
    finishedProfileItems.clear();
    return CompletableResultCode.ofSuccess();
  }
}
