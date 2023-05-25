/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.profile.data.ProfileData;

/** Builder for {@link ProfileLimits}. */
public final class ProfileLimitsBuilder {

  private static final int DEFAULT_PROFILE_MAX_NUM_ATTRIBUTES = 128;
  private static final int DEFAULT_PROFILE_MAX_ATTRIBUTE_LENGTH = Integer.MAX_VALUE;

  private int maxNumAttributes = DEFAULT_PROFILE_MAX_NUM_ATTRIBUTES;
  private int maxAttributeValueLength = DEFAULT_PROFILE_MAX_ATTRIBUTE_LENGTH;

  ProfileLimitsBuilder() {}

  /**
   * Sets the max number of attributes per {@link ProfileData}.
   *
   * @param maxNumberOfAttributes the max number of attributes per {@link ProfileData}. Must be
   *     positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributes} is not positive.
   */
  public ProfileLimitsBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
    Utils.checkArgument(maxNumberOfAttributes > 0, "maxNumberOfAttributes must be greater than 0");
    this.maxNumAttributes = maxNumberOfAttributes;
    return this;
  }

  /**
   * Sets the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @param maxAttributeValueLength the max number of characters for attribute strings. Must not be
   *     negative.
   * @return this.
   * @throws IllegalArgumentException if {@code maxAttributeValueLength} is negative.
   */
  public ProfileLimitsBuilder setMaxAttributeValueLength(int maxAttributeValueLength) {
    Utils.checkArgument(
        maxAttributeValueLength > -1, "maxAttributeValueLength must be non-negative");
    this.maxAttributeValueLength = maxAttributeValueLength;
    return this;
  }

  /** Builds and returns a {@link ProfileLimits} with the values of this builder. */
  public ProfileLimits build() {
    return ProfileLimits.create(maxNumAttributes, maxAttributeValueLength);
  }
}
