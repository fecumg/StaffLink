package fpt.edu.stafflink;

import static fpt.edu.stafflink.ProjectsActivity.PROJECT_ACCESS_TYPE_ASSIGNED;
import static fpt.edu.stafflink.ProjectsActivity.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.ProjectsActivity.PROJECT_ACCESS_TYPE_OBSERVABLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.fragments.ProjectInfoFragment;
import fpt.edu.stafflink.fragments.TasksFragment;
import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import okhttp3.RequestBody;

public class ProjectAccessActivity extends BaseActivity {
    private static final String ERROR_TAG = "ProjectAccessActivity";

    ImageButton buttonBackToProjects;
    TextView textViewProjectAccessTitle;
    ImageButton buttonSubmitProject;
    FrameLayout fragmentProjectInfo;

    HorizontalScrollView projectAccessMenu;
    TextView projectAccessMenuInfo;
    TextView projectAccessMenuInitiated;
    TextView projectAccessMenuInProgress;
    TextView projectAccessMenuPending;
    TextView projectAccessMenuCompleted;
    TextView projectAccessMenuOverdue;
    TextView projectAccessMenuFailed;

    TextView[] menuItems;

    private String id;
    private int position;
    private int accessType;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_project_access);

        buttonBackToProjects = findViewById(R.id.buttonBackToProjects);
        textViewProjectAccessTitle = findViewById(R.id.textViewProjectAccessTitle);
        buttonSubmitProject = findViewById(R.id.buttonSubmitProject);
        fragmentProjectInfo = findViewById(R.id.fragmentProjectInfo);

        projectAccessMenu = findViewById(R.id.projectAccessMenu);
        projectAccessMenuInfo = findViewById(R.id.projectAccessMenuInfo);
        projectAccessMenuInitiated = findViewById(R.id.projectAccessMenuInitiated);
        projectAccessMenuInProgress = findViewById(R.id.projectAccessMenuInProgress);
        projectAccessMenuPending = findViewById(R.id.projectAccessMenuPending);
        projectAccessMenuCompleted = findViewById(R.id.projectAccessMenuCompleted);
        projectAccessMenuOverdue = findViewById(R.id.projectAccessMenuOverdue);
        projectAccessMenuFailed = findViewById(R.id.projectAccessMenuFailed);

        menuItems = new TextView[] {
                projectAccessMenuInfo,
                projectAccessMenuInitiated,
                projectAccessMenuInProgress,
                projectAccessMenuPending,
                projectAccessMenuCompleted,
                projectAccessMenuOverdue,
                projectAccessMenuFailed
        };

        Intent intent = getIntent();
        this.id = intent.getStringExtra(PARAM_STRING_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
        this.accessType = intent.getIntExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);

        this.initByCase();

        buttonBackToProjects.setOnClickListener(view -> onBackPressed());
    }

    private void initByCase() {
        if (StringUtils.isEmpty(this.id)) {
            this.showInfoOnNew();
        } else{
            if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED) {
                if (super.isAuthorized(getString(R.string.authorized_tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInfo.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            } else if (this.accessType == PROJECT_ACCESS_TYPE_ASSIGNED) {
                if (super.isAuthorized(getString(R.string.assigned_tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInfo.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            } else {
//                observable access type
                if (super.isAuthorized(getString(R.string.tasks_path))) {
                    projectAccessMenu.setVisibility(View.VISIBLE);
                    this.setOnclickMenuItems();
                    projectAccessMenuInfo.performClick();
                } else {
                    projectAccessMenu.setVisibility(View.GONE);
                    this.showInfoOnEdit();
                }
            }
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
        } else if (menuItem.equals(projectAccessMenuPending)) {
            this.showTasks(TaskStatus.PENDING.getCode());
        } else if (menuItem.equals(projectAccessMenuCompleted)) {
            this.showTasks(TaskStatus.COMPLETED.getCode());
        } else if (menuItem.equals(projectAccessMenuOverdue)) {
            this.showTasks(TaskStatus.OVERDUE.getCode());
        } else if (menuItem.equals(projectAccessMenuFailed)) {
            this.showTasks(TaskStatus.FAILED.getCode());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentProjectInfo, fragment);
        fragmentTransaction.commit();
    }

    private void showInfoOnNew() {
        projectAccessMenu.setVisibility(View.GONE);
        ProjectInfoFragment projectInfoFragment = ProjectInfoFragment.newInstance(this.id, this.position, this.accessType);
        this.replaceFragment(projectInfoFragment);

        if (super.isAuthorized(getString(R.string.new_project_path))) {
            buttonSubmitProject.setVisibility(View.VISIBLE);
            buttonSubmitProject.setOnClickListener(view -> performOnclickSubmitNew(projectInfoFragment));
        } else {
            buttonSubmitProject.setVisibility(View.GONE);
        }
    }

    private void showInfoOnEdit() {
        ProjectInfoFragment projectInfoFragment = ProjectInfoFragment.newInstance(this.id, this.position, this.accessType);
        this.replaceFragment(projectInfoFragment);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.edit_project_path))) {
            buttonSubmitProject.setVisibility(View.VISIBLE);
            buttonSubmitProject.setOnClickListener(view -> performOnclickSubmitEdit(projectInfoFragment));
        } else {
            buttonSubmitProject.setVisibility(View.GONE);
        }
    }

    private void showTasks(int status) {
        TasksFragment tasksFragment = TasksFragment.newInstance(this.id, this.position, this.accessType, status);
        this.replaceFragment(tasksFragment);
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
            projectInfoFragment.submitEditProject(this.id, projectRequestBody);
        }
    }

}