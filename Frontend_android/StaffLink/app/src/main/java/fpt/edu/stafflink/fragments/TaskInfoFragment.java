package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_DONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_ASSIGNED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.BaseActivity;
import fpt.edu.stafflink.R;
import fpt.edu.stafflink.TaskAccessActivity;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.components.CustomSelectComponent;
import fpt.edu.stafflink.components.CustomSelectedListComponent;
import fpt.edu.stafflink.debouncing.Debouncer;
import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.models.others.SearchedUser;
import fpt.edu.stafflink.models.others.SelectedUser;
import fpt.edu.stafflink.models.others.TaskStatusDto;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.EditStatusRequest;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.EditTaskRequest;
import fpt.edu.stafflink.models.requestDtos.taskRequestDtos.NewTaskRequest;
import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.DateUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TaskInfoFragment extends BaseFragment{
    private static final String ERROR_TAG = "TaskInfoFragment";
    private static final String SELECT_ASSIGNED_USER_ACTION = "selectAssignedUserAction";
    private static final int MIN_SEARCH_LENGTH = 4;
    private static final int DEBOUNCING_DELAY = 1000;

    TextView textViewError;

    NestedScrollView scrollViewWrapper;
    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;
    CustomInputTextComponent inputTextCreatedAt;
    CustomInputTextComponent inputTextDueDate;
    CustomSelectComponent<TaskStatusDto> selectStatus;
    CustomInputTextComponent inputTextCreateBy;
    CustomSelectedListComponent<SelectedUser> selectedListUsers;
    CustomInputTextComponent inputTextSearchUsers;
    CustomListComponent<SearchedUser> listUsers;

    private String projectId;
    private String id;
    private int position;
    private int accessType;

    private UserResponse createdBy;

    final Calendar calendar= Calendar.getInstance();

    TaskAccessActivity taskAccessActivity;

    public TaskInfoFragment() {
        // Required empty public constructor
    }

    public static TaskInfoFragment newInstance(String projectId, String id, int position, int accessType) {
        TaskInfoFragment fragment = new TaskInfoFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_PARENT_STRING_ID, projectId);
        args.putString(PARAM_STRING_ID, id);
        args.putInt(PARAM_POSITION, position);
        args.putInt(PARAM_PROJECT_ACCESS_TYPE, accessType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.projectId = getArguments().getString(PARAM_PARENT_STRING_ID);
            this.id = getArguments().getString(PARAM_STRING_ID);
            this.position = getArguments().getInt(PARAM_POSITION);
            this.accessType = getArguments().getInt(PARAM_PROJECT_ACCESS_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_info, container, false);

        textViewError = view.findViewById(R.id.textViewError);
        scrollViewWrapper = view.findViewById(R.id.scrollViewWrapper);
        inputTextName = view.findViewById(R.id.inputTextName);
        inputTextDescription = view.findViewById(R.id.inputTextDescription);
        inputTextCreatedAt = view.findViewById(R.id.inputTextCreatedAt);
        inputTextDueDate = view.findViewById(R.id.inputTextDueDate);
        selectStatus = view.findViewById(R.id.selectStatus);
        inputTextCreateBy = view.findViewById(R.id.inputTextCreateBy);

        selectedListUsers = view.findViewById(R.id.selectedListUsers);
        inputTextSearchUsers = view.findViewById(R.id.inputTextSearchUsers);
        listUsers = view.findViewById(R.id.listUsers);

        listUsers.setNestedScrollingEnabled(false);

        if (StringUtils.isNotEmpty(this.id)) {
            this.fetchDataOnEdit(this.id);
        } else {
            getBaseActivity().authUser.observe(getBaseActivity(), authorizedFunctions -> {
                if (getBaseActivity().getAuthUser() != null) {
                    bindCreatedBy(getBaseActivity().getAuthUser());
                }
                getBaseActivity().authUser.removeObservers(getBaseActivity());
            });
        }

        this.prepareTaskForm();
        this.initSelectStatus();
        this.initInputTextDueDate();
        this.initSearchList();
        this.initSelectedList();
        this.listenToAdapterOnClick();
        this.setTaskAccessActivity();

        return view;
    }

    private void initSearchList() {
        listUsers.setTitleField("name");
        listUsers.setContentField("roles");
        listUsers.setAction(SELECT_ASSIGNED_USER_ACTION);
        listUsers.setError(null);
    }

    private void initSelectedList() {
        selectedListUsers.setMainField("name");
    }

    private void prepareTaskForm() {
        if (
                (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && getBaseActivity().isAuthorized(getString(R.string.edit_task_path))) ||
                        StringUtils.isEmpty(this.id)
        ) {
            inputTextName.setEditable(true);
            inputTextDescription.setEditable(true);
            inputTextDueDate.setEditable(true);
            selectedListUsers.setCancellable(true);
            inputTextSearchUsers.setVisibility(View.VISIBLE);
            selectStatus.setEditable(true);

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

            if (StringUtils.isEmpty(this.id)) {
                Date presentDate = new Date();
                inputTextCreatedAt.setText(DateUtils.dateToString(presentDate, getString(R.string.date_pattern)));
                calendar.setTime(new Date());
                calendar.add(Calendar.WEEK_OF_YEAR, 2);
                Date autoGeneratedDueAt = calendar.getTime();
                inputTextDueDate.setText(DateUtils.dateToString(autoGeneratedDueAt, getString(R.string.date_pattern)));
            }
        } else {
            inputTextName.setEditable(false);
            inputTextDescription.setEditable(false);
            inputTextDueDate.setEditable(false);
            selectedListUsers.setCancellable(false);
            inputTextSearchUsers.setVisibility(View.GONE);

            selectStatus.setEditable(this.accessType == PROJECT_ACCESS_TYPE_ASSIGNED);
        }
    }

    private void initSelectStatus() {
        if (StringUtils.isNotEmpty(this.id)) {
            selectStatus.setVisibility(View.VISIBLE);

            selectStatus.setMainField("message");

            selectStatus.setOnSelectHandler((view) -> {
                if (selectStatus.getSelectedOption().getCode() == TaskStatus.INITIATED.getCode()) {
                    selectStatus.setSpinnerColor(ContextCompat.getColor(super.retrieveContext(), R.color.primary_light));
                    ((TextView) view).setTextColor(ContextCompat.getColor(super.retrieveContext(), R.color.light));
                } else if (selectStatus.getSelectedOption().getCode() == TaskStatus.IN_PROGRESS.getCode()) {
                    selectStatus.setSpinnerColor(ContextCompat.getColor(super.retrieveContext(), R.color.warning_light));
                    ((TextView) view).setTextColor(ContextCompat.getColor(super.retrieveContext(), R.color.dark));
                } else if (selectStatus.getSelectedOption().getCode() == TaskStatus.COMPLETED.getCode()) {
                    selectStatus.setSpinnerColor(ContextCompat.getColor(super.retrieveContext(), R.color.success_light));
                    ((TextView) view).setTextColor(ContextCompat.getColor(super.retrieveContext(), R.color.light));
                } else if (selectStatus.getSelectedOption().getCode() == TaskStatus.OVERDUE.getCode()) {
                    selectStatus.setSpinnerColor(ContextCompat.getColor(super.retrieveContext(), R.color.danger_light));
                    ((TextView) view).setTextColor(ContextCompat.getColor(super.retrieveContext(), R.color.light));
                }
            });
        } else {
            selectStatus.setVisibility(View.GONE);
        }
    }

    private void initInputTextDueDate() {
        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            inputTextDueDate.setText(DateUtils.dateToString(calendar.getTime(), getString(R.string.date_pattern)));
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        inputTextDueDate.setOnClick(view -> datePickerDialog.show());
        inputTextDueDate.setOnFocus((view, b) -> {
            if (b) {
                datePickerDialog.show();
            }
        });
        inputTextDueDate.setOnTextChanged(new TextWatcher() {
            CharSequence previousCharSequence;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousCharSequence = charSequence;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!DateUtils.validate(charSequence.toString().trim(), getBaseActivity().getString(R.string.date_pattern))) {
                    datePickerDialog.show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fetchDataOnEdit(String id) {
        Disposable disposable = RetrofitServiceManager.getTaskService(getContext())
                .getTask(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            TaskResponse taskResponse = gson.fromJson(gson.toJson(responseBody), TaskResponse.class);
                                            this.bindEditedTask(taskResponse);
                                            this.fetchCreatedBy(taskResponse.getCreatedBy());
                                            if (taskResponse.getUserIds() != null) {
                                                taskResponse.getUserIds()
                                                        .forEach(this::fetchAssignedUser);
                                            }
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

    private void fetchAssignedUser(int userId) {
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
                                            this.bindAssignedUser(userResponse);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchAssignedUser: " + error.getMessage(), error);
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
                .searchUsers(search, pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleGenericResponse(
                                        response,
                                        userResponses -> {
                                            listUsers.setError(null);

                                            List<UserResponse> userResponsesExceptCreatedBy = userResponses.stream()
                                                    .filter(userResponse -> this.createdBy == null || userResponse.getId() != this.createdBy.getId())
                                                    .collect(Collectors.toList());

                                            List<SearchedUser> searchedUsers = userResponsesExceptCreatedBy.stream()
                                                    .map(SearchedUser::new)
                                                    .collect(Collectors.toList());

                                            listUsers.setObjects(searchedUsers);

                                            int[] matrix = new int[2];
                                            listUsers.getLocationOnScreen(matrix);
                                            int listUsersPositionY = matrix[1];
                                            int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                                            if (screenHeight - listUsersPositionY < screenHeight / 2.5) {
                                                scrollViewWrapper.smoothScrollBy(0, screenHeight / 5);
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

    public void submitNewTask(RequestBody newTaskRequestBody) {
        this.taskAccessActivity.disableSubmitNew();

        Disposable disposable = RetrofitServiceManager.getTaskService(getContext())
                .newTask(newTaskRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            TaskResponse taskResponse = gson.fromJson(gson.toJson(responseBody), TaskResponse.class);
                                            super.getBaseActivity().pushToast("task added successfully");
                                            if (this.taskAccessActivity != null) {
                                                this.taskAccessActivity.setFormStatus(FORM_STATUS_DONE);
                                                this.taskAccessActivity.setId(taskResponse.getId());
                                            }
                                        },
                                        errorApiResponse -> {
                                            textViewError.setText(errorApiResponse.getMessage());
                                            this.taskAccessActivity.enableSubmitNew();
                                        }
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewTask: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                            this.taskAccessActivity.enableSubmitNew();
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    public void submitEditTask(String id, RequestBody editTaskRequestBody) {
        Disposable disposable = RetrofitServiceManager.getTaskService(getContext())
                .editTask(id, editTaskRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleGenericResponse(
                                        response,
                                        taskResponse -> {
                                            textViewError.setText(null);
                                            getBaseActivity().pushToast("task edited successfully");
                                            this.bindEditedTask(taskResponse);
                                            if (this.taskAccessActivity != null) {
                                                this.taskAccessActivity.setFormStatus(FORM_STATUS_DONE);
                                            }
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditTask: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    public void submitEditStatus(String id, RequestBody editStatusRequestBody) {
        Disposable disposable = RetrofitServiceManager.getTaskService(getContext())
                .editStatus(id, editStatusRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleGenericResponse(
                                        response,
                                        taskResponse -> {
                                            textViewError.setText(null);
                                            getBaseActivity().pushToast("task status edited successfully");
                                            this.bindEditedTask(taskResponse);
                                            if (this.taskAccessActivity != null) {
                                                this.taskAccessActivity.setFormStatus(FORM_STATUS_DONE);
                                            }
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditStatus: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    public NewTaskRequest validateNewTask() {
        String name = inputTextName.getText().toString().trim();
        String description = inputTextDescription.getText().toString().trim();
        String createdAt = inputTextCreatedAt.getText().toString().trim();
        String dueDate = inputTextDueDate.getText().toString().trim();

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

        if (!DateUtils.validate(dueDate, getBaseActivity().getString(R.string.date_pattern))){
            inputTextDueDate.setError("invalid date format");
            inputTextDueDate.requestFocus();
        } else if (DateUtils.validate(createdAt, getBaseActivity().getString(R.string.date_pattern)) && DateUtils.parseDate(dueDate, getBaseActivity().getString(R.string.date_pattern)).compareTo(DateUtils.parseDate(createdAt, getBaseActivity().getString(R.string.date_pattern))) < 0) {
            inputTextDueDate.setError("due date must be after created date");
            inputTextDueDate.requestFocus();
        } else {
            inputTextDueDate.setError(null);
        }

        List<Integer> userIds = selectedListUsers.getObjects()
                .stream()
                .map(SelectedUser::getId)
                .collect(Collectors.toList());

        return new NewTaskRequest(
                name,
                description,
                dueDate,
                userIds,
                this.projectId);
    }

    public EditTaskRequest validateEditTask() {
        String name = inputTextName.getText().toString().trim();
        String description = inputTextDescription.getText().toString().trim();
        String createdAt = inputTextCreatedAt.getText().toString().trim();
        String dueDate = inputTextDueDate.getText().toString().trim();

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

        if (!DateUtils.validate(dueDate, getBaseActivity().getString(R.string.date_pattern))){
            inputTextDueDate.setError("invalid date format");
            inputTextDueDate.requestFocus();
        } else if (DateUtils.validate(createdAt, getBaseActivity().getString(R.string.date_pattern)) && DateUtils.parseDate(dueDate, getBaseActivity().getString(R.string.date_pattern)).compareTo(DateUtils.parseDate(createdAt, getBaseActivity().getString(R.string.date_pattern))) < 0) {
            inputTextDueDate.setError("due date must be after created date");
            inputTextDueDate.requestFocus();
        } else {
            inputTextDueDate.setError(null);
        }

        int status = selectStatus.getSelectedOption().getCode();

        List<Integer> userIds = selectedListUsers.getObjects()
                .stream()
                .map(SelectedUser::getId)
                .collect(Collectors.toList());

        return new EditTaskRequest(
                name,
                description,
                dueDate,
                status,
                userIds
        );
    }

    public EditStatusRequest validateEditStatus() {
        int status = selectStatus.getSelectedOption().getCode();
        return new EditStatusRequest(status);
    }

    public RequestBody buildNewTaskRequestBody(NewTaskRequest newTaskRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: NewTaskRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(newTaskRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildNewTaskRequestBody: " + e.getMessage(), e);
            }
        }

        newTaskRequest.getUserIds()
                .forEach(userId -> builder.addFormDataPart("userIds", String.valueOf(userId)));

        return builder.build();
    }

    public RequestBody buildEditTaskRequestBody(EditTaskRequest editTaskRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: EditTaskRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(editTaskRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildEditTaskRequestBody: " + e.getMessage(), e);
            }
        }

        builder.addFormDataPart("status", String.valueOf(editTaskRequest.getStatus()));

        editTaskRequest.getUserIds()
                .forEach(userId -> builder.addFormDataPart("userIds", String.valueOf(userId)));

        return builder.build();
    }

    public RequestBody buildEditStatusRequestBody(EditStatusRequest editStatusRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("status", String.valueOf(editStatusRequest.getStatus()));
        return builder.build();
    }

    private void bindEditedTask(TaskResponse taskResponse) {
        inputTextName.setText(taskResponse.getName());
        inputTextDescription.setText(taskResponse.getDescription());
        inputTextCreatedAt.setText(DateUtils.dateToString(taskResponse.getCreatedAt(), getBaseActivity().getString(R.string.date_pattern)));
        inputTextDueDate.setText(DateUtils.dateToString(taskResponse.getDueAt(), getBaseActivity().getString(R.string.date_pattern)));

        List<TaskStatusDto> taskStatusDtos;
        if (
                taskResponse.getStatusCode() == TaskStatus.OVERDUE.getCode() ||
                (taskResponse.getStatusCode() == TaskStatus.COMPLETED.getCode() && taskResponse.getDueAt().compareTo(new Date()) < 0)
        ) {
            taskStatusDtos = Arrays.stream(TaskStatus.values())
                    .filter(taskStatus -> taskStatus.getCode() == TaskStatus.OVERDUE.getCode() || taskStatus.getCode() == TaskStatus.COMPLETED.getCode())
                    .map(TaskStatusDto::new)
                    .collect(Collectors.toList());
        } else {
            taskStatusDtos = Arrays.stream(TaskStatus.values())
                    .filter(taskStatus -> taskStatus.getCode() != TaskStatus.OVERDUE.getCode())
                    .map(TaskStatusDto::new)
                    .collect(Collectors.toList());
        }
        selectStatus.setOptions(taskStatusDtos);

        TaskStatusDto taskStatusDto = new TaskStatusDto(TaskStatus.getTaskStatusFormCode(taskResponse.getStatusCode()));
        selectStatus.setSelectedOption(taskStatusDto);
    }

    private void bindCreatedBy(UserResponse userResponse) {
        if (userResponse == null) {
            inputTextCreateBy.setText(getBaseActivity().getString(R.string.unidentified));
        } else {
            inputTextCreateBy.setText(userResponse.getName());
            this.createdBy = userResponse;
        }
    }
    private void bindAssignedUser(UserResponse userResponse) {
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
                }, new IntentFilter(SELECT_ASSIGNED_USER_ACTION));
    }

    private void performSelectUser(SelectedUser selectedUser) {
        selectedListUsers.addNewItem(selectedUser);
    }

    private void setTaskAccessActivity() {
        BaseActivity baseActivity = super.getBaseActivity();
        if (baseActivity instanceof TaskAccessActivity) {
            this.taskAccessActivity = (TaskAccessActivity) baseActivity;
        }
    }

    public void setTextViewError(String error) {
        this.textViewError.setText(error);
    }
}
