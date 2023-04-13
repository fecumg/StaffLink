package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.components.CustomCheckBoxComponent;
import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.stafflink.models.requestDtos.userRequestDtos.NewUserRequest;
import fpt.edu.stafflink.models.responseDtos.RoleResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.response.RetrofitResponse.MergedResponse;
import fpt.edu.stafflink.retrofit.RetrofitManager;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.ValidationUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class UserFormActivity extends BaseActivity {
    private static final String ERROR_TAG = "UserFormActivity";

    ImageButton buttonBackToUsers;
    ImageButton buttonSubmitUser;
    TextView textViewUserFormTitle;

    TextView textViewError;

    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextUsername;
    CustomInputTextComponent inputTextAddress;
    CustomInputTextComponent inputTextPhone;
    CustomInputTextComponent inputTextEmail;
    CustomInputTextComponent inputTextPassword;
    CustomInputTextComponent inputTextConfirmPassword;
    CustomImageComponentOval imageAvatar;
    CustomCheckBoxComponent<RoleResponse> checkBoxRoles;

    private int id;
    private int position;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_form);

        buttonBackToUsers = findViewById(R.id.buttonBackToUsers);
        buttonSubmitUser = findViewById(R.id.buttonSubmitUser);

        textViewUserFormTitle = findViewById(R.id.textViewUserFormTitle);

        textViewError = findViewById(R.id.textViewError);

        inputTextName = findViewById(R.id.inputTextName);
        inputTextUsername = findViewById(R.id.inputTextUsername);
        inputTextAddress = findViewById(R.id.inputTextAddress);
        inputTextPhone = findViewById(R.id.inputTextPhone);
        inputTextEmail = findViewById(R.id.inputTextEmail);
        inputTextPassword = findViewById(R.id.inputTextPassword);
        inputTextConfirmPassword = findViewById(R.id.inputTextConfirmPassword);
        imageAvatar = findViewById(R.id.imageAvatar);
        checkBoxRoles = findViewById(R.id.checkBoxRoles);

        Intent intent = getIntent();
        this.id = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);

        if (id == DEFAULT_ID) {
            textViewUserFormTitle.setText(R.string.user_form_title_new);

            this.fetchDataOnNew();

            buttonSubmitUser.setOnClickListener((view) -> {
                NewUserRequest newUserRequest = this.validateNewUser();
                if (newUserRequest != null) {
                    RequestBody newUserRequestBody = this.buildNewUserRequestBody(newUserRequest);
                    this.submitNewUser(newUserRequestBody);
                }
            });
        } else {
            textViewUserFormTitle.setText(R.string.user_form_title_edit);

            inputTextUsername.setEditable(false);

            inputTextPassword.setVisibility(View.GONE);
            inputTextConfirmPassword.setVisibility(View.GONE);

            this.fetchDataOnEdit(this, id);

            buttonSubmitUser.setOnClickListener((view) -> {
                EditUserRequest editUserRequest = this.validateEditUser();
                if (editUserRequest != null) {
                    RequestBody editUserRequestBody = this.buildEditUserRequestBody(editUserRequest);
                    this.submitEditUser(id, editUserRequestBody);
                }
            });
        }

        buttonBackToUsers.setOnClickListener(view -> onBackPressed());
    }

    private void fetchDataOnNew() {
        Pagination pagination = new Pagination(0);

        Disposable disposable = RetrofitServiceManager
                .getRoleService(this)
                .getRoles(pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                    response,
                                    (responseBody, gson) -> {
                                        checkBoxRoles.setError(null);
                                        Type type = new TypeToken<List<RoleResponse>>() {}.getType();
                                        List<RoleResponse> roleResponses = gson.fromJson(gson.toJson(responseBody), type);
                                        checkBoxRoles.setData(roleResponses, new LinkedList<>(), "name");
                                    },
                                    errorApiResponse -> checkBoxRoles.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnNew: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            checkBoxRoles.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void fetchDataOnEdit(Context context, int id) {
        Pagination pagination = new Pagination(0);

        Observable<Response<Object>> getRolesObservable = RetrofitServiceManager
                .getRoleService(this)
                .getRoles(pagination)
                .subscribeOn(Schedulers.io());


        Observable<Response<Object>> getUserObservable = RetrofitServiceManager
                .getUserService(this)
                .getUser(id)
                .subscribeOn(Schedulers.io());

        Disposable disposable = Observable.zip(getRolesObservable, getUserObservable, MergedResponse::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mergedResponse ->
                                super.handleMergedResponse(
                                    mergedResponse,
                                    (firstResBody, secondResBody, gson) -> {
                                        Type type = new TypeToken<List<RoleResponse>>() {}.getType();
                                        List<RoleResponse> roleResponses = gson.fromJson(gson.toJson(firstResBody), type);
                                        UserResponse userResponse = gson.fromJson(gson.toJson(secondResBody), UserResponse.class);
                                        checkBoxRoles.setData(
                                                roleResponses,
                                                new LinkedList<>(userResponse.getRoles()),
                                                "name");
                                        bindEditedUser(context, userResponse);
                                        checkBoxRoles.setError(null);
                                    },
                                    errorApiResponse -> checkBoxRoles.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnEdit: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            checkBoxRoles.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitNewUser(RequestBody newUserRequestBody) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .newUser(newUserRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                    response,
                                    (responseBody, gson) -> {
                                        textViewError.setText(null);
                                        UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                        this.id = userResponse.getId();
                                        this.backToUsers();
                                    },
                                    errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewUser: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitEditUser(int id, RequestBody editUserRequestBody) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .editUser(id, editUserRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                    response,
                                    (responseBody, gson) -> {
                                        textViewError.setText(null);
                                        this.backToUsers();
                                    },
                                    errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditUser: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private NewUserRequest validateNewUser() {
        String name = inputTextName.getText().toString().trim();
        String username = inputTextUsername.getText().toString().trim();
        String address = inputTextAddress.getText().toString().trim();
        String phone = inputTextPhone.getText().toString().trim();
        String email = inputTextEmail.getText().toString().trim();
        String password = inputTextPassword.getText().toString().trim();
        String confirmPassword = inputTextConfirmPassword.getText().toString().trim();
        File avatar = imageAvatar.getFile();
        List<RoleResponse> checkedRoles = checkBoxRoles.getCheckedObjects();

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

        if (StringUtils.isEmpty(password)) {
            inputTextPassword.setError("password cannot be empty");
            inputTextPassword.requestFocus();
            return null;
        } else {
            inputTextPassword.setError(null);
        }

        if (password.length() < 6) {
            inputTextPassword.setError("password must exceed 5 characters");
            inputTextPassword.requestFocus();
            return null;
        } else {
            inputTextPassword.setError(null);
        }

        if (StringUtils.isEmpty(confirmPassword)) {
            inputTextConfirmPassword.setError("please confirm password");
            inputTextConfirmPassword.requestFocus();
            return null;
        } else if (!confirmPassword.equals(password)) {
            inputTextConfirmPassword.setError("password and confirm password does not match");
            inputTextConfirmPassword.requestFocus();
            return null;
        } else {
            inputTextConfirmPassword.setError(null);
        }

        if (avatar == null || !avatar.exists()) {
            super.pushToast("avatar must be selected");
            imageAvatar.requestFocus();
            return null;
        }

        List<Integer> checkedRoleIds = checkedRoles.stream()
                .map(RoleResponse::getId)
                .collect(Collectors.toList());

        return new NewUserRequest(
                name,
                username,
                address,
                phone,
                email,
                password,
                confirmPassword,
                avatar,
                checkedRoleIds);
    }

    private EditUserRequest validateEditUser() {
        String name = inputTextName.getText().toString().trim();
        String username = inputTextUsername.getText().toString().trim();
        String address = inputTextAddress.getText().toString().trim();
        String phone = inputTextPhone.getText().toString().trim();
        String email = inputTextEmail.getText().toString().trim();
        File avatar = imageAvatar.getFile();
        List<RoleResponse> checkedRoles = checkBoxRoles.getCheckedObjects();

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

        List<Integer> checkedRoleIds = checkedRoles.stream()
                .map(RoleResponse::getId)
                .collect(Collectors.toList());

        return new EditUserRequest(
                name,
                username,
                address,
                phone,
                email,
                avatar,
                checkedRoleIds);
    }

    public RequestBody buildNewUserRequestBody(NewUserRequest newUserRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: NewUserRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(newUserRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildNewUserRequestBody: " + e.getMessage(), e);
            }
        }

        RequestBody fileRequestBody = RequestBody.create(newUserRequest.getAvatar(), MediaType.parse("multipart/form-data"));
        builder.addFormDataPart("avatar", newUserRequest.getAvatar().getName(), fileRequestBody);

        newUserRequest.getRoleIds()
                .forEach(roleId -> builder.addFormDataPart("roleIds", String.valueOf(roleId)));

        return builder.build();
    }

    public RequestBody buildEditUserRequestBody(EditUserRequest editUserRequest) {
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

        editUserRequest.getRoleIds()
                .forEach(roleId -> builder.addFormDataPart("roleIds", String.valueOf(roleId)));

        return builder.build();
    }

    private void bindEditedUser(Context context, UserResponse userResponse) {
        inputTextName.setText(userResponse.getName());
        inputTextUsername.setText(userResponse.getUsername());
        inputTextAddress.setText(userResponse.getAddress());
        inputTextPhone.setText(userResponse.getPhone());
        inputTextEmail.setText(userResponse.getEmail());
        imageAvatar.setUrl(RetrofitManager.getImageUrl(context, userResponse.getAvatar()));
    }

    private void backToUsers() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        setResult(RESULT_OK, intent);
        finish();
    }
}