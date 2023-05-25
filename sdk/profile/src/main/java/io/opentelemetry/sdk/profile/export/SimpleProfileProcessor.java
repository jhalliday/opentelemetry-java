/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.ProfileProcessor;
import io.opentelemetry.sdk.profile.ReadWriteProfile;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link ProfileProcessor} that passes {@link ProfileData} directly to the
 * configured exporter.
 *
 * <p>This processor will cause all profiles to be exported directly as they finish, meaning each
 * export request will have a single profiles. Most backends will not perform well with a single
 * profiles per request so unless you know what you're doing, strongly consider using {@link
 * BatchProfileProcessor} instead, including in special environments such as serverless runtimes.
 * {@link SimpleProfileProcessor} is generally meant to for testing only.
 */
public final class SimpleProfileProcessor implements ProfileProcessor {

  private static final Logger LOGGER = Logger.getLogger(SimpleProfileProcessor.class.getName());

  private final ProfileExporter profileExporter;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new {@link SimpleProfileProcessor} which exports profiles to the {@link
   * ProfileExporter} synchronously.
   *
   * <p>This processor will cause all profiles to be exported directly as they finish, meaning each
   * export request will have a single profiles. Most backends will not perform well with a single
   * profiles per request so unless you know what you're doing, strongly consider using {@link
   * BatchProfileProcessor} instead, including in special environments such as serverless runtimes.
   * {@link SimpleProfileProcessor} is generally meant to for testing only.
   */
  public static ProfileProcessor create(ProfileExporter exporter) {
    requireNonNull(exporter, "exporter");
    return new SimpleProfileProcessor(exporter);
  }

  private SimpleProfileProcessor(ProfileExporter profileExporter) {
    this.profileExporter = requireNonNull(profileExporter, "profileExporter");
  }

  @Override
  public void onEmit(Context context, ReadWriteProfile profile) {
    try {
      List<ProfileData> profiles = Collections.singletonList(profile.toProfileData());
      CompletableResultCode result = profileExporter.export(profiles);
      pendingExports.add(result);
      result.whenComplete(
          () -> {
            pendingExports.remove(result);
            if (!result.isSuccess()) {
              LOGGER.log(Level.FINE, "Exporter failed");
            }
          });
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, "Exporter threw an Exception", e);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    CompletableResultCode result = new CompletableResultCode();

    CompletableResultCode flushResult = forceFlush();
    flushResult.whenComplete(
        () -> {
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

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofAll(pendingExports);
  }

  @Override
  public String toString() {
    return "SimpleProfileProcessor{" + "profileExporter=" + profileExporter + '}';
  }
}
