package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_DONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.BaseActivity;
import fpt.edu.stafflink.ProjectAccessActivity;
import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.components.CustomSelectedListComponent;
import fpt.edu.stafflink.debouncing.Debouncer;
import fpt.edu.stafflink.models.others.SelectedUser;
import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProjectInfoFragment extends BaseFragment {
    private static final String ERROR_TAG = "ProjectInfoFragment";
    private static final String SELECT_USER_ACTION = "searchUserAction";
    private static final int MIN_SEARCH_LENGTH = 4;
    private static final int DEBOUNCING_DELAY = 1000;

    TextView textViewError;
    ScrollView scrollViewWrapper;
    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;
    CustomInputTextComponent inputTextCreateBy;
    CustomSelectedListComponent<SelectedUser> selectedListUsers;
    CustomInputTextComponent inputTextSearchUsers;
    CustomListComponent<UserResponse> listUsers;

    private String id;
    private int position;
    private int accessType;

    private UserResponse createdBy;

    ProjectAccessActivity projectAccessActivity;

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
        scrollViewWrapper = view.findViewById(R.id.scrollViewWrapper);
        inputTextName = view.findViewById(R.id.inputTextName);
        inputTextDescription = view.findViewById(R.id.inputTextDescription);
        inputTextCreateBy = view.findViewById(R.id.inputTextCreateBy);

        selectedListUsers = view.findViewById(R.id.selectedListUsers);
        inputTextSearchUsers = view.findViewById(R.id.inputTextSearchUsers);
        listUsers = view.findViewById(R.id.listUsers);

        if (StringUtils.isNotEmpty(this.id)) {
            this.fetchDataOnEdit(this.id);
        } else {
            UserResponse authUser = super.getBaseActivity().getAuthUser();
            if (authUser != null) {
                this.bindCreatedBy(authUser);
            }
        }

        this.prepareProjectForm();
        this.initSearchList();
        this.initSelectedList();
        this.listenToAdapterOnClick();
        this.setProjectAccessActivity();

        return view;
    }

    private void initSearchList() {
        listUsers.setTitleField("name");
        listUsers.setContentField("username");
        listUsers.setAction(SELECT_USER_ACTION);
        listUsers.setError(null);
    }

    private void initSelectedList() {
        selectedListUsers.setMainField("name");
    }

    private void prepareProjectForm() {
        if (
                (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && getBaseActivity().isAuthorized(getString(R.string.edit_project_path))) ||
                StringUtils.isEmpty(this.id)
        ) {
            inputTextName.setEditable(true);
            inputTextDescription.setEditable(true);
            selectedListUsers.setCancellable(true);
            inputTextSearchUsers.setVisibility(View.VISIBLE);

            Debouncer debouncer = new Debouncer();
            inputTextSearchUsers.setOnTextChanged(new TextWatcher() {
                CharSequence searchText;
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    searchText = charSequence;
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    debouncer.call(() -> getBaseActivity().runOnUiThread(() -> searchUsers(searchText.toString().trim())), DEBOUNCING_DELAY);
                }
            });
        } else {
            inputTextName.setEditable(false);
            inputTextDescription.setEditable(false);
            selectedListUsers.setCancellable(false);
            inputTextSearchUsers.setVisibility(View.GONE);
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
                                        this.fetchCreatedBy(projectResponse.getCreatedBy());
                                        projectResponse.getUserIds()
                                                .forEach(this::fetchAuthorizedUser);
                                    },
                                    errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                            ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchDataOnEdit: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    private void fetchCreatedBy(int userId) {
        if (userId == DEFAULT_ID) {
            this.bindCreatedBy(null);
            return;
        }
        Disposable disposable = RetrofitServiceManager.getUserService(getContext())
                .getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            this.bindCreatedBy(userResponse);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchCreatedBy: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    private void fetchAuthorizedUser(int userId) {
        Disposable disposable = RetrofitServiceManager.getUserService(getContext())
                .getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            this.bindAuthorizedUser(userResponse);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchAuthorizedUser: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    private void searchUsers(String search) {
        if (StringUtils.isEmpty(search) || search.length() < MIN_SEARCH_LENGTH) {
            listUsers.setError(null);
            listUsers.setObjects(new ArrayList<>());
            return;
        }
        Pagination pagination = new Pagination();

        Disposable disposable = RetrofitServiceManager.getUserService(getContext())
                .getUsers(search, pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listUsers.setError(null);
                                            Type type = new TypeToken<List<UserResponse>>() {}.getType();
                                            List<UserResponse> userResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            List<UserResponse> userResponsesExceptCreatedBy = userResponses.stream()
                                                            .filter(userResponse -> this.createdBy == null || userResponse.getId() != this.createdBy.getId())
                                                            .collect(Collectors.toList());

                                            listUsers.setObjects(userResponsesExceptCreatedBy);
                                            if (scrollViewWrapper.getScrollY() < 200) {
                                                scrollViewWrapper.smoothScrollBy(0, 400);
                                            }
                                        },
                                        errorApiResponse -> {
                                            listUsers.setError(errorApiResponse.getMessage());
                                            listUsers.setObjects(new ArrayList<>());
                                        }
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "SearchUsers: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listUsers.setError(error.getMessage());
                            listUsers.setObjects(new ArrayList<>());
                        }
                );

        getBaseActivity().compositeDisposable.add(disposable);
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
                                            super.getBaseActivity().pushToast("project added successfully");
                                            if (this.projectAccessActivity != null) {
                                                this.projectAccessActivity.setFormStatus(FORM_STATUS_DONE);
                                                this.projectAccessActivity.setProjectId(projectResponse.getId());
                                            }
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
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
                                            super.getBaseActivity().pushToast("project edited successfully");
                                            if (this.projectAccessActivity != null) {
                                                this.projectAccessActivity.setFormStatus(FORM_STATUS_DONE);
                                            }
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
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

        List<Integer> userIds = selectedListUsers.getObjects()
                .stream()
                .map(SelectedUser::getId)
                .collect(Collectors.toList());

        return new ProjectRequest(
                name,
                description,
                userIds);
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

        projectRequest.getUserIds()
                .forEach(userId -> builder.addFormDataPart("userIds", String.valueOf(userId)));

        return builder.build();
    }

    private void bindEditedProject(ProjectResponse projectResponse) {
        inputTextName.setText(projectResponse.getName());
        inputTextDescription.setText(projectResponse.getDescription());

        if (this.projectAccessActivity != null) {
            this.projectAccessActivity.setTitleOnEdit(projectResponse.getName());
        }
    }
    private void bindCreatedBy(UserResponse userResponse) {
        if (userResponse == null) {
            inputTextCreateBy.setText(getString(R.string.unidentified));
        } else {
            inputTextCreateBy.setText(userResponse.getName());
            this.createdBy = userResponse;
        }
    }
    private void bindAuthorizedUser(UserResponse userResponse) {
        if (userResponse != null) {
            SelectedUser selectedUser = new SelectedUser(userResponse.getId(), userResponse.getName());
            selectedListUsers.addNewItem(selectedUser);
        }
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(super.retrieveContext())
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int id = intent.getIntExtra(PARAM_ID, DEFAULT_ID);
                        String title = intent.getStringExtra(PARAM_TITLE);
                        if (id != DEFAULT_ID) {
                            SelectedUser selectedUser = new SelectedUser(id, title);
                            performSelectUser(selectedUser);
                        }
                    }
                }, new IntentFilter(SELECT_USER_ACTION));
    }

    private void performSelectUser(SelectedUser selectedUser) {
        selectedListUsers.addNewItem(selectedUser);
    }

    private void setProjectAccessActivity() {
        BaseActivity baseActivity = super.getBaseActivity();
        if (baseActivity instanceof ProjectAccessActivity) {
            this.projectAccessActivity = (ProjectAccessActivity) baseActivity;
        }
    }
}