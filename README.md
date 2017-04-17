#AWS Gateway OkHttp Interceptor
An [OkHttp interceptor][1] which signs requests with aws signature according to [Signature Version 4 Signing Process][2].

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
    debugCompile 'com.ghedeon:aws-interceptor:0.1'
}
```
##Usage
```
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
