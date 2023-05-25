/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Early stage experimental API for the profiles (i.e. CPU use, etc) signal type OpenTelemetry API.
 * Dev discussion/questions takes place via OpenTelemetry slack channel #otel-profiles. <br>
 * Naming conventions: <br>
 * package 'profile' follows 'trace' in being singular, rather than 'logs'/'metrics' plural. <br>
 * interface 'Profiler' is analogous to 'Logger', 'Tracer'. <br>
 * interface 'Profile' is analogous to 'LogRecord', 'Span'. <br>
 * Disclaimer: These names are currently straw-man proposals, not yet the result of
 * consensus-by-discussion. In particular, there is no coordination between different language SDKs
 * and the collector or other projects at this stage.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
package io.opentelemetry.api.profile;
