package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_FORM_STATUS;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TASK_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_ASSIGNED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;

import android.content.Intent;
import android.content.res.ColorStateList;
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

import fpt.edu.stafflink.fragments.AttachmentsFragment;
import fpt.edu.stafflink.fragments.BaseFragment;
import fpt.edu.stafflink.fragments.ChecklistFragment;
import fpt.edu.stafflink.fragments.CommentsFragment;
import fpt.edu.stafflink.fragments.TaskInfoFragment;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.EditStatusRequest;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.EditTaskRequest;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.NewTaskRequest;
import okhttp3.RequestBody;

public class TaskAccessActivity extends BaseActivity {

    ImageButton buttonBack;
    ImageButton buttonSubmitTask;
    TextView textViewTaskAccessTitle;

    FrameLayout fragmentTaskInfo;

    HorizontalScrollView taskAccessMenu;
    TextView taskAccessMenuInfo;
    TextView taskAccessMenuAttachments;
    TextView taskAccessMenuChecklist;
    TextView taskAccessMenuComments;

    TextView[] menuItems;

    private String projectName;
    private String taskName;
    private String projectId;
    private String id;
    private int position;
    private int accessType;
    private int formStatus;

    BaseFragment fragment;

    View currentMenuItem;

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_task_access);

        buttonBack = findViewById(R.id.buttonBack);
        textViewTaskAccessTitle = findViewById(R.id.textViewTaskAccessTitle);
        buttonSubmitTask = findViewById(R.id.buttonSubmitTask);
        fragmentTaskInfo = findViewById(R.id.fragmentTaskInfo);

        taskAccessMenu = findViewById(R.id.taskAccessMenu);
        taskAccessMenuInfo = findViewById(R.id.taskAccessMenuInfo);
        taskAccessMenuAttachments = findViewById(R.id.taskAccessMenuAttachments);
        taskAccessMenuChecklist = findViewById(R.id.taskAccessMenuChecklist);
        taskAccessMenuComments = findViewById(R.id.taskAccessMenuComments);

        menuItems = new TextView[] {
                taskAccessMenuInfo,
                taskAccessMenuAttachments,
                taskAccessMenuChecklist,
                taskAccessMenuComments
        };

        Intent intent = getIntent();
        this.projectId = intent.getStringExtra(PARAM_PARENT_STRING_ID);
        this.id = intent.getStringExtra(PARAM_STRING_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
        this.accessType = intent.getIntExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);
        this.projectName = intent.getStringExtra(PARAM_PROJECT_NAME);
        this.taskName = intent.getStringExtra(PARAM_TASK_NAME);

//        this.initByCases();
        super.authorizedFunctions.observe(this, authorizedFunctions -> {
            this.initByCases();
            super.authorizedFunctions.removeObservers(this);
        });

        buttonBack.setOnClickListener(view -> onBackPressed());
    }

    private void initByCases() {
        if (StringUtils.isEmpty(this.id)) {
            this.showInfoOnNew();
        } else{
            String title;
            if (StringUtils.isNotEmpty(this.projectName)) {
                title = this.projectName + "/\n" + this.taskName;
            } else {
                title = this.taskName;
            }
            textViewTaskAccessTitle.setText(title);

            taskAccessMenu.setVisibility(View.VISIBLE);
            if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
                taskAccessMenuComments.setVisibility(View.GONE);
            }

            this.setOnclickMenuItems();
            taskAccessMenuInfo.performClick();
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
        if (menuItem.equals(currentMenuItem)) {
            return;
        }
        if (menuItem.equals(taskAccessMenuInfo)) {
            this.showInfoOnEdit();
        } else if (menuItem.equals(taskAccessMenuAttachments)) {
//            show attachments
            this.showAttachments();
        } else if (menuItem.equals(taskAccessMenuChecklist)) {
//            show checklist
            this.showChecklist();
        } else if (menuItem.equals(taskAccessMenuComments)) {
//            show comments
            this.showComments();
        }
        currentMenuItem = menuItem;
    }

    private void replaceFragment(BaseFragment fragment) {
        if (fragment == null ) return;

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentTaskInfo);
        if(currentFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        }
        fragment.onAttach(getBaseContext());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentTaskInfo, fragment);
        fragmentTransaction.commit();
    }

    private void showInfoOnNew() {
        String title = this.projectName + "/\n" + getString(R.string.task_access_title_new);
        textViewTaskAccessTitle.setText(title);
        taskAccessMenu.setVisibility(View.GONE);
        fragment = TaskInfoFragment.newInstance(this.projectId, this.id, this.position, this.accessType);
        this.replaceFragment(fragment);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.new_task_path))) {
            buttonSubmitTask.setVisibility(View.VISIBLE);
            buttonSubmitTask.setOnClickListener(view -> performOnclickSubmitNew((TaskInfoFragment) fragment));
        } else {
            buttonSubmitTask.setVisibility(View.GONE);
        }
    }

    private void showInfoOnEdit() {
        taskAccessMenu.setVisibility(View.VISIBLE);
        fragment = TaskInfoFragment.newInstance(this.projectId, this.id, this.position, this.accessType);
        this.replaceFragment(fragment);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.edit_task_path))) {
            buttonSubmitTask.setVisibility(View.VISIBLE);
            buttonSubmitTask.setOnClickListener(view -> performOnclickSubmitEdit((TaskInfoFragment) fragment));
        } else if (this.accessType == PROJECT_ACCESS_TYPE_ASSIGNED) {
            buttonSubmitTask.setVisibility(View.VISIBLE);
            buttonSubmitTask.setOnClickListener(view -> performOnclickSubmitEditStatus((TaskInfoFragment) fragment));
        } else {
            buttonSubmitTask.setVisibility(View.GONE);
        }
    }

    private void showAttachments() {
        fragment = AttachmentsFragment.newInstance(this.projectId, this.id, this.position, this.accessType);
        this.replaceFragment(fragment);
        buttonSubmitTask.setVisibility(View.GONE);
    }

    private void showChecklist() {
        fragment = ChecklistFragment.newInstance(this.projectId, this.id, this.position, this.accessType);
        this.replaceFragment(fragment);
        buttonSubmitTask.setVisibility(View.GONE);
    }

    private void showComments() {
        fragment = CommentsFragment.newInstance(this.projectId, this.id, this.position, this.accessType);
        this.replaceFragment(fragment);
        buttonSubmitTask.setVisibility(View.GONE);
    }

    private void performOnclickSubmitNew(TaskInfoFragment taskInfoFragment) {
        NewTaskRequest newTaskRequest = taskInfoFragment.validateNewTask();
        if (newTaskRequest != null) {
            RequestBody newTaskRequestBody = taskInfoFragment.buildNewTaskRequestBody(newTaskRequest);
            taskInfoFragment.submitNewTask(newTaskRequestBody);
        }
    }

    private void performOnclickSubmitEdit(TaskInfoFragment taskInfoFragment) {
        EditTaskRequest editTaskRequest = taskInfoFragment.validateEditTask();
        if (editTaskRequest != null) {
            RequestBody editTaskRequestBody = taskInfoFragment.buildEditTaskRequestBody(editTaskRequest);
            taskInfoFragment.submitEditTask(this.id, editTaskRequestBody);
        }
    }

    private void performOnclickSubmitEditStatus(TaskInfoFragment taskInfoFragment) {
        EditStatusRequest editStatusRequest = taskInfoFragment.validateEditStatus();
        RequestBody editStatusRequestBody = taskInfoFragment.buildEditStatusRequestBody(editStatusRequest);
        taskInfoFragment.submitEditStatus(this.id, editStatusRequestBody);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFormStatus(int formStatus) {
        this.formStatus = formStatus;
    }

    public void enableSubmitNew() {
        buttonSubmitTask.setClickable(true);
        buttonSubmitTask.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
    }

    public void disableSubmitNew() {
        buttonSubmitTask.setClickable(false);
        buttonSubmitTask.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary)));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_STRING_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        intent.putExtra(PARAM_FORM_STATUS, this.formStatus);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}