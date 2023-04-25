package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_DONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_NONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_FORM_STATUS;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TASK_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_ASSIGNED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;
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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.fragments.BaseFragment;
import fpt.edu.stafflink.fragments.ProjectInfoFragment;
import fpt.edu.stafflink.fragments.TasksFragment;
import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class ProjectAccessActivity extends BaseActivity {
    private static final String ERROR_TAG = "ProjectAccessActivity";
    public static final String TASK_ACTION = "TaskAction";

    ImageButton buttonBackToProjects;
    TextView textViewProjectAccessTitle;
    ImageButton buttonSubmitProject;
    ImageButton buttonNewTask;
    FrameLayout fragmentProjectInfo;

    HorizontalScrollView projectAccessMenu;
    TextView projectAccessMenuInfo;
    TextView projectAccessMenuInitiated;
    TextView projectAccessMenuInProgress;
    TextView projectAccessMenuCompleted;
    TextView projectAccessMenuOverdue;

    TextView[] menuItems;

    private String projectName;
    private String projectId;
    private int position;
    private int accessType;
    private int formStatus;

    ActivityResultLauncher<Intent> formActivityResultLauncher;
    BaseFragment fragment;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_project_access);

        buttonBackToProjects = findViewById(R.id.buttonBackToProjects);
        textViewProjectAccessTitle = findViewById(R.id.textViewProjectAccessTitle);
        buttonSubmitProject = findViewById(R.id.buttonSubmitProject);
        buttonNewTask = findViewById(R.id.buttonNewTask);
        fragmentProjectInfo = findViewById(R.id.fragmentProjectInfo);

        projectAccessMenu = findViewById(R.id.projectAccessMenu);
        projectAccessMenuInfo = findViewById(R.id.projectAccessMenuInfo);
        projectAccessMenuInitiated = findViewById(R.id.projectAccessMenuInitiated);
        projectAccessMenuInProgress = findViewById(R.id.projectAccessMenuInProgress);
        projectAccessMenuCompleted = findViewById(R.id.projectAccessMenuCompleted);
        projectAccessMenuOverdue = findViewById(R.id.projectAccessMenuOverdue);

        menuItems = new TextView[] {
                projectAccessMenuInfo,
                projectAccessMenuInitiated,
                projectAccessMenuInProgress,
                projectAccessMenuCompleted,
                projectAccessMenuOverdue,
        };

        Intent intent = getIntent();
        this.projectId = intent.getStringExtra(PARAM_STRING_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
        this.accessType = intent.getIntExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);
        this.projectName = intent.getStringExtra(PARAM_PROJECT_NAME);

//        this.initByCases();
        super.authorizedFunctions.observe(this, authorizedFunctions -> {
            this.initByCases();
            super.authorizedFunctions.removeObservers(this);
        });

        this.setFormActivityResultLauncher();

        buttonBackToProjects.setOnClickListener(view -> onBackPressed());

        this.listenToAdapterOnClick();
    }

    private void initByCases() {
        if (StringUtils.isEmpty(this.projectId)) {
            this.showInfoOnNew();
        } else{
            textViewProjectAccessTitle.setText(this.projectName);

            if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED) {
                if (super.isAuthorized(getString(R.string.authorized_tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInitiated.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            } else if (this.accessType == PROJECT_ACCESS_TYPE_ASSIGNED) {
                if (super.isAuthorized(getString(R.string.assigned_tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInitiated.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            } else {
//                observable access type
                if (super.isAuthorized(getString(R.string.observable_tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInitiated.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            }
        }
    }

    private void toggleButtonNewOnMenuClicked(View menuItem) {
        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.new_task_path)) && !menuItem.equals(projectAccessMenuInfo)) {
            buttonNewTask.setVisibility(View.VISIBLE);

            Intent newTaskIntent = new Intent(ProjectAccessActivity.this, TaskAccessActivity.class);
            newTaskIntent.putExtra(PARAM_PROJECT_ACCESS_TYPE, this.accessType);
            newTaskIntent.putExtra(PARAM_PARENT_STRING_ID, this.projectId);
            buttonNewTask.setOnClickListener(view -> formActivityResultLauncher.launch(newTaskIntent));
        } else {
            buttonNewTask.setVisibility(View.GONE);
        }
    }

    private void setOnclickMenuItems() {
        int color = ContextCompat.getColor(this, R.color.project_access_menu_color);
        int highlightColor = ContextCompat.getColor(this, R.color.project_access_menu_color_highlight);
        Arrays.stream(menuItems)
                .forEach(
                        item -> item.setOnClickListener(
                                view -> {
                                    Arrays.stream(menuItems).forEach(i -> i.setBackgroundColor(color));
                                    view.setBackgroundColor(highlightColor);

                                    this.alterScreenByCase(view);
                                    this.toggleButtonNewOnMenuClicked(view);
                                }
                        )
                );
    }

    private void alterScreenByCase(View menuItem) {
        if (menuItem.equals(projectAccessMenuInfo)) {
            this.showInfoOnEdit();
        } else if (menuItem.equals(projectAccessMenuInitiated)) {
            this.showTasks(TaskStatus.INITIATED.getCode());
        } else if (menuItem.equals(projectAccessMenuInProgress)) {
            this.showTasks(TaskStatus.IN_PROGRESS.getCode());
        } else if (menuItem.equals(projectAccessMenuCompleted)) {
            this.showTasks(TaskStatus.COMPLETED.getCode());
        } else if (menuItem.equals(projectAccessMenuOverdue)) {
            this.showTasks(TaskStatus.OVERDUE.getCode());
        }
    }

    private void replaceFragment(BaseFragment fragment) {
        fragment.onAttach(getBaseContext());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentProjectInfo, fragment);
        fragmentTransaction.commit();
    }

    private void showInfoOnNew() {
        textViewProjectAccessTitle.setText(getString(R.string.project_access_title_new));
        buttonNewTask.setVisibility(View.GONE);
        projectAccessMenu.setVisibility(View.GONE);
        fragment = ProjectInfoFragment.newInstance(this.projectId, this.position, this.accessType);
        this.replaceFragment(fragment);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.new_project_path))) {
            buttonSubmitProject.setVisibility(View.VISIBLE);
            buttonSubmitProject.setOnClickListener(view -> performOnclickSubmitNew((ProjectInfoFragment) fragment));
        } else {
            buttonSubmitProject.setVisibility(View.GONE);
        }
    }

    private void showInfoOnEdit() {
        fragment = ProjectInfoFragment.newInstance(this.projectId, this.position, this.accessType);
        this.replaceFragment(fragment);
        buttonNewTask.setVisibility(View.GONE);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.edit_project_path))) {
            buttonSubmitProject.setVisibility(View.VISIBLE);
            buttonSubmitProject.setOnClickListener(view -> performOnclickSubmitEdit((ProjectInfoFragment) fragment));
        } else {
            buttonSubmitProject.setVisibility(View.GONE);
        }
    }

    private void showTasks(int status) {
        buttonSubmitProject.setVisibility(View.GONE);
        fragment = TasksFragment.newInstance(this.projectId, this.position, this.accessType, status);
        this.replaceFragment(fragment);
    }

    private void performOnclickSubmitNew(ProjectInfoFragment projectInfoFragment) {
        ProjectRequest projectRequest = projectInfoFragment.validateProject();
        if (projectRequest != null) {
            RequestBody projectRequestBody = projectInfoFragment.buildProjectRequestBody(projectRequest);
            projectInfoFragment.submitNewProject(projectRequestBody);
        }
    }

    private void performOnclickSubmitEdit(ProjectInfoFragment projectInfoFragment) {
        ProjectRequest projectRequest = projectInfoFragment.validateProject();
        if (projectRequest != null) {
            RequestBody projectRequestBody = projectInfoFragment.buildProjectRequestBody(projectRequest);
            projectInfoFragment.submitEditProject(this.projectId, projectRequestBody);
        }
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setFormStatus(int formStatus) {
        this.formStatus = formStatus;
    }

    private void fetchNewTask(String id) {
        io.reactivex.disposables.Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .getTask(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            TaskResponse taskResponse = gson.fromJson(gson.toJson(responseBody), TaskResponse.class);
                                            if (this.fragment instanceof TasksFragment && TaskStatus.INITIATED.getCode() == ((TasksFragment) this.fragment).getTaskStatus()) {
                                                CustomListComponent<TaskResponse> listTasks = ((TasksFragment) this.fragment).getListTasks();

                                                listTasks.adapter.addNewItem(taskResponse);
                                                listTasks.scrollTo(listTasks.getObjects().indexOf(taskResponse));
                                                listTasks.setError(null);
                                            } else {
                                                this.performClickByStatus(TaskStatus.INITIATED.getCode());
                                                ((TasksFragment) this.fragment).setOnListInitiatedListener(() -> {
                                                    CustomListComponent<TaskResponse> listTasks = ((TasksFragment) this.fragment).getListTasks();

                                                    listTasks.scrollTo(listTasks.getObjects().indexOf(taskResponse));
                                                    listTasks.setError(null);
                                                    System.out.println("done");
                                                });
                                            }
                                        },
                                        errorApiResponse -> this.setFragmentError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchNewTask: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            this.setFragmentError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchEditedTask(String id, int position) {
        io.reactivex.disposables.Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .getTask(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            TaskResponse taskResponse = gson.fromJson(gson.toJson(responseBody), TaskResponse.class);
                                            if (this.fragment instanceof TasksFragment && taskResponse.getStatusCode() == ((TasksFragment) this.fragment).getTaskStatus()) {
                                                CustomListComponent<TaskResponse> listTasks = ((TasksFragment) this.fragment).getListTasks();

                                                listTasks.adapter.modifyItem(position, taskResponse);
                                                listTasks.scrollTo(position);
                                                listTasks.setError(null);
                                            } else {
                                                performClickByStatus(taskResponse.getStatusCode());
                                                ((TasksFragment) this.fragment).setOnListInitiatedListener(() -> {
                                                    CustomListComponent<TaskResponse> listTasks = ((TasksFragment) this.fragment).getListTasks();
                                                    listTasks.scrollTo(listTasks.getObjects().indexOf(taskResponse));
                                                    listTasks.setError(null);
                                                });
                                            }
                                        },
                                        errorApiResponse -> this.setFragmentError(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchEditedTask: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            this.setFragmentError(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void setFragmentError(String error) {
        if (this.fragment instanceof TasksFragment) {
            CustomListComponent<TaskResponse> listTasks = ((TasksFragment) this.fragment).getListTasks();
            listTasks.setError(error);
        } else if (this.fragment instanceof ProjectInfoFragment) {
            ((ProjectInfoFragment) this.fragment).setTextViewError(error);
        }
    }

    private void performClickByStatus(int status) {
        if (status == TaskStatus.INITIATED.getCode()) {
            projectAccessMenu.scrollTo(projectAccessMenuInitiated.getLeft(), 0);
            projectAccessMenuInitiated.performClick();
        } else if (status == TaskStatus.IN_PROGRESS.getCode()) {
            projectAccessMenu.scrollTo(projectAccessMenuInProgress.getLeft(), 0);
            projectAccessMenuInProgress.performClick();
        } else if (status == TaskStatus.COMPLETED.getCode()) {
            projectAccessMenu.scrollTo(projectAccessMenuCompleted.getLeft(), 0);
            projectAccessMenuCompleted.performClick();
        } else if (status == TaskStatus.OVERDUE.getCode()) {
            projectAccessMenu.scrollTo(projectAccessMenuOverdue.getLeft(), 0);
            projectAccessMenuOverdue.performClick();
        }
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String objectId = intent.getStringExtra(PARAM_STRING_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        String title = intent.getStringExtra(PARAM_TITLE);
                        if (StringUtils.isNotEmpty(objectId)) {
                            Intent taskIntend = new Intent(ProjectAccessActivity.this, TaskAccessActivity.class);
                            taskIntend.putExtra(PARAM_PARENT_STRING_ID, projectId);
                            taskIntend.putExtra(PARAM_STRING_ID, objectId);
                            taskIntend.putExtra(PARAM_POSITION, objectPosition);

                            taskIntend.putExtra(PARAM_PROJECT_NAME, projectName);
                            taskIntend.putExtra(PARAM_TASK_NAME, title);

                            taskIntend.putExtra(PARAM_PROJECT_ACCESS_TYPE, accessType);

                            if (formActivityResultLauncher != null) {
                                formActivityResultLauncher.launch(taskIntend);
                            }
                        }
                    }
                }, new IntentFilter(TASK_ACTION));
    }

    private void setFormActivityResultLauncher() {
        formActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::doOnBackFromForm);
    }

    private void doOnBackFromForm(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String id = result.getData().getStringExtra(PARAM_STRING_ID);
            int position = result.getData().getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
            int formStatus = result.getData().getIntExtra(PARAM_FORM_STATUS, FORM_STATUS_NONE);
            if (StringUtils.isNotEmpty(id) && formStatus == FORM_STATUS_DONE) {
                if (position == DEFAULT_POSITION) {
                    this.fetchNewTask(id);
                } else {
                    this.fetchEditedTask(id, position);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_STRING_ID, this.projectId);
        intent.putExtra(PARAM_POSITION, this.position);
        intent.putExtra(PARAM_FORM_STATUS, this.formStatus);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formActivityResultLauncher = null;
    }
}