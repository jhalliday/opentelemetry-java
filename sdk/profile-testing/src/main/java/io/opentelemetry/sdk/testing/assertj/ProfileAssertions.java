/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.profile.data.ProfileData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class ProfileAssertions extends Assertions {

  /** Returns an assertion for {@link ProfileData}. */
  public static ProfileDataAssert assertThat(ProfileData profileData) {
    return new ProfileDataAssert(profileData);
  }

  private ProfileAssertions() {}
}
