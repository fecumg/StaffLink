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

import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.components.CustomSelectComponent;
import fpt.edu.stafflink.models.requestDtos.FunctionRequest;
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

public class FunctionFormActivity extends BaseActivity {
    private static final String ERROR_TAG = "FunctionFormActivity";

    ImageButton buttonBackToFunctions;
    ImageButton buttonSubmitFunction;
    TextView textViewFunctionFormTitle;

    TextView textViewError;

    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;
    CustomInputTextComponent inputTextUri;
    CustomSelectComponent<FunctionResponse> selectParent;
    SwitchCompat switchDisplayed;

    private int id;
    private int position;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_function_form);

        buttonBackToFunctions = findViewById(R.id.buttonBackToFunctions);
        buttonSubmitFunction = findViewById(R.id.buttonSubmitFunction);

        textViewFunctionFormTitle = findViewById(R.id.textViewFunctionFormTitle);

        textViewError = findViewById(R.id.textViewError);

        inputTextName = findViewById(R.id.inputTextName);
        inputTextDescription = findViewById(R.id.inputTextDescription);
        inputTextUri = findViewById(R.id.inputTextUri);
        selectParent = findViewById(R.id.selectParent);
        switchDisplayed = findViewById(R.id.switchDisplayed);

        compositeDisposable = new CompositeDisposable();

        Intent intent = getIntent();
        this.id = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);

        if (id == DEFAULT_ID) {
            textViewFunctionFormTitle.setText(R.string.function_form_title_new);

            this.fetchDataOnNew();

            buttonSubmitFunction.setOnClickListener((view) -> {
                FunctionRequest functionRequest = this.validateFunction();
                if (functionRequest != null) {
                    RequestBody functionRequestBody = this.buildFunctionRequestBody(functionRequest);
                    this.submitNewFunction(functionRequestBody);
                }
            });
        } else {
            textViewFunctionFormTitle.setText(R.string.function_form_title_edit);

            this.fetchDataOnEdit(id);

            buttonSubmitFunction.setOnClickListener((view) -> {
                FunctionRequest functionRequest = this.validateFunction();
                if (functionRequest != null) {
                    RequestBody functionRequestBody = this.buildFunctionRequestBody(functionRequest);
                    this.submitEditFunction(id, functionRequestBody);
                }
            });
        }

        buttonBackToFunctions.setOnClickListener(view -> onBackPressed());
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
                                            selectParent.setError(null);
                                            Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                            List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            selectParent.setData(functionResponses, "name");
                                        },
                                        errorApiResponse -> selectParent.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnNew: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            selectParent.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void fetchDataOnEdit(int id) {
        Observable<Response<Object>> getFunctionsObservable = RetrofitServiceManager
                .getFunctionService(this)
                .getPotentialParents(id)
                .subscribeOn(Schedulers.io());

        Observable<Response<Object>> getFunctionObservable = RetrofitServiceManager
                .getFunctionService(this)
                .getFunction(id)
                .subscribeOn(Schedulers.io());

        Disposable disposable = Observable.zip(getFunctionsObservable, getFunctionObservable, MergedResponse::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mergedResponse ->
                                super.handleMergedResponse(
                                        mergedResponse,
                                        (firstResBody, secondResBody, gson) -> {
                                            Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                            List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(firstResBody), type);
                                            FunctionResponse functionResponse = gson.fromJson(gson.toJson(secondResBody), FunctionResponse.class);

                                            selectParent.setData(functionResponses, "name");
                                            bindEditedFunction(functionResponse);
                                            selectParent.setError(null);
                                        },
                                        errorApiResponse -> selectParent.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnEdit: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            selectParent.setError(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitNewFunction(RequestBody functionRequestBody) {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .newFunction(functionRequestBody)
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
                                            this.backToFunctions();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void submitEditFunction(int id, RequestBody functionRequestBody) {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .editFunction(id, functionRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            this.backToFunctions();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private FunctionRequest validateFunction() {
        String name = inputTextName.getText().toString().trim();
        String description = inputTextDescription.getText().toString().trim();
        String uri = inputTextUri.getText().toString().trim();
        FunctionResponse parent = selectParent.getSelectedOption();
        boolean displayed = switchDisplayed.isChecked();

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

        return new FunctionRequest(
                name,
                description,
                uri,
                parent == null ? 0 : parent.getId(),
                displayed);
    }

    public RequestBody buildFunctionRequestBody(FunctionRequest functionRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: FunctionRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(functionRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildFunctionRequestBody: " + e.getMessage(), e);
            }
        }

        builder.addFormDataPart("parentId", String.valueOf(functionRequest.getParentId()));
        builder.addFormDataPart("displayed", String.valueOf(functionRequest.isDisplayed()));

        return builder.build();
    }

    private void bindEditedFunction(FunctionResponse functionResponse) {
        inputTextName.setText(functionResponse.getName());
        inputTextDescription.setText(functionResponse.getDescription());
        inputTextUri.setText(functionResponse.getUri());
        selectParent.setSelectedOption(functionResponse.getParent());
        switchDisplayed.setChecked(functionResponse.isDisplayed());
    }

    public void backToFunctions() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        setResult(RESULT_OK, intent);
        finish();
    }
}