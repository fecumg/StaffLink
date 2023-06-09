package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PARENT_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.gustavoavila.websocketclient.WebSocketClient;
import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomCommentsComponent;
import fpt.edu.stafflink.components.CustomInputTextComponent;
import fpt.edu.stafflink.models.others.DisplayedComment;
import fpt.edu.stafflink.models.requestDtos.CommentRequest;
import fpt.edu.stafflink.models.responseDtos.CommentResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import fpt.edu.stafflink.webClient.WebClientServiceManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CommentsFragment extends BaseFragment {
    private static final String ERROR_TAG = "CommentsFragment";
    private static final int DEFAULT_PAGE_SIZE = 12;

    TextView textViewError;
    CustomInputTextComponent inputTextComment;
    ImageButton buttonSubmitComment;
    CustomCommentsComponent comments;
    ProgressBar progressBarInfiniteLoading;

    private String id;
    private int accessType;

    WebSocketClient webSocketClient;
    URI uri;

    Map<Integer, UserResponse> relatedUserMap = new HashMap<>();

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    private int currentPage = 1;
    private boolean ableToLoad = true;

    public CommentsFragment() {
        // Required empty public constructor
    }

    public static CommentsFragment newInstance(String projectId, String id, int position, int accessType) {
        CommentsFragment fragment = new CommentsFragment();
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
            this.id = getArguments().getString(PARAM_STRING_ID);
            this.accessType = getArguments().getInt(PARAM_PROJECT_ACCESS_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        textViewError = view.findViewById(R.id.textViewError);
        inputTextComment = view.findViewById(R.id.inputTextComment);
        buttonSubmitComment = view.findViewById(R.id.buttonSubmitComment);
        comments = view.findViewById(R.id.comments);
        progressBarInfiniteLoading = view.findViewById(R.id.progressBarInfiniteLoading);

        this.initComments();

        this.fetchComments(this.id);
        comments.setOnScrolledToTopHandler(() -> this.fetchComments(this.id));

        this.subscribeToWebSocket();
        this.initNewCommentForm();

        return view;
    }

    private void initNewCommentForm() {
        if (this.accessType == PROJECT_ACCESS_TYPE_OBSERVABLE) {
            inputTextComment.setVisibility(View.GONE);
            buttonSubmitComment.setVisibility(View.GONE);
        } else {
            inputTextComment.setVisibility(View.VISIBLE);
            buttonSubmitComment.setVisibility(View.VISIBLE);
            buttonSubmitComment.setOnClickListener(view -> this.submitComment());
        }
    }

    private void initComments() {
        comments.setAuthUser(getBaseActivity().getAuthUser());
    }

    private void fetchCreatorOfComment(int userId, int startPosition, int effectedCount) {
        if (relatedUserMap.containsKey(userId) && relatedUserMap.get(userId) != null) {
            comments.insertRelatedUser(relatedUserMap.get(userId), startPosition, effectedCount);
            return;
        } else if (comments.getAuthUser() != null && comments.getAuthUser().getId() == userId) {
            comments.insertRelatedUser(comments.getAuthUser(), startPosition, effectedCount);
            return;
        }
        Disposable disposable = RetrofitServiceManager.getUserService(super.retrieveContext())
                .getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribe(
                        response ->
                                super.getBaseActivity().handleResponse(
                                        response,
                                        (responseBody, gson) -> {
                                            textViewError.setError(null);
                                            UserResponse userResponse = gson.fromJson(gson.toJson(responseBody), UserResponse.class);
                                            relatedUserMap.put(userId, userResponse);
                                            comments.insertRelatedUser(userResponse, startPosition, effectedCount);
                                        },
                                        errorApiResponse -> {
                                            textViewError.setError(errorApiResponse.getMessage());
                                            comments.insertRelatedUser(null, startPosition, effectedCount);
                                        }
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchCreatorOfComment: " + error.getMessage(), error);
                            super.getBaseActivity().pushToast(error.getMessage());
                            textViewError.setError(error.getMessage());
                            comments.insertRelatedUser(null, startPosition, effectedCount);
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void fetchComments(String taskId) {
        if (!this.ableToLoad) {
            return;
        }
        this.ableToLoad = false;

        progressBarInfiniteLoading.setVisibility(View.VISIBLE);
        MultiValuePagination pagination = new MultiValuePagination(this.currentPage, DEFAULT_PAGE_SIZE, "id", MultiValuePagination.DESC);
        int previousSize = comments.getObjects().size();

        List<Integer> userIds = new ArrayList<>();

        reactor.core.Disposable disposable = WebClientServiceManager.getCommentServiceInstance()
                .getCommentsByTaskId(getContext(), taskId, pagination)
                .subscribe(
                        commentResponse -> getBaseActivity().runOnUiThread(() -> {
                            textViewError.setText(null);
                            DisplayedComment displayedComment = new DisplayedComment(commentResponse);
                            comments.addNewItem(displayedComment);
                            userIds.add(commentResponse.getCreatedBy());
                        }),
                        error -> getBaseActivity().runOnUiThread(() -> {
                            progressBarInfiniteLoading.setVisibility(View.GONE);

                            Log.e(ERROR_TAG, "fetchComments: " + error.getMessage(), error);
                            getBaseActivity().pushToast(error.getMessage());
                            textViewError.setError(error.getMessage());
                        }),
                        () -> getBaseActivity().runOnUiThread(() -> {
                            progressBarInfiniteLoading.setVisibility(View.GONE);

                            int numberOfFetchedComments = comments.getObjects().size() - previousSize;
                            userIds.forEach(userId -> fetchCreatorOfComment(userId, 0, comments.getObjects().size()));
                            if (numberOfFetchedComments == DEFAULT_PAGE_SIZE) {
                                this.currentPage++;
                                this.ableToLoad = true;
                            } else {
                                this.ableToLoad = false;
                            }
                        })
                );
        reactorCompositeDisposable.add(disposable);
    }

    public void submitComment() {
        CommentRequest commentRequest = this.validateComment();
        if (commentRequest != null) {
            String commentRequestAsString = gson.toJson(commentRequest);
            webSocketClient.send(commentRequestAsString);
            inputTextComment.setText(null);
        }
    }

    private void subscribeToWebSocket() {
        uri = URI.create("ws://10.0.2.2:9091/comments/" + this.id);
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
            }

            @Override
            public void onTextReceived(String message) {
                CommentResponse commentResponse = gson.fromJson(message, CommentResponse.class);
                DisplayedComment displayedComment = new DisplayedComment(commentResponse);

                getBaseActivity().runOnUiThread(() -> {
                    comments.insertItem(0, displayedComment);
                    comments.scrollTo(0);
                    fetchCreatorOfComment(commentResponse.getCreatedBy(), 0, 1);
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
            }

            @Override
            public void onCloseReceived(int reason, String description) {
            }
        };

        webSocketClient.addHeader("Authorization", super.getBaseActivity().getBearer());
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public CommentRequest validateComment() {
        String message = inputTextComment.getText().toString().trim();

        if (StringUtils.isEmpty(message)) {
            inputTextComment.setError("message cannot be empty");
            inputTextComment.requestFocus();
            return null;
        } else {
            inputTextComment.setError(null);
        }
        return new CommentRequest(message, this.id);
    }

    @Override
    public void onDestroy() {
        if (webSocketClient != null) {
            webSocketClient.close(5000, WebSocketClient.CLOSE_CODE_NORMAL, "user exited discussion");
        }
        super.onDestroy();
    }
}
