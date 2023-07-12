/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profile;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.exporter.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.otlp.profile.ProfileRequestMarshaler;
import java.io.InputStream;
import javax.annotation.Nullable;

// Adapted from the protoc generated code for ProfileServiceGrpc.
final class MarshalerProfileServiceGrpc {

  private static final String SERVICE_NAME =
      "opentelemetry.proto.collector.profile.v1.ProfileService";

  private static final MethodDescriptor.Marshaller<ProfileRequestMarshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<ProfileRequestMarshaler>() {
        @Override
        public InputStream stream(ProfileRequestMarshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public ProfileRequestMarshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<ExportProfileServiceResponse>
      RESPONSE_MARSHALER =
          new MethodDescriptor.Marshaller<ExportProfileServiceResponse>() {
            @Override
            public InputStream stream(ExportProfileServiceResponse value) {
              throw new UnsupportedOperationException("Only for parsing");
            }

            @Override
            public ExportProfileServiceResponse parse(InputStream stream) {
              return ExportProfileServiceResponse.INSTANCE;
            }
          };

  private static final MethodDescriptor<ProfileRequestMarshaler, ExportProfileServiceResponse>
      getExportMethod =
          MethodDescriptor.<ProfileRequestMarshaler, ExportProfileServiceResponse>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
              .setRequestMarshaller(REQUEST_MARSHALLER)
              .setResponseMarshaller(RESPONSE_MARSHALER)
              .build();

  static ProfileServiceFutureStub newFutureStub(
      Channel channel, @Nullable String authorityOverride) {
    return ProfileServiceFutureStub.newStub(
        (c, options) -> new ProfileServiceFutureStub(c, options.withAuthority(authorityOverride)),
        channel);
  }

  static final class ProfileServiceFutureStub
      extends MarshalerServiceStub<
          ProfileRequestMarshaler, ExportProfileServiceResponse, ProfileServiceFutureStub> {
    private ProfileServiceFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MarshalerProfileServiceGrpc.ProfileServiceFutureStub build(
        Channel channel, CallOptions callOptions) {
      return new MarshalerProfileServiceGrpc.ProfileServiceFutureStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<ExportProfileServiceResponse> export(ProfileRequestMarshaler request) {
      return ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod, getCallOptions()), request);
    }
  }

  private MarshalerProfileServiceGrpc() {}
}
