package com.example

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.ghedeon.AwsInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    val serviceName = "" //SERVICE NAME;
    val region = "" //REGION;
    val url = "" // URL

    val awsInterceptor = AwsInterceptor(MyAWSCredentialsProvider(), serviceName, region)

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(awsInterceptor)
        .build()

    val request = Request.Builder().url(url).build()
    val response = okHttpClient.newCall(request).execute()

    println(response.body?.string())
}

// Sample implementation of IAM credentials provider
class MyAWSCredentialsProvider : AWSCredentialsProvider {
    private val accessKey = "" // IAM ACCESS KEY
    private val secretKey = "" // IAM SECRET KEY

    // Credentials provided by your authenticator: BasicAWSCredentials, BasicSessionCredentials, etc.
    override fun getCredentials(): AWSCredentials = BasicAWSCredentials(accessKey, secretKey)

    override fun refresh() {}
}
