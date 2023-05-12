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
import android.widget.TextView;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.TaskAccessActivity;
import fpt.edu.stafflink.components.CustomFilePickerComponent;
import fpt.edu.stafflink.models.others.SelectedAttachment;
import fpt.edu.stafflink.models.requestDtos.AttachmentRequest;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.retrofit.InputStreamRequestBody;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.utilities.FileUtils;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AttachmentsFragment extends BaseFragment{
    private static final String ERROR_TAG = "AttachmentsFragment";

    TextView textViewError;
    CustomFilePickerComponent filePickerAttachments;

    private String projectId;
    private String id;
    private int position;
    private int accessType;

    TaskAccessActivity taskAccessActivity;

    public AttachmentsFragment() {
        // Required empty public constructor
    }

    public static AttachmentsFragment newInstance(String projectId, String id, int position, int accessType) {
        AttachmentsFragment fragment = new AttachmentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_attachments, container, false);

        textViewError = view.findViewById(R.id.textViewError);
        filePickerAttachments = view.findViewById(R.id.filePickerAttachments);
        filePickerAttachments.registerPickFileActivityResultLauncherOnFragment(this);

        this.initFilePickerAttachments();
        this.fetchAttachments(this.id);

        return view;
    }

    private void initFilePickerAttachments() {
        filePickerAttachments.setDownloadable(true);
        filePickerAttachments.setMainField("name");

        if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
            filePickerAttachments.setCancellable(false);
            filePickerAttachments.setAbleToPickFile(false);
        } else {
            filePickerAttachments.registerPickFileActivityResultLauncherOnFragment(this);

            filePickerAttachments.setCancellable(true);
            filePickerAttachments.setAbleToPickFile(true);
            this.setUploadHandler();
            this.setRemoveHandler();
        }
    }

    private void fetchAttachments(String taskId) {
        MultiValuePagination pagination = new MultiValuePagination();
        pagination.setDirection(MultiValuePagination.ASC);

        reactor.core.Disposable disposable = WebClientServiceManager.getAttachmentServiceInstance()
                .getAttachmentsByTaskId(getContext(), taskId, pagination)
                .subscribe(
                        attachmentResponse -> getBaseActivity().runOnUiThread(() -> {
                            textViewError.setText(null);
                            filePickerAttachments.addNewItem(new SelectedAttachment(attachmentResponse.getId(), attachmentResponse.getName(), this.id));
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            Log.e(ERROR_TAG, "fetchAttachments: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setError(error.getMessage());
                        })
                );

        reactorCompositeDisposable.add(disposable);
    }

    private void setUploadHandler() {
        filePickerAttachments.setUploadHandler(uri -> {
            AttachmentRequest attachmentRequest = new AttachmentRequest(uri, this.id);
            RequestBody attachmentRequestBody = this.buildAttachmentRequestBody(attachmentRequest);
            Disposable disposable = RetrofitServiceManager.getAttachmentService(getContext())
                    .newAttachment(attachmentRequestBody)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response ->
                                    getBaseActivity().handleGenericResponse(
                                            response,
                                            attachmentResponse -> {
                                                textViewError.setText(null);
                                                if (attachmentResponse != null) {
                                                    SelectedAttachment selectedAttachment = new SelectedAttachment(attachmentResponse.getId(), attachmentResponse.getName(), this.id);
                                                    filePickerAttachments.addNewItem(selectedAttachment);
                                                    filePickerAttachments.scrollTo(filePickerAttachments.getObjects().indexOf(selectedAttachment));
                                                    getBaseActivity().pushToast("attachment added successfully");
                                                }
                                            },
                                            errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                    ),
                            error -> {
                                Log.e(ERROR_TAG, "setUploadHandler: " + error.getMessage(), error);
                                getBaseActivity().pushToast(error.getMessage());
                                textViewError.setText(error.getMessage());
                            });

            compositeDisposable.add(disposable);
        });
    }

    private RequestBody buildAttachmentRequestBody(AttachmentRequest attachmentRequest) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        String contentType = FileUtils.getContentType(getContext(), attachmentRequest.getUri());
        RequestBody fileRequestBody = new InputStreamRequestBody(MediaType.get(contentType), getBaseActivity().getContentResolver(), attachmentRequest.getUri());
        builder.addFormDataPart("attachment", FileUtils.getFilename(getContext(), attachmentRequest.getUri()), fileRequestBody);
        builder.addFormDataPart("taskId", attachmentRequest.getTaskId());

        return builder.build();
    }

    private void setRemoveHandler() {
        filePickerAttachments.setRemoveHandler((position, selectedAttachment) -> {
            Disposable disposable = RetrofitServiceManager.getAttachmentService(getContext())
                    .deleteAttachment(selectedAttachment.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response ->
                                    getBaseActivity().handleGenericResponse(
                                            response,
                                            responseBody -> {
                                                textViewError.setText(null);
                                                filePickerAttachments.adapter.removeItem(position);
                                                getBaseActivity().pushToast("attachment removed successfully");
                                            },
                                            errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                    ),
                            error -> {
                                Log.e(ERROR_TAG, "setRemoveHandler: " + error.getMessage(), error);
                                getBaseActivity().pushToast(error.getMessage());
                                textViewError.setText(error.getMessage());
                            });

            compositeDisposable.add(disposable);
        });
    }

    @Override
    public void onDestroy() {
        filePickerAttachments.flushPickFileActivityResultLauncher();
        super.onDestroy();
    }
}
