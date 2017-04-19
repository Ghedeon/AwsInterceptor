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

import android.support.annotation.NonNull;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.HttpMethodName;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AwsInterceptor implements Interceptor {

    @NonNull
    private final AWSCredentialsProvider credentialsProvider;
    @NonNull
    private final String serviceName;
    @NonNull
    private final AWS4Signer signer;

    public AwsInterceptor(@NonNull AWSCredentialsProvider credentialsProvider, @NonNull String serviceName, @NonNull String region) {
        this.credentialsProvider = credentialsProvider;
        this.serviceName = serviceName;
        signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        URI endpoint = originalRequest.url().uri();
        HttpMethodName methodName = HttpMethodName.valueOf(originalRequest.method());
        Map<String, String> headers = getAwsHeaders(endpoint, methodName);

        Request.Builder builder = originalRequest.newBuilder();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return chain.proceed(builder.build());
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private Map<String, String> getAwsHeaders(@NonNull URI endpoint, @NonNull HttpMethodName methodName) {
        DefaultRequest awsDummyRequest = new DefaultRequest(serviceName);
        awsDummyRequest.setEndpoint(endpoint);
        awsDummyRequest.setHttpMethod(methodName);

        signer.sign(awsDummyRequest, credentialsProvider.getCredentials());

        return awsDummyRequest.getHeaders();
    }
}
