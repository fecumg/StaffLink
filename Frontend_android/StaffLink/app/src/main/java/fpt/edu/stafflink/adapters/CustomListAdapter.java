package fpt.edu.stafflink.adapters;

import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_CONTENT;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.utilities.DimenUtils;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomListAdapter<T> extends BaseAdapter<T, CustomListAdapter.ViewHolder>{
    private static final int PADDING_STEP_IN_DP = 20;
    private static final int DEFAULT_PADDING_IN_DP = 5;

    private static final int TYPE_EVEN = 0;
    private static final int TYPE_ODD = 1;

    private String titleField;
    private String contentField;
    private String parentField;
    private int maxLines;
    private int height;

    public CustomListAdapter(List<T> objects, String titleField, String contentField) {
        super(objects);
        this.titleField = titleField;
        this.contentField = contentField;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return TYPE_EVEN;
        } else {
            return TYPE_ODD;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        ViewHolder viewHolder = new CustomListAdapter.ViewHolder(view);

        int color = ContextCompat.getColor(view.getContext(), R.color.table_alternate_color);
        Drawable colorDrawable = new ColorDrawable(color);
        if (viewType == TYPE_EVEN) {
            viewHolder.itemListLayout.setForeground(colorDrawable);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }

        T object = super.getObjects().get(position);

        String title = GenericUtils.getFieldValue(object, this.titleField);
        holder.itemListTitle.setText(title);

        String content = GenericUtils.getFieldValue(object, this.contentField);
        holder.itemListContent.setText(content);

        holder.itemListLayout.setOnClickListener(view -> this.onClickItem(view, GenericUtils.getObjectId(object), this.getObjectStringId(object), position, title, content));

        if (maxLines > 0) {
            holder.itemListContent.setMaxLines(maxLines);
        }
        if (height > 0) {
            holder.itemListLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, this.height));
        }

        if (StringUtils.isNotEmpty(parentField)) {
            int paddingStepInPx = DimenUtils.dpToPx(holder.itemView.getContext(), PADDING_STEP_IN_DP);
            int defaultPaddingInPx = DimenUtils.dpToPx(holder.itemView.getContext(), DEFAULT_PADDING_IN_DP);
            int level = GenericUtils.getObjectLevel(object, this.parentField, 0);

            holder.itemListLayout.setPadding(paddingStepInPx * level + defaultPaddingInPx, defaultPaddingInPx, defaultPaddingInPx, defaultPaddingInPx);

            StringBuilder prependedStringBuilder = new StringBuilder();
            for (int i = 0; i < level; i++) {
                prependedStringBuilder.append("*");
            }
        }
    }

    private void onClickItem(View view, int id, String stringId, int position, String title, String content) {
        if (StringUtils.isNotEmpty(action)) {
            Intent intent = new Intent(action);
            intent.putExtra(PARAM_ID, id);
            intent.putExtra(PARAM_STRING_ID, stringId);
            intent.putExtra(PARAM_POSITION, position);
            intent.putExtra(PARAM_TITLE, title);
            intent.putExtra(PARAM_CONTENT, content);
            LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
        }
    }

    private String getObjectStringId(T object) {
        try {
            if (object == null) {
                return "";
            }
            Method method = object.getClass().getMethod("getId");
            Object idObject = method.invoke(object);
            return String.valueOf(idObject);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setTitleField(String titleField) {
        this.titleField = titleField;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setContentField(String contentField) {
        this.contentField = contentField;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setData(List<T> objects, String titleField, String contentField) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.titleField = titleField;
        this.contentField = contentField;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    public String getParentField() {
        return this.parentField;
    }

    public void setMaxLines(int maxLines) {
        if (maxLines > 0) {
            this.maxLines = maxLines;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public int getMaxLines() {
        return this.maxLines;
    }

    public void setHeight(int height) {
        if (height > 0) {
            this.height = height;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public int getHeight() {
        return this.height;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemListLayout;
        TextView itemListTitle;
        TextView itemListContent;
        CustomImageComponentOval itemListIndexPrimary;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemListLayout = itemView.findViewById(R.id.itemListLayout);
            itemListTitle = itemView.findViewById(R.id.itemListTitle);
            itemListContent = itemView.findViewById(R.id.itemListContent);
            itemListIndexPrimary = itemView.findViewById(R.id.itemListIndexPrimary);
        }
    }
}
