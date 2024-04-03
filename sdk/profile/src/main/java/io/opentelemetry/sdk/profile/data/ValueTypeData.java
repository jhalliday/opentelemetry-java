/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

/**
 * ValueType describes the type and units of a value, with an optional aggregation temporality.
 * @see "pprofextended.proto::ValueType"
 */
public interface ValueTypeData {

  /**
   * Index into string table.
   */
  long type();

  /**
   * Index into string table.
   */
  long unit();

  AggregationTemporality aggregationTemporality();
}
