package fpt.edu.stafflink.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomCommentAdapter;
import fpt.edu.stafflink.models.others.DisplayedComment;
import fpt.edu.stafflink.models.responseDtos.UserResponse;

public class CustomCommentsComponent extends LinearLayout {
    RecyclerView CustomCommentsComponentMainElement;

    CustomCommentAdapter adapter;

    private List<UserResponse> relatedUsers = new ArrayList<>();

    private OnScrolledToTopHandler onScrolledToTopHandler;

    public CustomCommentsComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
        this.doOnScrolledToTop();
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_comments_custom, this);
        CustomCommentsComponentMainElement = view.findViewById(R.id.CustomCommentsComponentMainElement);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        CustomCommentsComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomCommentAdapter(new ArrayList<>());
        CustomCommentsComponentMainElement.setAdapter(adapter);
    }

    public void doOnScrolledToTop() {
        this.CustomCommentsComponentMainElement.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (CustomCommentsComponentMainElement.computeVerticalScrollOffset() == 0) {
                    if (onScrolledToTopHandler != null) {
                        onScrolledToTopHandler.handle();
                    }
                }
            }
        });
    }

    public List<DisplayedComment> getObjects() {
        return this.adapter.getObjects();
    }

    public void addNewItem(DisplayedComment object) {
        UserResponse authUser = this.adapter.getAuthUser();
        if (authUser != null && object.getComment().getCreatedBy() == authUser.getId()) {
            object.setAvatar(authUser.getAvatar());
        }
        this.adapter.addNewItem(object);
    }

    public void insertItem(int position, DisplayedComment object) {
        UserResponse authUser = this.adapter.getAuthUser();
        if (authUser != null && object.getComment().getCreatedBy() == authUser.getId()) {
            object.setAvatar(authUser.getAvatar());
        }
        this.adapter.insertItem(position, object);
    }

    public void setAuthUser(UserResponse authUser) {
        this.adapter.setAuthUser(authUser);
    }
    public UserResponse getAuthUser() {
        return this.adapter.getAuthUser();
    }

    public void scrollTo(int position) {
        if (-1 < position && position < this.getObjects().size()) {
            this.CustomCommentsComponentMainElement.smoothScrollToPosition(position);
        }
    }

    public void insertRelatedUser(UserResponse userResponse) {
        if (relatedUsers.contains(userResponse) || this.adapter.getAuthUser().equals(userResponse)) {
            return;
        }
        relatedUsers.add(userResponse);
        List<DisplayedComment> displayedComments = this.getObjects();
        for (int i = 0; i < displayedComments.size(); i++) {
            if (displayedComments.get(i).getComment().getCreatedBy() == userResponse.getId()) {
                displayedComments.get(i).setAvatar(userResponse.getAvatar());
                this.adapter.notifyItemChanged(i);
            }
        }
    }

    public void setOnScrolledToTopHandler(OnScrolledToTopHandler onScrolledToTopHandler) {
        this.onScrolledToTopHandler = onScrolledToTopHandler;
    }

    public interface OnScrolledToTopHandler {
        void handle();
    }
}
