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
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
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
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        HttpUrl url = ensureTrailingSlash(builder, originalRequest.url());

        HttpMethodName methodName = HttpMethodName.valueOf(originalRequest.method());
        Map<String, String> headers = getAwsHeaders(url, methodName);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return chain.proceed(builder.build());
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

    @SuppressWarnings("unchecked")
    @Nonnull
    private Map<String, String> getAwsHeaders(@Nonnull HttpUrl url, @Nonnull HttpMethodName methodName) {
        DefaultRequest awsDummyRequest = new DefaultRequest(serviceName);
        awsDummyRequest.setEndpoint(url.uri());
        awsDummyRequest.setHttpMethod(methodName);

        for (String paramName : url.queryParameterNames()) {
            awsDummyRequest.addParameter(paramName, url.queryParameter(paramName));
        }

        signer.sign(awsDummyRequest, credentialsProvider.getCredentials());

        return awsDummyRequest.getHeaders();
    }
}
