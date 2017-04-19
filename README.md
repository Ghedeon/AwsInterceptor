[ ![Download](https://api.bintray.com/packages/ghedeon/maven/aws-interceptor/images/download.svg) ](https://bintray.com/ghedeon/maven/aws-interceptor/_latestVersion)

# AWS Gateway OkHttp Interceptor
An [OkHttp interceptor][1] which signs requests with [AWS Signature v4][2].

## Install
Add ``aws-interceptor`` as a dependency to your ``build.gradle`` file.
```
repositories {
    maven {
        url "http://dl.bintray.com/ghedeon/maven"
    }
}

...

dependencies {
    debugCompile 'com.ghedeon:aws-interceptor:<check badge for latest version>'
}
```
## Usage
```java
AwsInterceptor awsInterceptor = new AwsInterceptor(credentialsProvider, serviceName, region);
 
OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(awsInterceptor)
                .build();
```
check `sample` module for more details

## License
[Apache License, Version 2.0][3]

[1]: https://github.com/square/okhttp/wiki/Interceptors
[2]: http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
[3]: http://www.apache.org/licenses/LICENSE-2.0
