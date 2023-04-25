package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.ProjectAccessActivity.TASK_ACTION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_ASSIGNED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_AUTHORIZED;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TASK_STATUS;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.components.CustomListComponent;
import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import reactor.core.Disposable;

public class TasksFragment extends BaseFragment {
    private static final String ERROR_TAG = "InitiatedFragment";

    CustomInputTextComponent inputTextSearchTasks;
    CustomListComponent<TaskResponse> listTasks;

    private String projectId;
    private int position;
    private int accessType;
    private int taskStatus;

    private OnListInitiatedListener onListInitiatedListener;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance(String id, int position, int accessType, int taskStatus) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_STRING_ID, id);
        args.putInt(PARAM_POSITION, position);
        args.putInt(PARAM_PROJECT_ACCESS_TYPE, accessType);
        args.putInt(PARAM_TASK_STATUS, taskStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.projectId = getArguments().getString(PARAM_STRING_ID);
            this.position = getArguments().getInt(PARAM_POSITION);
            this.accessType = getArguments().getInt(PARAM_PROJECT_ACCESS_TYPE);
            this.taskStatus = getArguments().getInt(PARAM_TASK_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        inputTextSearchTasks = view.findViewById(R.id.inputTextSearchTasks);
        listTasks = view.findViewById(R.id.listTasks);

        this.initList();

        if (this.accessType == PROJECT_ACCESS_TYPE_AUTHORIZED) {
            if (StringUtils.isNotEmpty(this.projectId)) {
                this.fetchAuthorizedTasksByProject(this.projectId);
            } else {
                this.fetchAuthorizedTasks();
            }
        } else if (this.accessType == PROJECT_ACCESS_TYPE_ASSIGNED) {
            if (StringUtils.isNotEmpty(this.projectId)) {
                this.fetchAssignedTasksByProject(this.projectId);
            } else {
                this.fetchAssignedTasks();
            }
        } else {
//            observable access type
            if (StringUtils.isNotEmpty(this.projectId)) {
                this.fetchTasksByProject(this.projectId);
            } else {
                this.fetchTasks();
            }
        }

        return view;
    }

    private void initList() {
        listTasks.setTitleField("name");
        listTasks.setContentField("description");
        listTasks.setAction(TASK_ACTION);
        listTasks.setError(null);
    }

    private void fetchAuthorizedTasks() {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getAuthorizedTasks(getContext(), this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.addItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchAuthorizedTasks: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    private void fetchAuthorizedTasksByProject(String projectId) {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getAuthorizedTasksByProject(getContext(), projectId, this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.addItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchAuthorizedTasksByProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    private void fetchAssignedTasks() {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getAssignedTasks(getContext(), this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.addItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchAssignedTasks: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    private void fetchAssignedTasksByProject(String projectId) {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getAuthorizedTasksByProject(getContext(), projectId, this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.adapter.addNewItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchAuthorizedTasksByProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    private void fetchTasks() {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getTasks(getContext(), this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.adapter.addNewItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchTasks: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    private void fetchTasksByProject(String projectId) {
        MultiValuePagination pagination = new MultiValuePagination();

        Disposable disposable = WebClientServiceManager.getTaskService()
                .getTasksByProject(getContext(), projectId, this.taskStatus, pagination)
                .subscribe(
                        taskResponse -> getBaseActivity().runOnUiThread(() -> {
                            listTasks.setError(null);
                            listTasks.adapter.addNewItem(taskResponse);
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchTasksByProject: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            listTasks.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            if (this.onListInitiatedListener != null) {
                                this.onListInitiatedListener.doOnListInitiatedListener();
                            }
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    public int getTaskStatus() {
        return this.taskStatus;
    }

    public CustomListComponent<TaskResponse> getListTasks() {
        return this.listTasks;
    }

    public void setOnListInitiatedListener(OnListInitiatedListener onListInitiatedListener) {
        this.onListInitiatedListener = onListInitiatedListener;
    }

    public interface OnListInitiatedListener {
        void doOnListInitiatedListener();
    }
}
