package fpt.edu.stafflink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.others.SelectedAttachment;
import fpt.edu.stafflink.utilities.GenericUtils;

public abstract class CustomFilePickerAdapter extends BaseAdapter<SelectedAttachment, CustomFilePickerAdapter.ViewHolder>  {
    private String mainField;

    private boolean removable = false;
    private boolean downloadable = false;

    public CustomFilePickerAdapter(List<SelectedAttachment> objects, String mainField) {
        super(objects);
        this.mainField = mainField;
    }

    public void setMainField(String mainField) {
        this.mainField = mainField;
        notifyItemRangeChanged(0, getItemCount());
    }
    public String getMainField() {
        return mainField;
    }

    public void setData(List<SelectedAttachment> objects, String mainField) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.mainField = mainField;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
        notifyItemRangeChanged(0, getItemCount());
    }

    public boolean isRemovable() {
        return this.removable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
        notifyItemRangeChanged(0, getItemCount());
    }

    public boolean isDownloadable() {
        return this.downloadable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }

        SelectedAttachment object = getObjects().get(position);
        holder.itemFilePickerMainElement.setText(GenericUtils.getFieldValue(object, this.mainField));

        if (this.removable || this.downloadable) {
            holder.itemFilePickerLayout.setOnLongClickListener(view -> {
                this.onItemLongClick(view, position, object);
                return true;
            });
        }
    }

    public abstract void onItemLongClick(View view, int position, SelectedAttachment selectedAttachment);

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemFilePickerLayout;
        TextView itemFilePickerMainElement;
        public ProgressBar itemFilePickerProgressbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemFilePickerLayout = itemView.findViewById(R.id.itemFilePickerLayout);
            itemFilePickerMainElement = itemView.findViewById(R.id.itemFilePickerMainElement);
            itemFilePickerProgressbar = itemView.findViewById(R.id.itemFilePickerProgressbar);
        }
    }
}
