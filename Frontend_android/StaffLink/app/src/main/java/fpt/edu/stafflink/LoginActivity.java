package fpt.edu.stafflink;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import fpt.edu.stafflink.components.CustomButtonComponent;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.userRequestDtos.LoginRequest;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.ActivityUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class LoginActivity extends BaseActivity {

    private static final String ERROR_TAG = "LoginActivity";

    TextView textViewError;

    CustomInputTextComponent inputTextCredentialUsername;
    CustomInputTextComponent inputTextCredentialPassword;
    CustomButtonComponent buttonLogin;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);

        textViewError = findViewById(R.id.textViewError);

        inputTextCredentialUsername = findViewById(R.id.inputTextCredentialUsername);
        inputTextCredentialPassword = findViewById(R.id.inputTextCredentialPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        this.initiate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputTextCredentialUsername.setText(null);
        inputTextCredentialUsername.clearFocus();

        inputTextCredentialPassword.setText(null);
        inputTextCredentialPassword.clearFocus();
    }

    private void initiate() {
        if (StringUtils.isEmpty(super.getBearer().trim())) {
            buttonLogin.setOnClick(view -> {
                LoginRequest loginRequest = this.validateLoginRequest();
                if (loginRequest != null) {
                    RequestBody loginRequestBody = this.buildLoginRequestBody(loginRequest);
                    this.submitLogin(loginRequestBody);
                }
            });
        } else {
            ActivityUtils.goTo(this, getString(R.string.personal_information_path));
        }
    }

    private LoginRequest validateLoginRequest() {
        String username = inputTextCredentialUsername.getText().toString().trim();
        String password = inputTextCredentialPassword.getText().toString().trim();

        if (StringUtils.isEmpty(username)) {
            inputTextCredentialUsername.setError("username cannot be empty");
            inputTextCredentialUsername.requestFocus();
            return null;
        } else {
            inputTextCredentialUsername.setError(null);
        }

        if (StringUtils.isEmpty(password)) {
            inputTextCredentialPassword.setError("password cannot be empty");
            inputTextCredentialPassword.requestFocus();
            return null;
        } else if (password.length() < 6){
            inputTextCredentialPassword.setError("password must exceed 5 characters");
            inputTextCredentialPassword.requestFocus();
            return null;
        } else {
            inputTextCredentialPassword.setError(null);
        }

        return new LoginRequest(username, password);
    }

    private RequestBody buildLoginRequestBody(LoginRequest loginRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("username", loginRequest.getUsername());
        builder.addFormDataPart("password", loginRequest.getPassword());

        return builder.build();
    }

    private void submitLogin(RequestBody loginRequestBody) {
        Disposable disposable = RetrofitServiceManager.getAuthenticationService(this)
                .login(loginRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> super.handleResponse(
                                response,
                                (resBody, gson) -> {
                                    textViewError.setText(null);
                                    pushToast("Welcome!");
                                    String jwt = resBody.toString();
                                    super.createBearer(jwt);
                                    ActivityUtils.goTo(this, getString(R.string.personal_information_path));
                                },
                                errorResBody -> textViewError.setText(errorResBody.getMessage())),
                        error -> {
                            Log.e(ERROR_TAG, "submitLogin: " + error.getMessage(), error);
                            textViewError.setText(error.getMessage());
                            super.pushToast(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }
}