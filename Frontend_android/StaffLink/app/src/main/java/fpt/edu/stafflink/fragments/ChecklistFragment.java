package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.TaskAccessActivity;
import fpt.edu.stafflink.components.CustomCheckBoxComponent;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.requestDtos.checkItemDtos.EditCheckItemRequest;
import fpt.edu.stafflink.models.requestDtos.checkItemDtos.NewCheckItemRequest;
import fpt.edu.stafflink.models.requestDtos.checkItemDtos.RearrangedCheckItemRequest;
import fpt.edu.stafflink.models.responseDtos.CheckItemResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChecklistFragment extends BaseFragment{
    private static final String ERROR_TAG = "ChecklistFragment";
    private static final int DEFAULT_DELAY_TIME = 1000;

    TextView textViewError;
    CustomInputTextComponent inputTextContent;
    ImageButton buttonSubmitCheckItem;
    ProgressBar progressBarChecklist;
    CustomCheckBoxComponent<CheckItemResponse> checkBoxChecklist;

    private String projectId;
    private String id;
    private int position;
    private int accessType;

    TaskAccessActivity taskAccessActivity;

    public ChecklistFragment() {
        // Required empty public constructor
    }

    public static ChecklistFragment newInstance(String projectId, String id, int position, int accessType) {
        ChecklistFragment fragment = new ChecklistFragment();
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
        View view = inflater.inflate(R.layout.fragment_checklist, container, false);

        textViewError = view.findViewById(R.id.textViewError);
        inputTextContent = view.findViewById(R.id.inputTextContent);
        buttonSubmitCheckItem = view.findViewById(R.id.buttonSubmitCheckItem);
        checkBoxChecklist = view.findViewById(R.id.checkBoxChecklist);
        progressBarChecklist = view.findViewById(R.id.progressBarChecklist);

        this.initNewCheckItemForm();
        this.initCheckBoxChecklist();
        this.fetchChecklist(this.id);
        this.setCheckBoxChecklistOnPositionChangedHandler();
        this.setCheckBoxChecklistOnRemovedHandler();

        return view;
    }

    private void initNewCheckItemForm() {
        if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
            inputTextContent.setVisibility(View.GONE);
            buttonSubmitCheckItem.setVisibility(View.GONE);
        } else {
            inputTextContent.setVisibility(View.VISIBLE);
            buttonSubmitCheckItem.setVisibility(View.VISIBLE);

            buttonSubmitCheckItem.setOnClickListener(view -> {
                NewCheckItemRequest newCheckItemRequest = this.validateNewCheckItem();
                if (newCheckItemRequest != null) {
                    RequestBody newCheckItemRequestBody = this.buildNewCheckItemRequestBody(newCheckItemRequest);
                    this.submitNewCheckItem(newCheckItemRequestBody);
                }
            });
        }
    }

    private void initCheckBoxChecklist() {
        checkBoxChecklist.setMainField("content");

        checkBoxChecklist.setOnCheckChangedHandler(((checkItemResponse, b) -> {
            this.updateProgressBar();

            checkBoxChecklist.postDelayed(() -> {
                EditCheckItemRequest editCheckItemRequest = new EditCheckItemRequest(b);
                RequestBody editCheckItemRequestBody = this.buildEditCheckItemRequestBody(editCheckItemRequest);
                this.submitEditCheckItem(checkItemResponse.getId(), editCheckItemRequestBody);
            }, DEFAULT_DELAY_TIME);
        }));
    }

    private void setCheckBoxChecklistOnPositionChangedHandler() {
        if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
            return;
        }
        checkBoxChecklist.setOnPositionChangedHandler(() -> {
            this.updateProgressBar();

            checkBoxChecklist.postDelayed(() -> {
                List<CheckItemResponse> cloneCheckBoxChecklist = new ArrayList<>(checkBoxChecklist.getObjects());
                Collections.reverse(cloneCheckBoxChecklist);

                List<RearrangedCheckItemRequest> rearrangedCheckItemRequests = new ArrayList<>();
                for (int i = 0; i < cloneCheckBoxChecklist.size(); i++) {
                    rearrangedCheckItemRequests.add(new RearrangedCheckItemRequest(cloneCheckBoxChecklist.get(i).getId(), i));
                }

                Disposable disposable = RetrofitServiceManager.getCheckItemService(super.retrieveContext())
                        .rearrangeCheckList(rearrangedCheckItemRequests)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response ->
                                        getBaseActivity().handleGenericResponse(
                                                response,
                                                checkItemResponse -> textViewError.setText(null),
                                                errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                        ),
                                error -> {
                                    Log.e(ERROR_TAG, "setCheckBoxChecklistOnPositionChangedHandler: " + error.getMessage(), error);
                                    getBaseActivity().pushToast(error.getMessage());
                                    textViewError.setText(error.getMessage());
                                });

                getBaseActivity().compositeDisposable.add(disposable);
            }, DEFAULT_DELAY_TIME);
        });
    }

    private void setCheckBoxChecklistOnRemovedHandler() {
        if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
            return;
        }
        checkBoxChecklist.setOnRemovedHandler(checkItemResponse -> {
            this.updateProgressBar();

            checkBoxChecklist.postDelayed(() -> {
                Disposable disposable = RetrofitServiceManager.getCheckItemService(super.retrieveContext())
                        .deleteCheckItem(checkItemResponse.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response ->
                                        getBaseActivity().handleGenericResponse(
                                                response,
                                                voidValue -> textViewError.setText(null),
                                                errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                        ),
                                error -> {
                                    Log.e(ERROR_TAG, "setCheckBoxChecklistOnRemovedHandler: " + error.getMessage(), error);
                                    getBaseActivity().pushToast(error.getMessage());
                                    textViewError.setText(error.getMessage());
                                });

                getBaseActivity().compositeDisposable.add(disposable);
            }, DEFAULT_DELAY_TIME);
        });
    }

    private void fetchChecklist(String taskId) {
        MultiValuePagination pagination = new MultiValuePagination("position", MultiValuePagination.DESC);

        reactor.core.Disposable disposable = WebClientServiceManager.getCheckItemServiceInstance()
                .getChecklistByTaskId(super.retrieveContext(), taskId, pagination)
                .subscribe(
                        checkItemResponse -> getBaseActivity().runOnUiThread(() -> {
                            textViewError.setText(null);
                            checkBoxChecklist.addNewItem(checkItemResponse, checkItemResponse.isChecked());
                            this.updateProgressBar();
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchChecklist: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setError(error.getMessage());
                        })
                );

        getBaseActivity().reactorCompositeDisposable.add(disposable);
    }

    public void submitNewCheckItem(RequestBody newCheckItemRequestBody) {
        inputTextContent.setText(null);
        Disposable disposable = RetrofitServiceManager.getCheckItemService(super.retrieveContext())
                .newCheckItem(newCheckItemRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleGenericResponse(
                                        response,
                                        (checkItemResponse) -> {
                                            textViewError.setText(null);
                                            if (checkBoxChecklist.getObjects().size() > 0) {
                                                checkBoxChecklist.insertItem(0, checkItemResponse);
                                            } else {
                                                checkBoxChecklist.addNewItem(checkItemResponse);
                                            }
                                            checkBoxChecklist.scrollTo(0);
                                            super.getBaseActivity().pushToast("Check item added successfully");
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitNewCheckItem: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    public void submitEditCheckItem(String id, RequestBody editCheckItemRequestBody) {
        Disposable disposable = RetrofitServiceManager.getCheckItemService(super.retrieveContext())
                .editCheckItem(id, editCheckItemRequestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                getBaseActivity().handleGenericResponse(
                                        response,
                                        (checkItemResponse) -> {
                                            textViewError.setText(null);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "submitEditCheckItem: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        });

        getBaseActivity().compositeDisposable.add(disposable);
    }

    public NewCheckItemRequest validateNewCheckItem() {
        String content = inputTextContent.getText().toString().trim();

        if (StringUtils.isEmpty(content)) {
            inputTextContent.setError("content cannot be empty");
            inputTextContent.requestFocus();
            return null;
        } else {
            inputTextContent.setError(null);
        }
        return new NewCheckItemRequest(content, this.id);
    }

    public RequestBody buildNewCheckItemRequestBody(NewCheckItemRequest newCheckItemRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Field field: NewCheckItemRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(newCheckItemRequest);
                if (fieldValue instanceof String) {
                    builder.addFormDataPart(field.getName(), fieldValue.toString());
                }
            } catch (IllegalAccessException e) {
                Log.e(ERROR_TAG, "buildNewCheckItemRequestBody: " + e.getMessage(), e);
            }
        }

        return builder.build();
    }

    public RequestBody buildEditCheckItemRequestBody(EditCheckItemRequest editCheckItemRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("checked", String.valueOf(editCheckItemRequest.isChecked()));

        return builder.build();
    }

    private void updateProgressBar() {
        int percentage = (checkBoxChecklist.getCheckedObjects().size() * 100) / checkBoxChecklist.getObjects().size();
        progressBarChecklist.setProgress(percentage);
    }
}
