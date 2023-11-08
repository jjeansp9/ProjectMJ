package kr.jeet.edu.manager.server;

import java.util.concurrent.TimeUnit;

import kr.jeet.edu.manager.BuildConfig;
import kr.jeet.edu.manager.utils.LogMgr;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private static RetrofitApi retrofitApi;
    private static RetrofitApi longTimeRetrofitApi;

    private RetrofitClient() {

        // 로그를 보기 위한 interceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        OkHttpClient LongTimeClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = null;
        Retrofit longTimeRetrofit = null;
        if(BuildConfig.DEBUG) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX)   // 꼭 / 로 끝나야함
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client) // 로그 기능 추가
                    .build();

        } else {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX)   // 꼭 / 로 끝나야함
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        longTimeRetrofit = new Retrofit.Builder()
                .baseUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX)   // 꼭 / 로 끝나야함
                .addConverterFactory(GsonConverterFactory.create())
                .client(LongTimeClient) // 로그 기능 추가
                .build();
        retrofitApi = retrofit.create(RetrofitApi.class);
        longTimeRetrofitApi = longTimeRetrofit.create(RetrofitApi.class);
    }

    public static RetrofitClient getInstance() {
        if(instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public static RetrofitApi getApiInterface() {
        return retrofitApi;
    }
    public static RetrofitApi getLongTimeApiInterface() {
        return longTimeRetrofitApi;
    }
}
