package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import fpt.edu.stafflink.components.CustomListComponent;
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
    CustomListComponent<FunctionResponse> listFunctions;
    ActivityResultLauncher<Intent> formActivityResultLauncher;

    Pagination defaultPagination = new Pagination(0);

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_functions);

        buttonNewFunction = findViewById(R.id.buttonNewFunction);
        buttonRefreshFunctions = findViewById(R.id.buttonRefreshFunctions);
        listFunctions = findViewById(R.id.listFunctions);

        this.setFormActivityResultLauncher();

        buttonNewFunction.setOnClickListener(view -> formActivityResultLauncher.launch(new Intent(FunctionsActivity.this, FunctionFormActivity.class)));
        buttonRefreshFunctions.setOnClickListener(view -> this.refresh());

        this.initList();
        this.fetchFunctions();

        this.listenToAdapterOnClick();
    }

    private void initList() {
        listFunctions.setParentField("parent");
        listFunctions.setTitleField("name");
        listFunctions.setContentField("description");
        listFunctions.setAction(FUNCTION_ACTION);
        listFunctions.setError(null);
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
                                            listFunctions.setError(null);
                                            FunctionResponse functionResponse = gson.fromJson(gson.toJson(responseBody), FunctionResponse.class);
                                            listFunctions.addItem(functionResponse);
                                            super.pushToast("function with name: " + functionResponse.getName() + " added successfully");
                                            listFunctions.scrollTo(listFunctions.getObjects().indexOf(functionResponse));
                                        },
                                        errorApiResponse -> listFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listFunctions.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedFunction(int id, int position) {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .getFunctions(defaultPagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listFunctions.setError(null);
                                            Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                            List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(responseBody), type);

                                            int newPosition = this.getIndexOfId(id, functionResponses);
                                            if (position == newPosition) {
                                                listFunctions.adapter.modifyItem(position, functionResponses.get(newPosition));
                                            } else {
                                                listFunctions.setObjects(functionResponses);
                                            }
                                            super.pushToast("function with name: " + functionResponses.get(newPosition).getName() + " edited successfully");
                                            listFunctions.scrollTo(newPosition);
                                        },
                                        errorApiResponse -> listFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedFunction: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listFunctions.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private int getIndexOfId(int id, List<FunctionResponse> functionResponses) {
        for (int i = 0; i < functionResponses.size(); i++) {
            if (functionResponses.get(i).getId() == id) {
                return i;
            }
        }
        return  -1;
    }

    private void fetchFunctions() {
        Disposable disposable = RetrofitServiceManager.getFunctionService(this)
                .getFunctions(defaultPagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listFunctions.setError(null);
                                            Type type = new TypeToken<List<FunctionResponse>>() {}.getType();
                                            List<FunctionResponse> functionResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            listFunctions.setObjects(functionResponses);
                                        },
                                        errorApiResponse -> listFunctions.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchFunctions: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listFunctions.setError(error.getMessage());
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

                            if (formActivityResultLauncher != null) {
                                formActivityResultLauncher.launch(editFunctionIntent);
                            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formActivityResultLauncher = null;
    }
}