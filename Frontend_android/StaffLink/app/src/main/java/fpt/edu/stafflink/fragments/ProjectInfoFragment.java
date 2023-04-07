package fpt.edu.stafflink.fragments;

import static android.app.Activity.RESULT_OK;
import static fpt.edu.stafflink.ProjectsActivity.PROJECT_ACCESS_TYPE_CREATED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProjectInfoFragment extends BaseFragment {
    private static final String ERROR_TAG = "ProjectInfoFragment";

    TextView textViewError;
    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;

    private String id;
    private int position;
    private int accessType;

    public ProjectInfoFragment() {
        // Required empty public constructor
    }

    public static ProjectInfoFragment newInstance(String id, int position, int accessType) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_ID, id);
        args.putInt(PARAM_POSITION, position);
        args.putInt(PARAM_PROJECT_ACCESS_TYPE, accessType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.id = getArguments().getString(PARAM_ID);
            this.position = getArguments().getInt(PARAM_POSITION);
            this.accessType = getArguments().getInt(PARAM_PROJECT_ACCESS_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        textViewError = view.findViewById(R.id.textViewError);
        inputTextName = view.findViewById(R.id.inputTextName);
        inputTextDescription = view.findViewById(R.id.inputTextDescription);

        if (StringUtils.isNotEmpty(this.id)) {
            this.fetchDataOnEdit(this.id);
        }

        this.prepareProjectForm();

        return view;
    }

    private void prepareProjectForm() {
        if (this.accessType == PROJECT_ACCESS_TYPE_CREATED || StringUtils.isEmpty(this.id)) {
            inputTextName.setEditable(true);
            inputTextDescription.setEditable(true);
        } else {
            inputTextName.setEditable(false);
            inputTextDescription.setEditable(false);
        }
    }

    private void fetchDataOnEdit(String id) {
        Disposable disposable = RetrofitServiceManager.getProjectService(getContext())
                .getProject(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            ProjectResponse projectResponse = gson.fromJson(gson.toJson(responseBody), ProjectResponse.class);
                                            this.bindEditedProject(projectResponse);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnEdit: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public void submitNewProject(RequestBody projectRequestBody) {
        Disposable disposable = RetrofitServiceManager.getProjectService(getContext())
                .newProject(projectRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            ProjectResponse projectResponse = gson.fromJson(gson.toJson(responseBody), ProjectResponse.class);
                                            this.id = projectResponse.getId();
                                            this.backToProjects();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public void submitEditProject(String id, RequestBody projectRequestBody) {
        Disposable disposable = RetrofitServiceManager.getProjectService(getContext())
                .editProject(id, projectRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            this.backToProjects();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public ProjectRequest validateProject() {
        String name = inputTextName.getText().toString().trim();
        String description = inputTextDescription.getText().toString().trim();

        if (StringUtils.isEmpty(name)) {
            inputTextName.setError("name cannot be empty");
            inputTextName.requestFocus();
            return null;
        } else {
            inputTextName.setError(null);
        }

        if (StringUtils.isEmpty(description)) {
            inputTextDescription.setError("description cannot be empty");
            inputTextDescription.requestFocus();
            return null;
        } else {
            inputTextDescription.setError(null);
        }

        return new ProjectRequest(
                name,
                description);
    }

    public RequestBody buildProjectRequestBody(ProjectRequest projectRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: ProjectRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(projectRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildProjectRequestBody: " + e.getMessage(), e);
            }
        }

        return builder.build();
    }

    private void bindEditedProject(ProjectResponse projectResponse) {
        inputTextName.setText(projectResponse.getName());
        inputTextDescription.setText(projectResponse.getDescription());
    }

    private void backToProjects() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_STRING_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        getBaseActivity().setResult(RESULT_OK, intent);
        getBaseActivity().finish();
    }
}