/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.profile.ProfileProcessor;
import io.opentelemetry.sdk.profile.ReadWriteProfile;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link ProfileProcessor} that batches profiles exported by the SDK then
 * pushes them to the exporter pipeline.
 *
 * <p>All profiles reported by the SDK implementation are first added to a synchronized queue (with
 * a {@code maxQueueSize} maximum size, if queue is full profiles are dropped). Profiles are
 * exported either when there are {@code maxExportBatchSize} pending profiles or {@code
 * scheduleDelayNanos} has passed since the last export finished.
 */
public final class BatchProfileProcessor implements ProfileProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchProfileProcessor.class.getSimpleName() + "_WorkerThread";
  private static final AttributeKey<String> PROFILE_PROCESSOR_TYPE_LABEL =
      AttributeKey.stringKey("profileProcessorType");
  private static final AttributeKey<Boolean> PROFILE_PROCESSOR_DROPPED_LABEL =
      AttributeKey.booleanKey("dropped");
  private static final String PROFILE_PROCESSOR_TYPE_VALUE =
      BatchProfileProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchProfileProcessor}.
   *
   * @param profileExporter the {@link ProfileExporter} to which the profiles are pushed
   * @return a new {@link BatchProfileProcessor}.
   * @throws NullPointerException if the {@code profileExporter} is {@code null}.
   */
  public static BatchProfileProcessorBuilder builder(ProfileExporter profileExporter) {
    return new BatchProfileProcessorBuilder(profileExporter);
  }

  BatchProfileProcessor(
      ProfileExporter profileExporter,
      MeterProvider meterProvider,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.worker =
        new Worker(
            profileExporter,
            meterProvider,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<>(maxQueueSize)); // TODO: use JcTools.newFixedSizeQueue(..)
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
  }

  @Override
  public void onEmit(Context context, ReadWriteProfile profile) {
    if (profile == null) {
      return;
    }
    worker.addProfile(profile);
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    return worker.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return worker.forceFlush();
  }

  // Visible for testing
  ArrayList<ProfileData> getBatch() {
    return worker.batch;
  }

  @Override
  public String toString() {
    return "BatchProfileProcessor{"
        + "profileExporter="
        + worker.profileExporter
        + ", scheduleDelayNanos="
        + worker.scheduleDelayNanos
        + ", maxExportBatchSize="
        + worker.maxExportBatchSize
        + ", exporterTimeoutNanos="
        + worker.exporterTimeoutNanos
        + '}';
  }

  // Worker is a thread that batches multiple profiles and calls the registered ProfileExporter to
  // export the data.
  private static final class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private final LongCounter processedProfilesCounter;
    private final Attributes droppedAttrs;
    private final Attributes exportedAttrs;

    private final ProfileExporter profileExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<ReadWriteProfile> queue;
    // When waiting on the profiles queue, exporter thread sets this atomic to the number of more
    // profiles it needs before doing an export. Writer threads would then wait for the queue to
    // reach
    // profilesNeeded size before notifying the exporter thread about new entries.
    // Integer.MAX_VALUE is used to imply that exporter thread is not expecting any signal. Since
    // exporter thread doesn't expect any signal initially, this value is initialized to
    // Integer.MAX_VALUE.
    private final AtomicInteger profilesNeeded = new AtomicInteger(Integer.MAX_VALUE);
    private final BlockingQueue<Boolean> signal;
    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private final ArrayList<ProfileData> batch;

    private Worker(
        ProfileExporter profileExporter,
        MeterProvider meterProvider,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        Queue<ReadWriteProfile> queue) {
      this.profileExporter = profileExporter;
      this.scheduleDelayNanos = scheduleDelayNanos;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutNanos = exporterTimeoutNanos;
      this.queue = queue;
      this.signal = new ArrayBlockingQueue<>(1);
      Meter meter = meterProvider.meterBuilder("io.opentelemetry.sdk.profile").build();
      meter
          .gaugeBuilder("queueSize")
          .ofLongs()
          .setDescription("The number of profiles queued")
          .setUnit("1")
          .buildWithCallback(
              result ->
                  result.record(
                      queue.size(),
                      Attributes.of(PROFILE_PROCESSOR_TYPE_LABEL, PROFILE_PROCESSOR_TYPE_VALUE)));
      processedProfilesCounter =
          meter
              .counterBuilder("processedProfiles")
              .setUnit("1")
              .setDescription(
                  "The number of profiles processed by the BatchProfileProcessor. "
                      + "[dropped=true if they were dropped due to high throughput]")
              .build();
      droppedAttrs =
          Attributes.of(
              PROFILE_PROCESSOR_TYPE_LABEL,
              PROFILE_PROCESSOR_TYPE_VALUE,
              PROFILE_PROCESSOR_DROPPED_LABEL,
              true);
      exportedAttrs =
          Attributes.of(
              PROFILE_PROCESSOR_TYPE_LABEL,
              PROFILE_PROCESSOR_TYPE_VALUE,
              PROFILE_PROCESSOR_DROPPED_LABEL,
              false);

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addProfile(ReadWriteProfile profile) {
      if (!queue.offer(profile)) {
        processedProfilesCounter.add(1, droppedAttrs);
      } else {
        if (queue.size() >= profilesNeeded.get()) {
          signal.offer(true);
        }
      }
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        if (flushRequested.get() != null) {
          flush();
        }
        while (!queue.isEmpty() && batch.size() < maxExportBatchSize) {
          batch.add(queue.poll().toProfileData());
        }
        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
          exportCurrentBatch();
          updateNextExportTime();
        }
        if (queue.isEmpty()) {
          try {
            long pollWaitTime = nextExportTime - System.nanoTime();
            if (pollWaitTime > 0) {
              profilesNeeded.set(maxExportBatchSize - batch.size());
              signal.poll(pollWaitTime, TimeUnit.NANOSECONDS);
              profilesNeeded.set(Integer.MAX_VALUE);
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }

    private void flush() {
      int profilesToFlush = queue.size();
      while (profilesToFlush > 0) {
        ReadWriteProfile profile = queue.poll();
        assert profile != null;
        batch.add(profile.toProfileData());
        profilesToFlush--;
        if (batch.size() >= maxExportBatchSize) {
          exportCurrentBatch();
        }
      }
      exportCurrentBatch();
      CompletableResultCode flushResult = flushRequested.get();
      if (flushResult != null) {
        flushResult.succeed();
        flushRequested.set(null);
      }
    }

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    private CompletableResultCode shutdown() {
      CompletableResultCode result = new CompletableResultCode();

      CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(
          () -> {
            continueWork = false;
            CompletableResultCode shutdownResult = profileExporter.shutdown();
            shutdownResult.whenComplete(
                () -> {
                  if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
                    result.fail();
                  } else {
                    result.succeed();
                  }
                });
          });

      return result;
    }

    private CompletableResultCode forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      // we set the atomic here to trigger the worker loop to do a flush of the entire queue.
      if (flushRequested.compareAndSet(null, flushResult)) {
        signal.offer(true);
      }
      CompletableResultCode possibleResult = flushRequested.get();
      // there's a race here where the flush happening in the worker loop could complete before we
      // get what's in the atomic. In that case, just return success, since we know it succeeded in
      // the interim.
      return possibleResult == null ? CompletableResultCode.ofSuccess() : possibleResult;
    }

    private void exportCurrentBatch() {
      if (batch.isEmpty()) {
        return;
      }

      try {
        CompletableResultCode result = profileExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (result.isSuccess()) {
          processedProfilesCounter.add(batch.size(), exportedAttrs);
        } else {
          LOGGER.log(Level.FINE, "Exporter failed");
        }
      } catch (RuntimeException e) {
        LOGGER.log(Level.WARNING, "Exporter threw an Exception", e);
      } finally {
        batch.clear();
      }
    }
  }
}
