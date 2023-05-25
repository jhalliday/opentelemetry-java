/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.profile.ProfileBuilder;
import io.opentelemetry.api.profile.Profiler;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link ProfileProcessor} is the interface to allow synchronous hooks for profiles emitted by
 * {@link Profiler}s.
 */
@ThreadSafe
public interface ProfileProcessor extends Closeable {

  /**
   * Returns a {@link ProfileProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static ProfileProcessor composite(ProfileProcessor... processors) {
    return composite(Arrays.asList(processors));
  }

  /**
   * Returns a {@link ProfileProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static ProfileProcessor composite(Iterable<ProfileProcessor> processors) {
    List<ProfileProcessor> processorList = new ArrayList<>();
    for (ProfileProcessor processor : processors) {
      processorList.add(processor);
    }
    if (processorList.isEmpty()) {
      return NoopProfileProcessor.getInstance();
    }
    if (processorList.size() == 1) {
      return processorList.get(0);
    }
    return MultiProfileProcessor.create(processorList);
  }

  /**
   * Called when a {@link Profiler} {@link ProfileBuilder#emit()}s a profile.
   *
   * @param context the context set via {@link ProfileBuilder#setContext(Context)}, or {@link
   *     Context#current()} if not explicitly set
   * @param profile the profile
   */
  void onEmit(Context context, ReadWriteProfile profile);

  /**
   * Shutdown the profile processor.
   *
   * @return result
   */
  default CompletableResultCode shutdown() {
    return forceFlush();
  }

  /**
   * Process all profiles that have not yet been processed.
   *
   * @return result
   */
  default CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Closes this {@link ProfileProcessor} after processing any remaining profiles, releasing any
   * resources.
   */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
