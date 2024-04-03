/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

/**
 * Provides additional context for a sample,
 * such as thread ID or allocation size, with optional units.
 * @see "pprofextended.proto::Label"
 */
public interface LabelData {

  /**
   * Index into string table
   */
  int getKeyIndex();

  /**
   * String value of the label data, if applicable.
   * Index into string table
   */
  int getStrIndex();

  /**
   * Numeric value of the label data, if applicable.
   */
  long getNum();

  /**
   * Specifies the units of num, applicable only if num is present.
   * Use arbitrary string (for example, "requests") as a custom count unit.
   */
  int getNumUnitIndex();
}
