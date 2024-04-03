/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

/**
 * Represents a mapping between Attribute Keys and Units.
 * @see "pprofextended.proto::AttributeUnit"
 */
public interface AttributeUnitData {

  /**
   * Index into string table.
   */
  long getAttributeKey();

  /**
   * Index into string table.
   */
  int getUnitIndex();
}
