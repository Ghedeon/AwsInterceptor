/*
 *  Copyright (C) 2017 Ghedeon
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ghedeon;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import okhttp3.*;
import okio.Buffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;


public class AwsInterceptor implements Interceptor {

    @Nonnull
    private final AWSCredentialsProvider credentialsProvider;
    @Nonnull
    private final String serviceName;
    @Nonnull
    private final AWS4Signer signer;

    public AwsInterceptor(@Nonnull AWSCredentialsProvider credentialsProvider, @Nonnull String serviceName, @Nonnull String region) {
        this.credentialsProvider = credentialsProvider;
        this.serviceName = serviceName;
        signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
    }

    @Override
    public Response intercept(@Nonnull Chain chain) throws IOException {
        Request signedRequest = sign(chain.request());

        return chain.proceed(signedRequest);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private Request sign(@Nonnull Request request) throws IOException {
        Request.Builder builder = request.newBuilder();
        DefaultRequest awsDummyRequest = new DefaultRequest(serviceName);

        HttpUrl url = setEndpoint(builder, awsDummyRequest, request.url());

        setQueryParams(awsDummyRequest, url);

        setHttpMethod(awsDummyRequest, request.method());

        setBody(awsDummyRequest, request.body());

        signer.sign(awsDummyRequest, credentialsProvider.getCredentials());

        applyAwsHeaders(builder, awsDummyRequest.getHeaders());

        return builder.build();
    }

    @Nonnull
    private HttpUrl setEndpoint(@Nonnull Request.Builder builder, @Nonnull DefaultRequest awsRequest, @Nonnull HttpUrl url) {
        HttpUrl canonicalUrl = ensureTrailingSlash(builder, url);
        awsRequest.setEndpoint(canonicalUrl.uri());

        return canonicalUrl;
    }

    private void setQueryParams(@Nonnull DefaultRequest awsRequest, @Nonnull HttpUrl url) {
        for (String paramName : url.queryParameterNames()) {
            awsRequest.addParameter(paramName, url.queryParameter(paramName));
        }
    }

    private void setHttpMethod(@Nonnull DefaultRequest awsRequest, @Nonnull String method) {
        HttpMethodName methodName = HttpMethodName.valueOf(method);
        awsRequest.setHttpMethod(methodName);
    }

    private void setBody(@Nonnull DefaultRequest awsRequest, @Nullable RequestBody body) throws IOException {
        if (body == null) {
            return;
        }

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        awsRequest.setContent(new BufferedInputStream(buffer.inputStream()));
        awsRequest.addHeader("Content-Length", String.valueOf(body.contentLength()));
        buffer.close();
    }

    private void applyAwsHeaders(@Nonnull Request.Builder builder, @Nonnull Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
    }

    @Nonnull
    private HttpUrl ensureTrailingSlash(@Nonnull Request.Builder builder, @Nonnull HttpUrl url) {
        String lastPathSegment = url.pathSegments().get(url.pathSize() - 1);
        if (!lastPathSegment.isEmpty()) {
            url = url.newBuilder().addPathSegment("").build();
            builder.url(url);
        }

        return url;
    }

}
