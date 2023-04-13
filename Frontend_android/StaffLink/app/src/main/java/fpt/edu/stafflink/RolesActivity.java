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

import fpt.edu.stafflink.components.CustomTableComponent;
import fpt.edu.stafflink.models.responseDtos.RoleResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RolesActivity extends BaseActivity {

    private static final String ERROR_TAG = "RolesActivity";
    private static final String ROLE_ACTION = "RoleAction";

    ImageButton buttonNewRole;
    ImageButton buttonRefreshRoles;
    CustomTableComponent<RoleResponse> tableRoles;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_roles);

        buttonNewRole = findViewById(R.id.buttonNewRole);
        buttonRefreshRoles = findViewById(R.id.buttonRefreshRoles);
        tableRoles = findViewById(R.id.tableRoles);

        this.setFormActivityResultLauncher();

        buttonNewRole.setOnClickListener(view -> formActivityResultLauncher.launch(new Intent(RolesActivity.this, RoleFormActivity.class)));
        buttonRefreshRoles.setOnClickListener(view -> this.refresh());


        this.initTable();
        this.fetchRoles();

        this.listenToAdapterOnClick();
    }

    private void initTable() {
        String[] displayedFields = new String[] {
                "name",
                "description"
        };

        tableRoles.setDisplayedFields(displayedFields);
        tableRoles.setAction(ROLE_ACTION);
        tableRoles.setError(null);
    }

    private void fetchNewRole(int id) {
        Disposable disposable = RetrofitServiceManager.getRoleService(this)
                .getRole(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableRoles.setError(null);
                                            RoleResponse roleResponse = gson.fromJson(gson.toJson(responseBody), RoleResponse.class);
                                            tableRoles.adapter.addNewItem(roleResponse);
                                            super.pushToast("role with name: " + roleResponse.getName() + " added successfully");
                                            tableRoles.scrollTo(tableRoles.getObjects().indexOf(roleResponse));
                                        },
                                        errorApiResponse -> tableRoles.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewRole: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableRoles.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedRole(int id, int position) {
        Disposable disposable = RetrofitServiceManager.getRoleService(this)
                .getRole(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableRoles.setError(null);
                                            RoleResponse roleResponse = gson.fromJson(gson.toJson(responseBody), RoleResponse.class);
                                            tableRoles.adapter.modifyItem(position, roleResponse);
                                            super.pushToast("role with name: " + roleResponse.getName() + " edited successfully");
                                            tableRoles.scrollTo(position);
                                        },
                                        errorApiResponse -> tableRoles.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedRole: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableRoles.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchRoles() {
        Pagination pagination = new Pagination(0);

        Disposable disposable = RetrofitServiceManager.getRoleService(this)
                .getRoles(pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableRoles.setError(null);
                                            Type type = new TypeToken<List<RoleResponse>>() {}.getType();
                                            List<RoleResponse> roleResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            tableRoles.setObjects(roleResponses);
                                        },
                                        errorApiResponse -> tableRoles.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchRoles: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableRoles.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void refresh() {
        this.fetchRoles();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int objectId = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (objectId != DEFAULT_ID) {
                            Intent editRoleIntent = new Intent(RolesActivity.this, RoleFormActivity.class);
                            editRoleIntent.putExtra(PARAM_ID, objectId);
                            editRoleIntent.putExtra(PARAM_POSITION, objectPosition);
                            formActivityResultLauncher.launch(editRoleIntent);
                        }
                    }
                }, new IntentFilter(ROLE_ACTION));
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
                                fetchNewRole(id);
                            } else {
                                fetchEditedRole(id, position);
                            }
                        }
                    }
                });
    }
}