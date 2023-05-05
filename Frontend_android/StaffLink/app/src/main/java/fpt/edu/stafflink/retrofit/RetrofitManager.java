package fpt.edu.stafflink.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fpt.edu.stafflink.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static Retrofit retrofitInstance;

    private static Retrofit generateRetrofitInstance(Context context, String domain) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
            String bearer = sharedPreferences.getString(context.getString(R.string.authorization_sharedPreference), "");

            Request request=chain.request().newBuilder()
                    .addHeader("Authorization", bearer)
                    .build();
            return chain.proceed(request);
        }).addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(domain)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofitInstance == null) {
            retrofitInstance = generateRetrofitInstance(context, context.getString(R.string.default_domain));
        }
        return retrofitInstance;
    }

    public static String getImageUrl(Context context, String filename) {
        String domain = context.getString(R.string.default_domain);
        String imagePathPrefix = context.getString(R.string.image_path_prefix);
        return formatDomain(domain) + "/" + formatPath(imagePathPrefix) + "/" + filename;
    }

    public static String getThumbnailUrl(Context context, String filename) {
        String domain = context.getString(R.string.default_domain);
        String imagePathPrefix = context.getString(R.string.thumbnail_path_prefix);
        return formatDomain(domain) + "/" + formatPath(imagePathPrefix) + "/" + filename;
    }

    public static String getUrl(Context context, String path) {
        String domain = context.getString(R.string.default_domain);
        return formatDomain(domain) + "/" + formatPath(path);
    }

    public static String getAppUrl(Context context, String path) {
        String domain = context.getString(R.string.staff_link_domain);
        return formatDomain(domain) + "/" + formatPath(path);
    }

    private static String formatDomain(String domain) {
        return domain.endsWith("/") ? domain.substring(0, domain.length() -1) : domain;
    }

    private static String formatPath(String path) {
        if (path.startsWith("/")) {
            String stringStartedPath = path.substring(1);
            if (stringStartedPath.endsWith("/")) {
                return stringStartedPath.substring(0, stringStartedPath.length() - 1);
            } else
                return stringStartedPath;
        } else {
            if (path.endsWith("/")) {
                return path.substring(0, path.length() - 1);
            } else {
                return path;
            }
        }
    }

    public static void dispose() {
        if (retrofitInstance != null) {
            retrofitInstance = null;
        }
    }
}
