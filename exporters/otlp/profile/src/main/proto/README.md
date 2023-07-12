
The `profile.proto` in this directory is NOT directly consumed by the build process.
It's here for information only.

The `opentelemetry-java` project build gets the OTLP `.proto` files
and some of the stubs from its dependency on `opentelemetry-proto-java`
see
`exporters/otlp/common/build.gradle.kts` and `dependencyManagement/build.gradle.kts`

The canonical `.proto` SHOULD live in the [opentelemetry-proto repository](https://github.com/open-telemetry/opentelemetry-proto/)
and get packaged to a `.jar` by tooling in [opentelemetry-proto-java repository](https://github.com/open-telemetry/opentelemetry-proto-java)

This is incredibly painful to do with early stage `.proto` files, as the `opentelemetry-proto`
repo won't accept files without a spec and we don't get a spec draft until we have the files defined.

To square this circle, there is a temporary fork of `opentelemetry-proto` at [opentelemetry-proto-profile repository](https://github.com/open-telemetry/opentelemetry-proto-profile)
which at least allows sharing of the `.proto` files.

What it doesn't do is help with building the proto `.jar` or publishing it at maven coordinates that distinguish it from the mainline one,
so there is still no standard way to get `opentelemetry-java` to consume it.

Likewise, there is not any way of then publishing the resulting modified `opentelemetry-java` build either.
For those steps we'd need forked `opentelemetry-proto-java` and `opentelemetry-java` repositories that used different maven coordinates.
