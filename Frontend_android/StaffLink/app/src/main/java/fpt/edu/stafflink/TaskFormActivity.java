package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.FORM_STATUS_DONE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_FORM_STATUS;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.components.CustomFilePickerComponent;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.components.CustomSelectComponent;
import fpt.edu.stafflink.components.CustomSelectedListComponent;
import fpt.edu.stafflink.debouncing.Debouncer;
import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.models.others.SelectedUser;
import fpt.edu.stafflink.models.others.TaskStatusDto;
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

public class TaskFormActivity extends BaseActivity {
    private static final String ERROR_TAG = "TaskFormActivity";
    private static final String SELECT_ASSIGNED_USER_ACTION = "selectAssignedUserAction";
    private static final int MIN_SEARCH_LENGTH = 4;
    private static final int DEBOUNCING_DELAY = 1000;

    ImageButton buttonBack;
    ImageButton buttonSubmitTask;
    TextView textViewTaskFormTitle;

    TextView textViewError;

    NestedScrollView scrollViewWrapper;
    CustomInputTextComponent inputTextName;
    CustomInputTextComponent inputTextDescription;
    CustomInputTextComponent inputTextCreatedAt;
    CustomInputTextComponent inputTextDueDate;
    CustomSelectComponent<TaskStatusDto> selectStatus;
    CustomInputTextComponent inputTextCreateBy;
    CustomFilePickerComponent filePickerAttachments;
    CustomSelectedListComponent<SelectedUser> selectedListUsers;
    CustomInputTextComponent inputTextSearchUsers;
    CustomListComponent<UserResponse> listUsers;

    private String projectId;
    private String id;
    private int position;
    private int accessType;
    private int formStatus;

    private UserResponse createdBy;

    final Calendar calendar= Calendar.getInstance();

    @Override
    protected void onSubCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_task_form);

        buttonBack = findViewById(R.id.buttonBack);
        buttonSubmitTask = findViewById(R.id.buttonSubmitTask);

        textViewTaskFormTitle = findViewById(R.id.textViewTaskFormTitle);

        textViewError = findViewById(R.id.textViewError);

        scrollViewWrapper = findViewById(R.id.scrollViewWrapper);
        inputTextName = findViewById(R.id.inputTextName);
        inputTextDescription = findViewById(R.id.inputTextDescription);
        inputTextCreatedAt = findViewById(R.id.inputTextCreatedAt);
        inputTextDueDate = findViewById(R.id.inputTextDueDate);
        selectStatus = findViewById(R.id.selectStatus);
        inputTextCreateBy = findViewById(R.id.inputTextCreateBy);

        filePickerAttachments = findViewById(R.id.filePickerAttachments);

        selectedListUsers = findViewById(R.id.selectedListUsers);
        inputTextSearchUsers = findViewById(R.id.inputTextSearchUsers);
        listUsers = findViewById(R.id.listUsers);

        listUsers.setNestedScrollingEnabled(false);

        Intent intent = getIntent();
        this.projectId = intent.getStringExtra(PARAM_PARENT_STRING_ID);
        this.id = intent.getStringExtra(PARAM_STRING_ID);
        this.position = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
        this.accessType = intent.getIntExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);

        if (StringUtils.isNotEmpty(this.id)) {
            this.fetchDataOnEdit(this.id);
        } else {
            super.authUser.observe(this, authorizedFunctions -> {
                UserResponse authUser = super.getAuthUser();
                if (authUser != null) {
                    this.bindCreatedBy(authUser);
                }
                super.authorizedFunctions.removeObservers(this);
            });

        }

        this.initSelectStatus();
        this.initSearchList();
        this.initSelectedList();
        this.initInputTextDueDate();
        this.listenToAdapterOnClick();

        super.authorizedFunctions.observe(this, authorizedFunctions -> {
            this.toggleByCases();
            this.prepareTaskForm();
        });

        buttonBack.setOnClickListener(view -> back());
    }

    private void toggleByCases() {
        if (StringUtils.isEmpty(this.id)) {
            this.showOnNew();
        } else {
            this.showOnEdit();
        }
    }

    private void showOnNew() {
        textViewTaskFormTitle.setText(R.string.task_form_title_new);
        selectStatus.setVisibility(View.GONE);
        filePickerAttachments.setVisibility(View.GONE);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.new_task_path))) {
            buttonSubmitTask.setVisibility(View.VISIBLE);
            buttonSubmitTask.setOnClickListener(view -> performOnclickSubmitNew());
        } else {
            buttonSubmitTask.setVisibility(View.GONE);
        }
    }

    private void showOnEdit() {
        selectStatus.setVisibility(View.VISIBLE);
        filePickerAttachments.setVisibility(View.VISIBLE);

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.edit_task_path))) {
            buttonSubmitTask.setVisibility(View.VISIBLE);
            buttonSubmitTask.setOnClickListener(view -> performOnclickSubmitEdit());
        } else {
            buttonSubmitTask.setVisibility(View.GONE);
        }
    }

    private void initSelectStatus() {
        selectStatus.setMainField("message");

        List<TaskStatusDto> taskStatusDtos = Arrays.stream(TaskStatus.values())
                .map(TaskStatusDto::new)
                .collect(Collectors.toList());
        selectStatus.setOptions(taskStatusDtos);
    }

    private void initSearchList() {
        listUsers.setTitleField("name");
        listUsers.setContentField("username");
        listUsers.setAction(SELECT_ASSIGNED_USER_ACTION);
        listUsers.setError(null);
    }

    private void initSelectedList() {
        selectedListUsers.setMainField("name");
    }

    private void initInputTextDueDate() {
        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            inputTextDueDate.setText(DateUtils.dateToString(calendar.getTime(), getString(R.string.date_pattern)));
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        inputTextDueDate.setOnClick(view -> datePickerDialog.show());
        inputTextDueDate.setOnFocus((view, b) -> {
            if (b) {
                datePickerDialog.show();
            }
        });
        inputTextDueDate.setOnTextChanged(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!DateUtils.validate(charSequence.toString().trim(), getString(R.string.date_pattern))) {
                    datePickerDialog.show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void performOnclickSubmitNew() {
        NewTaskRequest newTaskRequest = this.validateNewTask();
        if (newTaskRequest != null) {
            RequestBody newTaskRequestBody = this.buildNewTaskRequestBody(newTaskRequest);
            this.submitNewTask(newTaskRequestBody);
        }
    }

    private void performOnclickSubmitEdit() {
        EditTaskRequest editTaskRequest = this.validateEditTask();
        if (editTaskRequest != null) {
            RequestBody editTaskRequestBody = this.buildEditTaskRequestBody(editTaskRequest);
            this.submitEditTask(this.id, editTaskRequestBody);
        }
    }

    private void prepareTaskForm() {
        if (
                (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED && super.isAuthorized(getString(R.string.edit_task_path))) ||
                        StringUtils.isEmpty(this.id)
        ) {
            inputTextName.setEditable(true);
            inputTextDescription.setEditable(true);
            inputTextDueDate.setEditable(true);
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
                    debouncer.call(() -> runOnUiThread(() -> searchUsers(searchText.toString().trim())), DEBOUNCING_DELAY);
                }
            });

            if (StringUtils.isEmpty(this.id)) {
                calendar.setTime(new Date());
                Date currentDate = calendar.getTime();
                calendar.add(Calendar.WEEK_OF_YEAR, 2);
                Date autoGeneratedDueAt = calendar.getTime();

                inputTextCreatedAt.setText(DateUtils.dateToString(currentDate, getString(R.string.date_pattern)));
                inputTextDueDate.setText(DateUtils.dateToString(autoGeneratedDueAt, getString(R.string.date_pattern)));
            }
        } else {
            inputTextName.setEditable(false);
            inputTextDescription.setEditable(false);
            inputTextDueDate.setEditable(false);
            selectedListUsers.setCancellable(false);
            inputTextSearchUsers.setVisibility(View.GONE);
        }
    }

    private void fetchDataOnEdit(String id) {
        Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .getTask(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
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
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void fetchCreatedBy(int userId) {
        if (userId == DEFAULT_ID) {
            this.bindCreatedBy(null);
            return;
        }
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
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
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void fetchAssignedUser(int userId) {
        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
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
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    private void searchUsers(String search) {
        if (StringUtils.isEmpty(search) || search.length() < MIN_SEARCH_LENGTH) {
            listUsers.setError(null);
            listUsers.setObjects(new ArrayList<>());
            return;
        }
        Pagination pagination = new Pagination();

        Disposable disposable = RetrofitServiceManager.getUserService(this)
                .getUsers(search, pagination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            listUsers.setError(null);
                                            Type type = new TypeToken<List<UserResponse>>() {}.getType();
                                            List<UserResponse> userResponses = gson.fromJson(gson.toJson(responseBody), type);
                                            List<UserResponse> userResponsesExceptCreatedBy = userResponses.stream()
                                                    .filter(userResponse -> this.createdBy == null || userResponse.getId() != this.createdBy.getId())
                                                    .collect(Collectors.toList());

                                            listUsers.setObjects(userResponsesExceptCreatedBy);

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
                            super.pushToast(error.getMessage());
                            listUsers.setError(error.getMessage());
                            listUsers.setObjects(new ArrayList<>());
                        }
                );

        compositeDisposable.add(disposable);
    }

    public void submitNewTask(RequestBody newTaskRequestBody) {
        Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .newTask(newTaskRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            TaskResponse taskResponse = gson.fromJson(gson.toJson(responseBody), TaskResponse.class);
                                            super.pushToast("task added successfully");
                                            this.formStatus = FORM_STATUS_DONE;
                                            this.id = taskResponse.getId();
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewTask: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public void submitEditTask(String id, RequestBody editTaskRequestBody) {
        Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .editTask(id, editTaskRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setText(null);
                                            super.pushToast("task edited successfully");
                                            this.formStatus = FORM_STATUS_DONE;
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditTask: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public NewTaskRequest validateNewTask() {
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

        String dueDate = inputTextDueDate.getText().toString().trim();

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

        String dueDate = inputTextDueDate.getText().toString().trim();
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

    private void bindEditedTask(TaskResponse taskResponse) {
//        set title

        inputTextName.setText(taskResponse.getName());
        inputTextDescription.setText(taskResponse.getDescription());
        inputTextDueDate.setText(DateUtils.dateToString(taskResponse.getDueAt(), getString(R.string.date_pattern)));

        TaskStatusDto taskStatusDto = new TaskStatusDto(TaskStatus.getTaskStatusFormCode(taskResponse.getStatusCode()));
        selectStatus.setSelectedOption(taskStatusDto);
    }
    private void bindCreatedBy(UserResponse userResponse) {
        if (userResponse == null) {
            inputTextCreateBy.setText(getString(R.string.unidentified));
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
        LocalBroadcastManager.getInstance(this)
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

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(PARAM_STRING_ID, this.id);
        intent.putExtra(PARAM_POSITION, this.position);
        intent.putExtra(PARAM_FORM_STATUS, this.formStatus);
        setResult(RESULT_OK, intent);
        finish();
    }
}