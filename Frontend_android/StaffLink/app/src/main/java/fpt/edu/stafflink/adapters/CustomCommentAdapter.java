package fpt.edu.stafflink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.models.others.DisplayedComment;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
import fpt.edu.stafflink.retrofit.RetrofitManager;

public class CustomCommentAdapter extends BaseAdapter<DisplayedComment, CustomCommentAdapter.ViewHolder> {
    private static final int TYPE_AUTH = 0;
    private static final int TYPE_OTHER = 1;

    private UserResponse authUser;

    public CustomCommentAdapter(List<DisplayedComment> objects) {
        super(objects);
    }

    public void setAuthUser(UserResponse authUser) {
        this.authUser = authUser;
        notifyItemRangeChanged(0, getItemCount());
    }

    public UserResponse getAuthUser() {
        return authUser;
    }

    @Override
    public int getItemViewType(int position) {
        if (authUser != null && getObjects().get(position).getComment().getCreatedBy() == authUser.getId()) {
            return TYPE_AUTH;
        } else return TYPE_OTHER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_AUTH) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_self, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }
        DisplayedComment object = super.getObjects().get(position);

        if ((getItemCount() > position + 1) && object.getComment().getCreatedBy() == super.getObjects().get(position + 1).getComment().getCreatedBy()) {
            holder.itemCommentAvatar.setVisibility(View.INVISIBLE);
            holder.itemCommentAvatar.clearUrl();

            int marginBottom = (int) holder.itemCommentLayout.getResources().getDimension(R.dimen.comment_item_margin_bottom);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, marginBottom);
            holder.itemCommentLayout.setLayoutParams(layoutParams);

            holder.itemCommentSender.setVisibility(View.GONE);
        } else {
            holder.itemCommentAvatar.setVisibility(View.VISIBLE);
            if (object.getUser() != null && StringUtils.isNotEmpty(object.getUser().getAvatar())) {
                holder.itemCommentAvatar.setUrl(RetrofitManager.getThumbnailUrl(holder.itemCommentAvatar.getContext(), object.getUser().getAvatar()));
            }

            int marginTop = (int) holder.itemCommentLayout.getResources().getDimension(R.dimen.comment_item_margin_top);
            int marginBottom = (int) holder.itemCommentLayout.getResources().getDimension(R.dimen.comment_item_margin_bottom);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, marginTop, 0, marginBottom);
            holder.itemCommentLayout.setLayoutParams(layoutParams);

            if (object.getUser() != null && this.getAuthUser() != null && object.getComment().getCreatedBy() != this.getAuthUser().getId()) {
                holder.itemCommentSender.setVisibility(View.VISIBLE);
                holder.itemCommentSender.setText(object.getUser().getName());
            } else {
                holder.itemCommentSender.setVisibility(View.GONE);
            }
        }
        holder.itemCommentMainElement.setText(object.getComment().getContent());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemCommentLayout;
        TextView itemCommentSender;
        TextView itemCommentMainElement;
        CustomImageComponentOval itemCommentAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCommentLayout = itemView.findViewById(R.id.itemCommentLayout);
            itemCommentSender = itemView.findViewById(R.id.itemCommentSender);
            itemCommentMainElement = itemView.findViewById(R.id.itemCommentMainElement);
            itemCommentAvatar = itemView.findViewById(R.id.itemCommentAvatar);
        }
    }
}
