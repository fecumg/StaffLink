package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;

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
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProjectsActivity extends BaseActivity {
    private static final String ERROR_TAG = "AllProjectsActivity";
    private static final String PROJECT_ACTION = "ProjectAction";
    public static final int PROJECT_ACCESS_TYPE_ALL = 0;
    public static final int PROJECT_ACCESS_TYPE_ASSIGNED = 1;
    public static final int PROJECT_ACCESS_TYPE_CREATED = 2;

    private int pageNumber = 1;
    private static final int PAGE_SIZE = 20;
    ImageButton buttonRefreshProjects;
    CustomListComponent<ProjectResponse> listProjects;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_projects);

        buttonRefreshProjects = findViewById(R.id.buttonRefreshProjects);
        listProjects = findViewById(R.id.listProjects);

        compositeDisposable = new CompositeDisposable();

        this.setFormActivityResultLauncher();

        buttonRefreshProjects.setOnClickListener(view -> this.refresh());

        this.initTable();
        this.fetchProjects();

        this.listenToAdapterOnClick();
    }

    private void initTable() {
        listProjects.setTitleField("name");
        listProjects.setContentField("description");
        listProjects.setAction(PROJECT_ACTION);
        listProjects.setError(null);
    }

    private void fetchProjects() {
//        Pagination pagination = new Pagination(pageNumber, PAGE_SIZE);

        Disposable disposable = RetrofitServiceManager.getProjectService(this)
                .getAllProjects()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listProjects.setError(null);
                                            Type type = new TypeToken<List<ProjectResponse>>() {}.getType();
                                            List<ProjectResponse> projectResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            listProjects.adapter.addNewItems(projectResponses);
                                        },
                                        errorApiResponse -> listProjects.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchProjects: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listProjects.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void refresh() {
//        this.pageNumber = 1;
        this.fetchProjects();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int objectId = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (objectId != DEFAULT_ID) {
                            Intent accessProjectIntent = new Intent(ProjectsActivity.this, ProjectAccessActivity.class);
                            accessProjectIntent.putExtra(PARAM_ID, objectId);
                            accessProjectIntent.putExtra(PARAM_POSITION, objectPosition);

                            setProjectAccessType(accessProjectIntent);

                            formActivityResultLauncher.launch(accessProjectIntent);
                        }
                    }
                }, new IntentFilter(PROJECT_ACTION));
    }

    private void setFormActivityResultLauncher() {
        formActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        this.doOnBackFromForm();
                    }
                });
    }

    private void setProjectAccessType(Intent accessProjectIntent) {
        accessProjectIntent.putExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_ALL);
    }

    private void doOnBackFromForm() {

    }
}