package fpt.edu.stafflink;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;

import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.retrofit.RetrofitManager;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.ActivityUtils;
import fpt.edu.stafflink.utilities.ValidationUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PersonalInformationActivity extends BaseActivity {
    private static final String ERROR_TAG = "PersonalInformationActivity";

    ImageButton buttonSubmitEdit;
    TextView textViewError;
    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextUsername;
    CustomInputTextComponent inputTextAddress;
    CustomInputTextComponent inputTextPhone;
    CustomInputTextComponent inputTextEmail;
    CustomImageComponentOval imageAvatar;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_information);

        buttonSubmitEdit = findViewById(R.id.buttonSubmitEdit);

        textViewError = findViewById(R.id.textViewError);

        inputTextName = findViewById(R.id.inputTextName);
        inputTextUsername = findViewById(R.id.inputTextUsername);
        inputTextAddress = findViewById(R.id.inputTextAddress);
        inputTextPhone = findViewById(R.id.inputTextPhone);
        inputTextEmail = findViewById(R.id.inputTextEmail);
        imageAvatar = findViewById(R.id.imageAvatar);

        this.initiate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.initiate();
    }

    private void initiate() {
        this.fetchAuthUser(this);

        buttonSubmitEdit.setOnClickListener(view -> {
            EditUserRequest editUserRequest = this.validateEditRequest();
            if (editUserRequest != null) {
                RequestBody editUserRequestBody = this.buildEditUserRequestBody(editUserRequest);
                this.submitEdit(this, editUserRequestBody);
            }
        });
    }

    private void fetchAuthUser(Context context) {
        String bearer = super.getBearer();
        if (StringUtils.isNotEmpty(bearer.trim())) {
            Disposable disposable = RetrofitServiceManager.getUserService(this)
                    .getAuthUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response ->
                                    super.handleResponse(
                                            response,
                                            (responseBody, gson) -> {
                                                textViewError.setText(null);
                                                UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                                this.bindAuthInformation(context, userResponse);
                                            },
                                            errorApiResponse -> {
                                                textViewError.setText(errorApiResponse.getMessage());
                                                super.removeBearer();
                                                ActivityUtils.goTo(this, getString(R.string.login_path));
                                            }
                                    ),
                            error -> {
                                Log.e(ERROR_TAG, "fetchAuthUser: " + error.getMessage(), error);
                                textViewError.setText(error.getMessage());
                                super.pushToast(error.getMessage());
                                super.removeBearer();
                                ActivityUtils.goTo(this, getString(R.string.login_path));
                            });

            compositeDisposable.add(disposable);
        } else {
            super.removeBearer();
            ActivityUtils.goTo(this, getString(R.string.login_path));
        }
    }

    private EditUserRequest validateEditRequest() {
        String name = inputTextName.getText().toString().trim();
        String username = inputTextUsername.getText().toString().trim();
        String address = inputTextAddress.getText().toString().trim();
        String phone = inputTextPhone.getText().toString().trim();
        String email = inputTextEmail.getText().toString().trim();
        File avatar = imageAvatar.getFile();

        if (StringUtils.isEmpty(name)) {
            inputTextName.setError("name cannot be empty");
            inputTextName.requestFocus();
            return null;
        } else {
            inputTextName.setError(null);
        }

        if (StringUtils.isEmpty(username)) {
            inputTextUsername.setError("username cannot be empty");
            inputTextUsername.requestFocus();
            return null;
        } else {
            inputTextUsername.setError(null);
        }

        if (StringUtils.isEmpty(address)) {
            inputTextAddress.setError("address cannot be empty");
            inputTextAddress.requestFocus();
            return null;
        } else {
            inputTextAddress.setError(null);
        }

        if (StringUtils.isEmpty(phone)) {
            inputTextPhone.setError("phone cannot be empty");
            inputTextPhone.requestFocus();
            return null;
        } else {
            inputTextPhone.setError(null);
        }

        if (!ValidationUtils.isValidEmail(email)) {
            inputTextEmail.setError("email format invalid");
            inputTextEmail.requestFocus();
            return null;
        } else {
            inputTextEmail.setError(null);
        }

        return new EditUserRequest(
                name,
                username,
                address,
                phone,
                email,
                avatar);
    }

    private RequestBody buildEditUserRequestBody(EditUserRequest editUserRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: EditUserRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(editUserRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildEditUserRequestBody: " + e.getMessage(), e);
            }
        }

        if (editUserRequest.getAvatar() != null) {
            RequestBody fileRequestBody = RequestBody.create(editUserRequest.getAvatar(), MediaType.parse("multipart/form-data"));
            builder.addFormDataPart("avatar", editUserRequest.getAvatar().getName(), fileRequestBody);
        }

        return builder.build();
    }

    private void submitEdit(Context context, RequestBody editUserRequestBody) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .editPersonalInfo(editUserRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            this.bindAuthInformation(context, userResponse);
                                            pushToast("personal information updated successfully");
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEdit: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void bindAuthInformation(Context context, UserResponse userResponse) {
        inputTextName.setText(userResponse.getName());
        inputTextUsername.setText(userResponse.getUsername());
        inputTextAddress.setText(userResponse.getAddress());
        inputTextPhone.setText(userResponse.getPhone());
        inputTextEmail.setText(userResponse.getEmail());
        imageAvatar.setUrl(RetrofitManager.getImageUrl(context, userResponse.getAvatar()));
    }
}