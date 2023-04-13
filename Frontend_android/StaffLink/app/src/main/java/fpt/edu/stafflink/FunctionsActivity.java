package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import fpt.edu.stafflink.components.CustomTableComponent;
import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FunctionsActivity extends BaseActivity {

    private static final String ERROR_TAG = "FunctionsActivity";
    private static final String FUNCTION_ACTION = "FunctionAction";

    ImageButton buttonNewFunction;
    ImageButton buttonRefreshFunctions;
    CustomTableComponent<FunctionResponse> tableFunctions;
    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_functions);

        buttonNewFunction = findViewById(R.id.buttonNewFunction);
        buttonRefreshFunctions = findViewById(R.id.buttonRefreshFunctions);
        tableFunctions = findViewById(R.id.tableFunctions);

        this.setFormActivityResultLauncher();

        buttonNewFunction.setOnClickListener(view -> formActivityResultLauncher.launch(new Intent(FunctionsActivity.this, FunctionFormActivity.class)));
        buttonRefreshFunctions.setOnClickListener(view -> this.refresh());

        this.initTable();
        this.fetchFunctions();

        this.listenToAdapterOnClick();
    }

    private void initTable() {
        String[] displayedFields = new String[] {
                "name",
                "description"
        };

        tableFunctions.setDisplayedFields(displayedFields);
        tableFunctions.setAction(FUNCTION_ACTION);
        tableFunctions.setError(null);
    }

    private void fetchNewFunction(int id) {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .getFunction(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableFunctions.setError(null);
                                            FunctionResponse functionResponse = gson.fromJson(gson.toJson(responseBody), FunctionResponse.class);
                                            tableFunctions.adapter.addNewItem(functionResponse);
                                            super.pushToast("function with name: " + functionResponse.getName() + " added successfully");
                                            tableFunctions.scrollTo(tableFunctions.getObjects().indexOf(functionResponse));
                                        },
                                        errorApiResponse -> tableFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableFunctions.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedFunction(int id, int position) {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .getFunction(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableFunctions.setError(null);
                                            FunctionResponse functionResponse = gson.fromJson(gson.toJson(responseBody), FunctionResponse.class);
                                            tableFunctions.adapter.modifyItem(position, functionResponse);
                                            super.pushToast("function with name: " + functionResponse.getName() + " edited successfully");
                                            tableFunctions.scrollTo(position);
                                        },
                                        errorApiResponse -> tableFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableFunctions.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchFunctions() {
        Pagination pagination = new Pagination(0);

        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .getFunctions(pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableFunctions.setError(null);
                                            Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                            List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            tableFunctions.setObjects(functionResponses);
                                        },
                                        errorApiResponse -> tableFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchFunctions: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableFunctions.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void refresh() {
        this.fetchFunctions();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int objectId = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (objectId != DEFAULT_ID) {
                            Intent editFunctionIntent = new Intent(FunctionsActivity.this, FunctionFormActivity.class);
                            editFunctionIntent.putExtra(PARAM_ID, objectId);
                            editFunctionIntent.putExtra(PARAM_POSITION, objectPosition);
                            formActivityResultLauncher.launch(editFunctionIntent);
                        }
                    }
                }, new IntentFilter(FUNCTION_ACTION));
    }

    private void setFormActivityResultLauncher() {
        formActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        int id = result.getData().getIntExtra(PARAM_ID, DEFAULT_ID);
                        int position = result.getData().getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (id != DEFAULT_ID) {
                            if (position == DEFAULT_POSITION) {
                                fetchNewFunction(id);
                            } else {
                                fetchEditedFunction(id, position);
                            }
                        }
                    }
                });
    }
}