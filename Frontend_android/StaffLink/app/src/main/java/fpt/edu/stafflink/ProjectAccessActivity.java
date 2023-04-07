package fpt.edu.stafflink;

import static fpt.edu.stafflink.ProjectsActivity.PROJECT_ACCESS_TYPE_OBSERVABLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.apache.commons.lang3.StringUtils;

import fpt.edu.stafflink.fragments.ProjectInfoFragment;
import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import okhttp3.RequestBody;

public class ProjectAccessActivity extends BaseActivity {
    private static final String ERROR_TAG = "ProjectAccessActivity";

    ImageButton buttonBackToProjects;
    TextView textViewProjectAccessTitle;
    ImageButton buttonSubmitProject;
    FrameLayout fragmentProjectInfo;

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

        Intent intent = getIntent();
        this.id = intent.getStringExtra(PARAM_STRING_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
        this.accessType = intent.getIntExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);

        System.out.println(this.accessType);

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        ProjectInfoFragment projectInfoFragment = ProjectInfoFragment.newInstance(this.id, this.position, this.accessType);
        fragmentTransaction.replace(R.id.fragmentProjectInfo, projectInfoFragment);
        fragmentTransaction.commit();

        buttonBackToProjects.setOnClickListener(view -> onBackPressed());

        buttonSubmitProject.setOnClickListener(view -> {
            ProjectRequest projectRequest = projectInfoFragment.validateProject();
            if (projectRequest != null) {
                RequestBody projectRequestBody = projectInfoFragment.buildProjectRequestBody(projectRequest);
                if (StringUtils.isNotEmpty(this.id)) {
                    projectInfoFragment.submitEditProject(this.id, projectRequestBody);
                } else {
                    projectInfoFragment.submitNewProject(projectRequestBody);
                }
            }
        });
    }



}