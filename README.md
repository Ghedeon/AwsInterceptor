[![](https://jitpack.io/v/Ghedeon/AwsInterceptor.svg)](https://jitpack.io/#Ghedeon/AwsInterceptor)

# AWS Gateway OkHttp Interceptor
An [OkHttp interceptor][1] which signs requests with [AWS Signature v4][2].

## Install
Add ``awsinterceptor`` as a dependency to your ``build.gradle`` file.
```
repositories {
    maven { url "https://jitpack.io" }
}

...

dependencies {
    implementation 'com.github.ghedeon:awsinterceptor:<check badge for latest version>'
}
```
## Usage

```kotlin
val awsInterceptor = AwsInterceptor(MyAWSCredentialsProvider(), serviceName, region)

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(awsInterceptor)
    .build()
```
check `sample` module for more details

## License
[Apache License, Version 2.0][3]

[1]: https://github.com/square/okhttp/wiki/Interceptors
[2]: http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
[3]: http://www.apache.org/licenses/LICENSE-2.0
