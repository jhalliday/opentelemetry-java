/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.util.function.Supplier;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds limits enforced during profile handling.
 *
 * <p>Note: To allow dynamic updates of {@link ProfileLimits} you should register a {@link
 * java.util.function.Supplier} with {@link SdkProfilerProviderBuilder#setProfileLimits(Supplier)}
 * which supplies dynamic configs when queried.
 */
@AutoValue
@Immutable
public abstract class ProfileLimits {

  private static final ProfileLimits DEFAULT = new ProfileLimitsBuilder().build();

  /** Returns the default {@link ProfileLimits}. */
  public static ProfileLimits getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link ProfileLimitsBuilder} to construct a {@link ProfileLimits}. */
  public static ProfileLimitsBuilder builder() {
    return new ProfileLimitsBuilder();
  }

  static ProfileLimits create(int maxNumAttributes, int maxAttributeLength) {
    return new AutoValue_ProfileLimits(maxNumAttributes, maxAttributeLength);
  }

  ProfileLimits() {}

  /**
   * Returns the max number of attributes per {@link ProfileData}.
   *
   * @return the max number of attributes per {@link ProfileData}.
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @return the max number of characters for attribute strings.
   */
  public abstract int getMaxAttributeValueLength();

  /**
   * Returns a {@link ProfileLimitsBuilder} initialized to the same property values as the current
   * instance.
   *
   * @return a {@link ProfileLimitsBuilder} initialized to the same property values as the current
   *     instance.
   */
  public ProfileLimitsBuilder toBuilder() {
    return new ProfileLimitsBuilder()
        .setMaxNumberOfAttributes(getMaxNumberOfAttributes())
        .setMaxAttributeValueLength(getMaxAttributeValueLength());
  }
}
