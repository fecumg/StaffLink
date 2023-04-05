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
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UsersActivity extends BaseActivity {
    private static final String ERROR_TAG = "UsersActivity";
    private static final String USER_ACTION = "UserAction";

    private int pageNumber = 1;
    private static final int PAGE_SIZE = 20;
    ImageButton buttonNewUser;
    ImageButton buttonRefreshUsers;
    CustomTableComponent<UserResponse> tableUsers;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_users);

        buttonNewUser = findViewById(R.id.buttonNewUser);
        buttonRefreshUsers = findViewById(R.id.buttonRefreshUsers);
        tableUsers = findViewById(R.id.tableUsers);

        compositeDisposable = new CompositeDisposable();

        this.setFormActivityResultLauncher();

        buttonNewUser.setOnClickListener(view -> formActivityResultLauncher.launch(new Intent(UsersActivity.this, UserFormActivity.class)));
        buttonRefreshUsers.setOnClickListener(view -> this.refresh());

        this.initTable();
        this.fetchUsers();

        this.listenToAdapterOnClick();
    }

    private void initTable() {
        String[] displayedFields = new String[] {
                "avatar",
                "name",
                "username"
        };

        String[] imageFields = new String[] {
                "avatar"
        };
        tableUsers.setDisplayedFields(displayedFields);
        tableUsers.setImageFields(imageFields);
        tableUsers.setAction(USER_ACTION);
        tableUsers.setError(null);
    }

    private void fetchNewUser(int id) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableUsers.setError(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            tableUsers.adapter.addNewItem(userResponse);
                                            super.pushToast("user with username: " + userResponse.getUsername() + " added successfully");
                                            tableUsers.scrollTo(tableUsers.getObjects().indexOf(userResponse));
                                        },
                                        errorApiResponse -> tableUsers.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewUser: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableUsers.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedUser(int id, int position) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            tableUsers.setError(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            tableUsers.adapter.modifyItem(position, userResponse);
                                            super.pushToast("user with username: " + userResponse.getUsername() + " edited successfully");
                                            tableUsers.scrollTo(position);
                                        },
                                        errorApiResponse -> tableUsers.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedUser: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableUsers.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchUsers() {
        Pagination pagination = new Pagination(pageNumber, PAGE_SIZE);

        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUsers(pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                            super.handleResponse(
                                    response,
                                    (responseBody, gson) -> {
                                        tableUsers.setError(null);
                                        Type type = new TypeToken<List<UserResponse>>() {}.getType();
                                        List<UserResponse> userResponses = gson.fromJson(gson.toJson(responseBody), type);
                                        if (pageNumber == 1) {
                                            tableUsers.setObjects(userResponses);
                                        } else {
                                            tableUsers.adapter.addNewItems(userResponses);
                                        }
                                        pageNumber ++;
                                    },
                                    errorApiResponse -> tableUsers.setError(errorApiResponse.getMessage())
                            ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchUsers: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            tableUsers.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void refresh() {
        this.pageNumber = 1;
        this.fetchUsers();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int objectId = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (objectId != DEFAULT_ID) {
                            Intent editUserIntent = new Intent(UsersActivity.this, UserFormActivity.class);
                            editUserIntent.putExtra(PARAM_ID, objectId);
                            editUserIntent.putExtra(PARAM_POSITION, objectPosition);
                            formActivityResultLauncher.launch(editUserIntent);
                        }
                    }
                }, new IntentFilter(USER_ACTION));
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
                                fetchNewUser(id);
                            } else {
                                fetchEditedUser(id, position);
                            }
                        }
                    }
                });
    }
}