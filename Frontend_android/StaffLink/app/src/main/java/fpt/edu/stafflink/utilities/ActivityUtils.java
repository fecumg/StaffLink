package fpt.edu.stafflink.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import fpt.edu.stafflink.retrofit.RetrofitManager;

public class ActivityUtils {
    public static AppCompatActivity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (AppCompatActivity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static void goTo(Context context, String destination) {
        AppCompatActivity activity = getActivity(context);
        if (StringUtils.isNotEmpty(destination)) {
            Uri uri = Uri.parse(RetrofitManager.getAppUrl(context, destination));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        }
    }

    public static Uri getUri(Context context) {
        AppCompatActivity activity = getActivity(context);
        return activity.getIntent() != null ? activity.getIntent().getData() : null;
    }
}
