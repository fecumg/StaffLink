package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import reactor.core.scheduler.Scheduler;
import retrofit2.Response;

public class ProjectsActivity extends BaseActivity {
    private static final String ERROR_TAG = "AllProjectsActivity";
    private static final String PROJECT_ACTION = "ProjectAction";
    public static final int PROJECT_ACCESS_TYPE_OBSERVABLE = 0;
    public static final int PROJECT_ACCESS_TYPE_ASSIGNED = 1;
    public static final int PROJECT_ACCESS_TYPE_CREATED = 2;

    private int pageNumber = 1;
    private static final int PAGE_SIZE = 20;
    ImageButton buttonRefreshProjects;
    ImageButton buttonNewProject;
    CustomListComponent<ProjectResponse> listProjects;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_projects);

        buttonRefreshProjects = findViewById(R.id.buttonRefreshProjects);
        buttonNewProject = findViewById(R.id.buttonNewProject);
        listProjects = findViewById(R.id.listProjects);

        compositeDisposable = new CompositeDisposable();

        this.setFormActivityResultLauncher();

        buttonRefreshProjects.setOnClickListener(view -> this.refresh());

        buttonNewProject.setOnClickListener(view -> formActivityResultLauncher.launch(new Intent(ProjectsActivity.this, ProjectAccessActivity.class)));

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

    private void fetchNewProject(String id) {
        Disposable disposable = RetrofitServiceManager.getProjectService(this)
                .getProject(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listProjects.setError(null);
                                            ProjectResponse projectResponse = gson.fromJson(gson.toJson(responseBody), ProjectResponse.class);
                                            listProjects.adapter.addNewItem(projectResponse);
                                            super.pushToast("project with name: " + projectResponse.getName() + " added successfully");
                                            listProjects.scrollTo(listProjects.getObjects().indexOf(projectResponse));
                                        },
                                        errorApiResponse -> listProjects.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewProject: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listProjects.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedProject(String id, int position) {
        Disposable disposable = RetrofitServiceManager.getProjectService(this)
                .getProject(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listProjects.setError(null);
                                            ProjectResponse projectResponse = gson.fromJson(gson.toJson(responseBody), ProjectResponse.class);
                                            listProjects.adapter.modifyItem(position, projectResponse);
                                            super.pushToast("project with name: " + projectResponse.getName() + " edited successfully");
                                            listProjects.scrollTo(position);
                                        },
                                        errorApiResponse -> listProjects.setError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedProject: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listProjects.setError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchProjects() {
        MultiValuePagination pagination = new MultiValuePagination();

        reactor.core.Disposable disposable = WebClientServiceManager.getProjectServiceInstance()
                .getCreatedProjects(this, pagination)
                .subscribe(
                        projectResponse -> runOnUiThread(() -> {
                            listProjects.setError(null);
                            listProjects.adapter.addNewItem(projectResponse);
                        }),
                        error -> runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchProjects: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listProjects.setError(error.getMessage());
                        })
                );

        reactorCompositeDisposable.add(disposable);
    }

    private void refresh() {
        this.pageNumber = 1;
        listProjects.setObjects(new ArrayList<>());
        this.fetchProjects();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String objectId = intent.getStringExtra(PARAM_STRING_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        if (StringUtils.isNotEmpty(objectId)) {
                            Intent accessProjectIntent = new Intent(ProjectsActivity.this, ProjectAccessActivity.class);
                            accessProjectIntent.putExtra(PARAM_STRING_ID, objectId);
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
                this::doOnBackFromForm);
    }

    private void setProjectAccessType(Intent accessProjectIntent) {
        accessProjectIntent.putExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_CREATED);
    }

    private void doOnBackFromForm(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String id = result.getData().getStringExtra(PARAM_STRING_ID);
            int position = result.getData().getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
            if (StringUtils.isNotEmpty(id)) {
                if (position == DEFAULT_POSITION) {
                    this.fetchNewProject(id);
                } else {
                    this.fetchEditedProject(id, position);
                }
            }
        }
    }
}