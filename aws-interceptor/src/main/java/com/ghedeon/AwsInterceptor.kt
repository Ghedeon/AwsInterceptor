/*
 *  Copyright (C) 2021 Ghedeon
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
package com.ghedeon

import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.HttpMethodName
import okhttp3.*
import okio.Buffer
import java.io.ByteArrayInputStream

@Suppress("MemberVisibilityCanBePrivate")
class AwsInterceptor(
    val credentialsProvider: AWSCredentialsProvider,
    val serviceName: String,
    val region: String
) : Interceptor {

    private val signer by lazy {
        AWS4Signer().apply {
            setServiceName(serviceName)
            setRegionName(region)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val signedRequest = chain.request().sign()
        return chain.proceed(signedRequest)
    }

    private fun Request.sign(): Request {
        val canonicalUrl = url.canonicalized()
        val awsDummyRequest = DefaultRequest<Any>(serviceName).apply {
            endpoint = canonicalUrl.toUri()
            httpMethod = HttpMethodName.valueOf(method)
            setQueryParams(canonicalUrl)
            setBody(body)
        }

        signer.sign(awsDummyRequest, credentialsProvider.credentials)

        return newBuilder()
            .url(canonicalUrl)
            .applyAwsHeaders(awsDummyRequest.headers)
            .build()
    }
}

private fun DefaultRequest<*>.setQueryParams(url: HttpUrl) {
    url.queryParameterNames.forEach { name ->
        addParameter(name, url.queryParameter(name))
    }
}

private fun DefaultRequest<*>.setBody(body: RequestBody?) {
    body ?: return
    Buffer().use {
        body.writeTo(it)
        content = ByteArrayInputStream(it.readByteArray())
        addHeader("Content-Length", body.contentLength().toString())
    }
}

private fun Request.Builder.applyAwsHeaders(headers: Map<String, String>): Request.Builder {
    headers.entries.forEach { header(it.key, it.value) }
    return this
}

private fun HttpUrl.canonicalized(): HttpUrl =
    if (pathSegments.last().isNotEmpty()) {
        newBuilder().addPathSegment("").build()
    } else this
