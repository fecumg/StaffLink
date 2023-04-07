package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.components.CustomCheckBoxComponent;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.RoleRequest;
import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
import fpt.edu.stafflink.models.responseDtos.RoleResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.response.RetrofitResponse.MergedResponse;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class RoleFormActivity extends BaseActivity {

    private static final String ERROR_TAG = "RoleFormActivity";

    ImageButton buttonBackToRoles;
    ImageButton buttonSubmitRole;
    TextView textViewRoleFormTitle;

    TextView textViewError;

    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;
    CustomCheckBoxComponent<FunctionResponse> checkBoxFunctions;

    private int id;
    private int position;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_role_form);

        buttonBackToRoles = findViewById(R.id.buttonBackToRoles);
        buttonSubmitRole = findViewById(R.id.buttonSubmitRole);

        textViewRoleFormTitle = findViewById(R.id.textViewRoleFormTitle);

        textViewError = findViewById(R.id.textViewError);

        inputTextName = findViewById(R.id.inputTextName);
        inputTextDescription = findViewById(R.id.inputTextDescription);
        checkBoxFunctions = findViewById(R.id.checkBoxFunctions);

        compositeDisposable = new CompositeDisposable();

        checkBoxFunctions.setParentField("parent");

        Intent intent = getIntent();
        this.id = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);

        if (id == DEFAULT_ID) {
            textViewRoleFormTitle.setText(R.string.role_form_title_new);

            this.fetchDataOnNew();

            buttonSubmitRole.setOnClickListener((view) -> {
                RoleRequest roleRequest = this.validateRole();
                if (roleRequest != null) {
                    RequestBody roleRequestBody = this.buildRoleRequestBody(roleRequest);
                    this.submitNewRole(roleRequestBody);
                }
            });
        } else {
            textViewRoleFormTitle.setText(R.string.role_form_title_edit);

            this.fetchDataOnEdit(id);

            buttonSubmitRole.setOnClickListener((view) -> {
                RoleRequest roleRequest = this.validateRole();
                if (roleRequest != null) {
                    RequestBody roleRequestBody = this.buildRoleRequestBody(roleRequest);
                    this.submitEditRole(id, roleRequestBody);
                }
            });
        }

        buttonBackToRoles.setOnClickListener(view -> onBackPressed());
    }

    private void fetchDataOnNew() {
        Pagination pagination = new Pagination(0);

        Disposable disposable = RetrofitServiceManager
                .getFunctionService(this)
                .getFunctions(pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                    response,
                                    (responseBody, gson) -> {
                                        checkBoxFunctions.setError(null);
                                        Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                        List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(responseBody), type);
                                        checkBoxFunctions.setData(functionResponses, new LinkedList<>(), "name");
                                    },
                                    errorApiResponse -> checkBoxFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnNew: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            checkBoxFunctions.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void fetchDataOnEdit(int id) {
        Pagination pagination = new Pagination(0);

        Observable<Response<Object>> getFunctionsObservable = RetrofitServiceManager
                .getFunctionService(this)
                .getFunctions(pagination)
                .subscribeOn(Schedulers.io());

        Observable<Response<Object>> getRoleObservable = RetrofitServiceManager
                .getRoleService(this)
                .getRole(id)
                .subscribeOn(Schedulers.io());

        Disposable disposable = Observable.zip(getFunctionsObservable, getRoleObservable, MergedResponse::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mergedResponse ->
                                super.handleMergedResponse(
                                    mergedResponse,
                                    (firstResBody, secondResBody, gson) -> {
                                        Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                        List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(firstResBody), type);
                                        RoleResponse roleResponse = gson.fromJson(gson.toJson(secondResBody), RoleResponse.class);
                                        checkBoxFunctions.setData(
                                                functionResponses,
                                                new LinkedList<>(roleResponse.getFunctions()),
                                                "name");
                                        bindEditedRole(roleResponse);
                                        checkBoxFunctions.setError(null);
                                    },
                                    errorApiResponse -> checkBoxFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnEdit: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            checkBoxFunctions.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitNewRole(RequestBody roleRequestBody) {
        Disposable disposable = RetrofitServiceManager.getRoleService(this)
                .newRole(roleRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            RoleResponse roleResponse = gson.fromJson(gson.toJson(responseBody), RoleResponse.class);
                                            this.id = roleResponse.getId();
                                            this.backToRoles();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewRole: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitEditRole(int id, RequestBody roleRequestBody) {
        Disposable disposable = RetrofitServiceManager.getRoleService(this)
                .editRole(id, roleRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            this.backToRoles();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditRole: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private RoleRequest validateRole() {
        String name = inputTextName.getText().toString().trim();
        String description = inputTextDescription.getText().toString().trim();
        List<FunctionResponse> checkedFunctions = checkBoxFunctions.getCheckedObjects();

        if (StringUtils.isEmpty(name)) {
            inputTextName.setError("name cannot be empty");
            inputTextName.requestFocus();
            return null;
        } else {
            inputTextName.setError(null);
        }

        if (StringUtils.isEmpty(description)) {
            inputTextDescription.setError("description cannot be empty");
            inputTextDescription.requestFocus();
            return null;
        } else {
            inputTextDescription.setError(null);
        }

        List<Integer> checkedFunctionIds = checkedFunctions.stream()
                .map(FunctionResponse::getId)
                .collect(Collectors.toList());

        return new RoleRequest(
                name,
                description,
                checkedFunctionIds);
    }

    public RequestBody buildRoleRequestBody(RoleRequest roleRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: RoleRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(roleRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildRoleRequestBody: " + e.getMessage(), e);
            }
        }

        roleRequest.getFunctionIds()
                .forEach(functionId -> builder.addFormDataPart("functionIds", String.valueOf(functionId)));

        return builder.build();
    }

    private void bindEditedRole(RoleResponse roleResponse) {
        inputTextName.setText(roleResponse.getName());
        inputTextDescription.setText(roleResponse.getDescription());
    }

    private void backToRoles() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        setResult(RESULT_OK, intent);
        finish();
    }
}