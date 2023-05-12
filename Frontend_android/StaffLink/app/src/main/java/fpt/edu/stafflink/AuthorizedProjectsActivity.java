package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_DONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_NONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_FORM_STATUS;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AuthorizedProjectsActivity extends BaseActivity {
    protected static final String ERROR_TAG = "AuthorizedProjectsActivity";
    private static final String AUTHORIZED_PROJECT_ACTION = "AuthorizedProjectAction";

    private int pageNumber = 1;
    private static final int PAGE_SIZE = 20;

    TextView ProjectsActivityTitle;
    ImageButton buttonRefreshProjects;
    ImageButton buttonNewProject;
    CustomListComponent<ProjectResponse> listProjects;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    reactor.core.Disposable fetchProjectsDisposable;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_projects);

        ProjectsActivityTitle = findViewById(R.id.ProjectsActivityTitle);
        buttonRefreshProjects = findViewById(R.id.buttonRefreshProjects);
        buttonNewProject = findViewById(R.id.buttonNewProject);
        listProjects = findViewById(R.id.listProjects);

        this.setFormActivityResultLauncher();

        buttonRefreshProjects.setOnClickListener(view -> this.refresh());

        super.authorizedFunctions.observe(this, authorizedFunctions -> prepareButtonNew());

        this.initTitle();
        this.initTable();

        this.fetchProjects();
        this.startToListenToAdapterOnClick();
    }

    protected void initTitle() {
        ProjectsActivityTitle.setText(getString(R.string.authorized_projects_title));
    }

    protected void initTable() {
        listProjects.setTitleField("name");
        listProjects.setContentField("description");
        listProjects.setAction(AUTHORIZED_PROJECT_ACTION);
        listProjects.setError(null);
    }

    protected void prepareButtonNew() {
        if (super.isAuthorized(getString(R.string.new_project_path))) {
            buttonNewProject.setVisibility(View.VISIBLE);

            Intent newProjectIntent = new Intent(AuthorizedProjectsActivity.this, ProjectAccessActivity.class);
            setIntentProjectAccessType(newProjectIntent);
            buttonNewProject.setOnClickListener(view -> formActivityResultLauncher.launch(newProjectIntent));
        } else {
            buttonNewProject.setVisibility(View.GONE);
        }
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
                                            listProjects.addItem(projectResponse);
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

    protected void fetchProjects() {
        MultiValuePagination pagination = new MultiValuePagination();

        fetchProjectsDisposable = WebClientServiceManager.getProjectService()
                .getAuthorizedProjects(this, pagination)
                .subscribe(
                        projectResponse -> runOnUiThread(() -> {
                            listProjects.setError(null);
                            listProjects.addItem(projectResponse);
                        }),
                        error -> runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchProjects: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            listProjects.setError(error.getMessage());
                        })
                );

        reactorCompositeDisposable.add(fetchProjectsDisposable);
    }

    private void refresh() {
        runOnUiThread(() -> {
            if (fetchProjectsDisposable != null && !fetchProjectsDisposable.isDisposed()) {
                fetchProjectsDisposable.dispose();
            }
        });
        this.pageNumber = 1;
        listProjects.setObjects(new ArrayList<>());
        this.fetchProjects();
    }

    protected void startToListenToAdapterOnClick() {
        this.listenToAdapterOnClick(AUTHORIZED_PROJECT_ACTION);
    }

    protected void listenToAdapterOnClick(String action) {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String objectId = intent.getStringExtra(PARAM_STRING_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        String title = intent.getStringExtra(PARAM_TITLE);
                        if (StringUtils.isNotEmpty(objectId)) {
                            Intent accessProjectIntent = new Intent(AuthorizedProjectsActivity.this, ProjectAccessActivity.class);
                            accessProjectIntent.putExtra(PARAM_STRING_ID, objectId);
                            accessProjectIntent.putExtra(PARAM_POSITION, objectPosition);
                            accessProjectIntent.putExtra(PARAM_PROJECT_NAME, title);

                            setIntentProjectAccessType(accessProjectIntent);

                            if (formActivityResultLauncher != null) {
                                formActivityResultLauncher.launch(accessProjectIntent);
                            }
                        }
                    }
                }, new IntentFilter(action));
    }

    private void setFormActivityResultLauncher() {
        formActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::doOnBackFromForm);
    }

    protected void setIntentProjectAccessType(Intent accessProjectIntent) {
        accessProjectIntent.putExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_AUTHORIZED);
    }

    private void doOnBackFromForm(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String id = result.getData().getStringExtra(PARAM_STRING_ID);
            int position = result.getData().getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
            int formStatus = result.getData().getIntExtra(PARAM_FORM_STATUS, FORM_STATUS_NONE);
            if (StringUtils.isNotEmpty(id) && formStatus == FORM_STATUS_DONE) {
                if (position == DEFAULT_POSITION) {
                    this.fetchNewProject(id);
                } else {
                    this.fetchEditedProject(id, position);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formActivityResultLauncher = null;
    }
}