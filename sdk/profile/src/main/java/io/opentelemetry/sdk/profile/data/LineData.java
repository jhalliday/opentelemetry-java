/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

/**
 * Details a specific line in a source code, linked to a function.
 * @see "pprofextended.proto::Line"
 */
public interface LineData {

  /**
   * The index of the corresponding Function for this line.
   * Index into function table.
   */
  int getFunctionIndex();

  /**
   * Line number in source code.
   */
  int getLine();

  /**
   * Column number in source code.
   */
  int getColumn();
}
