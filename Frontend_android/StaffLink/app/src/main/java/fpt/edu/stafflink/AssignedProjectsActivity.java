package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_ASSIGNED;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientServiceManager;

public class AssignedProjectsActivity extends AuthorizedProjectsActivity {
    private static final String ASSIGNED_PROJECT_ACTION = "AssignedProjectAction";

    @Override
    protected void initTitle() {
        ProjectsActivityTitle.setText(getString(R.string.assigned_projects_title));
    }

    @Override
    protected void initTable() {
        listProjects.setTitleField("name");
        listProjects.setContentField("description");
        listProjects.setAction(ASSIGNED_PROJECT_ACTION);
        listProjects.setError(null);
    }

    @Override
    protected void prepareButtonNew() {
        buttonNewProject.setVisibility(View.GONE);
    }

    @Override
    protected void fetchProjects() {
        MultiValuePagination pagination = new MultiValuePagination();

        reactor.core.Disposable disposable = WebClientServiceManager.getProjectService()
                .getAssignedProjects(this, pagination)
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

    @Override
    protected void startToListenToAdapterOnClick() {
        super.listenToAdapterOnClick(ASSIGNED_PROJECT_ACTION);
    }

    @Override
    protected void setIntentProjectAccessType(Intent accessProjectIntent) {
        accessProjectIntent.putExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_ASSIGNED);
    }
}
