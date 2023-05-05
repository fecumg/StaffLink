package fpt.edu.stafflink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.components.CustomNavigationComponent;
import fpt.edu.stafflink.exceptions.UnauthorizedException;
import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.response.ErrorApiResponse;
import fpt.edu.stafflink.response.RetrofitResponse.ErrorResponseHandler;
import fpt.edu.stafflink.response.RetrofitResponse.GenericResponseHandler;
import fpt.edu.stafflink.response.RetrofitResponse.MergedResponse;
import fpt.edu.stafflink.response.RetrofitResponse.MergedResponseHandler;
import fpt.edu.stafflink.response.RetrofitResponse.ResponseHandler;
import fpt.edu.stafflink.retrofit.RetrofitManager;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.ActivityUtils;
import fpt.edu.stafflink.webClient.WebClientManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import reactor.core.Disposables;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String ERROR_TAG = "BaseActivity";
    private static final String LOGOUT_ACTION = "logoutAction";

    DrawerLayout baseDrawerLayout;
    FrameLayout baseFrameLayout;
    NavigationView baseNavigationView;
    TextView baseNavigationTextViewError;
    LinearLayout baseNavigationAuthLayout;
    CustomImageComponentOval baseNavigationImageAvatar;
    TextView baseNavigationTextViewGreeting;
    ImageButton baseNavigationButtonLogout;
    CustomNavigationComponent baseNavigationComponent;
    LinearLayout baseNavigationLoginButtonLayout;

    public CompositeDisposable compositeDisposable;
    public reactor.core.Disposable.Composite reactorCompositeDisposable;

    SharedPreferences sharedPreferences;

    public MutableLiveData<List<FunctionResponse>> authorizedFunctions;
    public MutableLiveData<UserResponse> authUser;

    Toast toast;

    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();

            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            if (throwable instanceof UnauthorizedException) {
                ActivityUtils.goTo(this, getString(R.string.unauthorized_path));
            }
        });

        compositeDisposable = new CompositeDisposable();
        reactorCompositeDisposable = Disposables.composite();

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        authorizedFunctions = new MutableLiveData<>();
        authUser = new MutableLiveData<>();

        this.onSubCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base);

        baseFrameLayout = findViewById(R.id.baseFrameLayout);
        getLayoutInflater().inflate(layoutResID, baseFrameLayout, true);

        baseDrawerLayout = findViewById(R.id.baseDrawerLayout);
        baseNavigationView = findViewById(R.id.baseNavigationView);
        baseNavigationTextViewError = findViewById(R.id.baseNavigationTextViewError);
        baseNavigationAuthLayout = findViewById(R.id.baseNavigationAuthLayout);
        baseNavigationImageAvatar = findViewById(R.id.baseNavigationImageAvatar);
        baseNavigationTextViewGreeting = findViewById(R.id.baseNavigationTextViewGreeting);
        baseNavigationButtonLogout = findViewById(R.id.baseNavigationButtonLogout);
        baseNavigationComponent = findViewById(R.id.baseNavigationComponent);
        baseNavigationLoginButtonLayout = findViewById(R.id.baseNavigationLoginButtonLayout);

        baseNavigationComponent.setDrawerLayout(baseDrawerLayout);

        baseNavigationImageAvatar.registerPickImageActivityResultLauncher();

        listenToLogoutEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        baseDrawerLayout.closeDrawers();
        this.fetchAuthorizedFunctions();
        this.fetchAuthUser(this);
    }

    public void handleResponse(Response<Object> response, ResponseHandler handler, ErrorResponseHandler errorHandler) {
        Gson gson = new GsonBuilder().create();
        if (response.isSuccessful()) {
            Object resBody = response.body();
            if (resBody != null) {
                handler.handle(resBody, gson);
            }
        } else {
            if (response.code() == 401) {
                ActivityUtils.goTo(this, getString(R.string.unauthorized_path));
            }
            try (ResponseBody responseBody = response.errorBody()) {
                if (responseBody != null) {
                    ErrorApiResponse errorApiResponse = gson.fromJson(responseBody.string(), ErrorApiResponse.class);
                    errorHandler.handle(errorApiResponse);
                    this.pushToast(errorApiResponse.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(ERROR_TAG, "handleResponse: " + e.getMessage(), e);
            }
        }
    }

    public <T> void handleGenericResponse(Response<T> response, GenericResponseHandler<T> handler, ErrorResponseHandler errorHandler) {
        Gson gson = new GsonBuilder().create();
        if (response.isSuccessful()) {
            T resBody = response.body();
            handler.handle(resBody);
        } else {
            if (response.code() == 401) {
                ActivityUtils.goTo(this, getString(R.string.unauthorized_path));
            }
            try (ResponseBody responseBody = response.errorBody()) {
                if (responseBody != null) {
                    ErrorApiResponse errorApiResponse = gson.fromJson(responseBody.string(), ErrorApiResponse.class);
                    errorHandler.handle(errorApiResponse);
                    this.pushToast(errorApiResponse.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(ERROR_TAG, "handleGenericResponse: " + e.getMessage(), e);
            }
        }
    }

    public void handleMergedResponse(MergedResponse mergedResponse, MergedResponseHandler handler, ErrorResponseHandler errorHandler) {
        Gson gson = new GsonBuilder().create();
        Response<Object> first = mergedResponse.getFirst();
        Response<Object> second = mergedResponse.getSecond();

        if (first.isSuccessful() && second.isSuccessful()) {
            Object firstResBody = mergedResponse.getFirst().body();
            Object secondResBody = mergedResponse.getSecond().body();
            if (firstResBody != null && secondResBody != null) {
                handler.handle(firstResBody, secondResBody, gson);
            }
        }

        if (!first.isSuccessful() || !second.isSuccessful()) {
            if (first.code() == 401) {
                ActivityUtils.goTo(this, getString(R.string.unauthorized_path));
            }

            if (!first.isSuccessful()) {
                this.handleErrorResponse(first, gson, errorHandler);
            } else {
                this.handleErrorResponse(second, gson, errorHandler);
            }
        }
    }

    private void handleErrorResponse(Response<Object> errorResponse, Gson gson, ErrorResponseHandler errorHandler) {
        try (ResponseBody responseBody = errorResponse.errorBody()) {
            if (responseBody != null) {
                ErrorApiResponse errorApiResponse = gson.fromJson(responseBody.string(), ErrorApiResponse.class);
                errorHandler.handle(errorApiResponse);
                this.pushToast(errorApiResponse.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(ERROR_TAG, "handleErrorResponse: " + e.getMessage(), e);
        }
    }

    protected void fetchAuthorizedFunctions() {
        String bearer = this.getBearer();
        if (StringUtils.isNotEmpty(bearer.trim())) {
            Disposable disposable = RetrofitServiceManager.getAuthenticationService(this)
                    .getAuthorizedFunctions()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> this.handleResponse(
                                                response,
                                                (resBody, gson) -> {
                                                    baseNavigationComponent.setError(null);
                                                    Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                                    authorizedFunctions.setValue(gson.fromJson(gson.toJson(resBody), type));
                                                    this.setNavigationFunctions(this.authorizedFunctions.getValue());
                                                },
                                                errorResBody -> {
                                                    baseNavigationComponent.setError(errorResBody.getMessage());
                                                    this.setNavigationFunctions(new ArrayList<>());
                                                }
                            ),
                            error -> {
                                Log.e(ERROR_TAG, "fetchAuthorizedFunctions: " + error.getMessage(), error);
                                this.pushToast(error.getMessage());
                                this.setNavigationFunctions(new ArrayList<>());
                            });

            compositeDisposable.add(disposable);
        } else {
            this.setNavigationFunctions(new ArrayList<>());
        }
    }

    protected void fetchAuthUser(Context context) {
        String bearer = this.getBearer();
        if (StringUtils.isNotEmpty(bearer.trim())) {
            Disposable disposable = RetrofitServiceManager.getAuthenticationService(this)
                    .getAuthUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response ->
                                    this.handleResponse(
                                            response,
                                            (responseBody, gson) -> {
                                                baseNavigationTextViewError.setText(null);
                                                UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                                this.bindAuthInformation(context, userResponse);
                                            },
                                            errorApiResponse -> {
                                                baseNavigationTextViewError.setText(errorApiResponse.getMessage());
                                                this.bindAnonymous();
                                            }
                                    ),
                            error -> {
                                Log.e(ERROR_TAG, "fetchAuthUser: " + error.getMessage(), error);
                                this.pushToast(error.getMessage());
                                baseNavigationTextViewError.setText(error.getMessage());
                                this.bindAnonymous();
                            });

            compositeDisposable.add(disposable);
        } else {
            this.bindAnonymous();
        }
    }

    private void bindAnonymous() {
        baseNavigationLoginButtonLayout.setVisibility(View.VISIBLE);
        baseNavigationAuthLayout.setVisibility(View.GONE);

        baseNavigationLoginButtonLayout.setOnClickListener(view -> {
            baseDrawerLayout.closeDrawers();
            ActivityUtils.goTo(this, getString(R.string.login_path));
        });
    }

    private void bindAuthInformation(Context context, UserResponse userResponse) {
        this.authUser.setValue(userResponse);

        baseNavigationLoginButtonLayout.setVisibility(View.GONE);
        baseNavigationAuthLayout.setVisibility(View.VISIBLE);
        baseNavigationImageAvatar.setUrl(RetrofitManager.getThumbnailUrl(context, userResponse.getAvatar()));

        baseNavigationTextViewGreeting.setText(R.string.navigation_greeting);
        baseNavigationTextViewGreeting.append(userResponse.getName());

        baseNavigationImageAvatar.setOnTouch(() -> {
            baseDrawerLayout.closeDrawers();
            ActivityUtils.goTo(this, getString(R.string.personal_information_path));
        });

        baseNavigationButtonLogout.setOnClickListener(view -> logout());
    }

    protected void setNavigationFunctions(List<FunctionResponse> functionResponses) {
        Uri uri = ActivityUtils.getUri(this);
        runOnUiThread(() -> {
            if (uri != null) {
                baseNavigationComponent.setFunctions(functionResponses, uri.getPath());
            } else {
                baseNavigationComponent.setFunctions(functionResponses);
            }
        });
    }

    public void pushToast(String text) {
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    protected void createBearer(String jwt) {
        sharedPreferences.edit().putString(getString(R.string.authorization_sharedPreference), "Bearer " + jwt).apply();
    }

    protected void removeBearer() {
        sharedPreferences.edit().remove(getString(R.string.authorization_sharedPreference)).apply();
    }

    public String getBearer() {
        return sharedPreferences.getString(getString(R.string.authorization_sharedPreference), "");
    }

    public boolean isAuthorized(String functionPath) {
        if (this.authorizedFunctions.getValue() != null) {
            return this.authorizedFunctions.getValue().stream()
                    .anyMatch(functionResponse -> {
                        String functionUri = functionResponse.getUri().startsWith("/") ? functionResponse.getUri() : ("/" + functionResponse.getUri());
                        return functionPath.equals(functionUri);
                    });
        } else {
            return false;
        }
    }

    public UserResponse getAuthUser() {
        return this.authUser.getValue();
    }

    private void logout() {
        this.removeBearer();
        this.authUser.setValue(null);
        this.authorizedFunctions.setValue(null);

        RetrofitManager.dispose();
        WebClientManager.dispose();

        Intent logoutIntent = new Intent(LOGOUT_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(logoutIntent);

        ActivityUtils.goTo(this, getString(R.string.login_path));
    }

    private void listenToLogoutEvent() {
        if (this instanceof LoginActivity) {
            return;
        }

        System.out.println(this);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        finish();
                    }
                }, new IntentFilter(LOGOUT_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
        reactorCompositeDisposable.dispose();
    }

    protected abstract void onSubCreate(@Nullable Bundle savedInstanceState);
}
