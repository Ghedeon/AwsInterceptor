package com.example;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.ghedeon.AwsInterceptor;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.Credentials.basic;
import static okhttp3.RequestBody.create;

public class Main {

    public static void main(String[] args) throws IOException {
        Session session = auth();

        String serviceName = ""; //SERVICE NAME;
        String region = ""; //REGION;
        String url = ""; // URL

        AWSCredentialsProvider credentialsProvider = new MyAWSCredentialsProvider(session);

        AwsInterceptor awsInterceptor = new AwsInterceptor(credentialsProvider, serviceName, region);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(awsInterceptor)
                .build();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();

        System.out.println(response.body().string());
    }

    private static Session auth() throws IOException {
        String authUrl = ""; //AUTH URL
        String userName = ""; //USERNAME
        String password = ""; //PASSWORD

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        String basic = basic(userName, password);
        RequestBody formBody = create(MediaType.parse("application/json"), "");
        Request request = new Request.Builder().url(authUrl)
                .header("Authorization", basic)
                .post(formBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();

        return new Gson().fromJson(response.body().string(), SessionResponse.class).session;
    }

    private static class MyAWSCredentialsProvider implements AWSCredentialsProvider {
        private final Session session;

        MyAWSCredentialsProvider(Session session) {
            this.session = session;
        }

        @Override
        public AWSCredentials getCredentials() {
            return new BasicSessionCredentials(session.access_key_id, session.secret_access_key, session.session_token);
        }

        @Override
        public void refresh() {

        }
    }

    private class SessionResponse {
        Session session;
    }

    private class Session {
        String access_key_id;
        String secret_access_key;
        String session_token;
        String uuid;
    }
}
