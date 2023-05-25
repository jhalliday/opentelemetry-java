/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProfileLimitsTest {

  @Test
  void defaultProfileLimits() {
    assertThat(ProfileLimits.getDefault().getMaxNumberOfAttributes()).isEqualTo(128);
    assertThat(ProfileLimits.getDefault().getMaxAttributeValueLength())
        .isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void updateProfileLimits_All() {
    ProfileLimits profileLimits =
        ProfileLimits.builder().setMaxNumberOfAttributes(8).setMaxAttributeValueLength(9).build();
    assertThat(profileLimits.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(profileLimits.getMaxAttributeValueLength()).isEqualTo(9);

    // Preserves values
    ProfileLimits profileLimitsDupe = profileLimits.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(profileLimitsDupe).usingRecursiveComparison().isEqualTo(profileLimits);
  }

  @Test
  void invalidProfileLimits() {
    assertThatThrownBy(() -> ProfileLimits.builder().setMaxNumberOfAttributes(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ProfileLimits.builder().setMaxNumberOfAttributes(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ProfileLimits.builder().setMaxAttributeValueLength(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
