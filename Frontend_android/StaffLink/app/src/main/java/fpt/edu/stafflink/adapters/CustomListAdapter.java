package fpt.edu.stafflink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomListAdapter<T> extends BaseAdapter<T, CustomListAdapter.ViewHolder>{

    private String titleField;
    private String contentField;

    public CustomListAdapter(List<T> objects, String titleField, String contentField) {
        super(objects);
        this.titleField = titleField;
        this.contentField = contentField;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new CustomListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }

        T object = super.getObjects().get(position);
        if (StringUtils.isNotEmpty(this.titleField)) {
            holder.itemListTitle.setText(GenericUtils.getFieldValue(object, this.titleField));
        }
        if (StringUtils.isNotEmpty(this.contentField)) {
            holder.itemListContent.setText(GenericUtils.getFieldValue(object, this.contentField));
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
        super.objects = objects;
        this.titleField = titleField;
        this.contentField = contentField;
        notifyItemRangeChanged(0, getItemCount());
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
