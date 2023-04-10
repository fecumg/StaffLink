package fpt.edu.stafflink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomSelectedListAdapter<T> extends BaseAdapter<T, CustomSelectedListAdapter.ViewHolder> {
    private String mainField;

    private boolean removable = false;

    public CustomSelectedListAdapter(List<T> objects, String mainField) {
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

    public void setData(List<T> objects, String mainField) {
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }

        T object = getObjects().get(position);
        holder.itemSelectedListMainElement.setText(GenericUtils.getFieldValue(object, this.mainField));

        if (this.removable) {
            holder.itemSelectedListRemoveButton.setVisibility(View.VISIBLE);
            holder.itemSelectedListRemoveButton.setOnClickListener(view -> super.removeItem(position));
        } else {
            holder.itemSelectedListRemoveButton.setVisibility(View.GONE);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemSelectedListLayout;
        TextView itemSelectedListMainElement;
        ImageButton itemSelectedListRemoveButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemSelectedListLayout = itemView.findViewById(R.id.itemSelectedListLayout);
            itemSelectedListMainElement = itemView.findViewById(R.id.itemSelectedListMainElement);
            itemSelectedListRemoveButton = itemView.findViewById(R.id.itemSelectedListRemoveButton);
        }
    }
}
