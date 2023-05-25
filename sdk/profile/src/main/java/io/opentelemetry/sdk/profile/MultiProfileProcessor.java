/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link ProfileProcessor} that forwards all profile to a list of {@link
 * ProfileProcessor}s.
 */
final class MultiProfileProcessor implements ProfileProcessor {

  private final List<ProfileProcessor> profileProcessors;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Create a new {@link MultiProfileProcessor}.
   *
   * @param profileProcessorsList list of profile processors to forward profiles to
   * @return a multi profile processor instance
   */
  static ProfileProcessor create(List<ProfileProcessor> profileProcessorsList) {
    return new MultiProfileProcessor(
        new ArrayList<>(Objects.requireNonNull(profileProcessorsList, "profileProcessorsList")));
  }

  @Override
  public void onEmit(Context context, ReadWriteProfile profile) {
    for (ProfileProcessor profileProcessor : profileProcessors) {
      profileProcessor.onEmit(context, profile);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>(profileProcessors.size());
    for (ProfileProcessor profileProcessor : profileProcessors) {
      results.add(profileProcessor.shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>(profileProcessors.size());
    for (ProfileProcessor profileProcessor : profileProcessors) {
      results.add(profileProcessor.forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiProfileProcessor(List<ProfileProcessor> profileProcessorsList) {
    this.profileProcessors = profileProcessorsList;
  }
}
