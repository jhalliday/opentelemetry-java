/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.profile.data.ProfileData;

/** A profile that can be read from and written to. */
public interface ReadWriteProfile {

  /**
   * Sets an attribute on the profile. If the profile previously contained a mapping for the key,
   * the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  <T> ReadWriteProfile setAttribute(AttributeKey<T> key, T value);

  // TODO: add additional setters

  /** Return an immutable {@link ProfileData} instance representing this profile. */
  ProfileData toProfileData();

  // TODO: add additional accessors.

}
