package fpt.edu.stafflink.adapters;

import static android.view.View.VISIBLE;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.components.CustomImageComponent;
import fpt.edu.stafflink.components.CustomImageComponentOval;
import fpt.edu.stafflink.constants.ImageShape;
import fpt.edu.stafflink.retrofit.RetrofitManager;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomTableAdapter<T> extends BaseAdapter<T, CustomTableAdapter.ViewHolder> {

    private static final String ERROR_TAG = "CustomTableAdapter";

    private static final int TEXT_INDEX = 0;
    private static final int RECTANGULAR_IMAGE_INDEX = 1;
    private static final int OVAL_IMAGE_INDEX = 2;
    private static final int BORDER_INDEX = 3;

    private static final int TYPE_EVEN = 0;
    private static final int TYPE_ODD = 1;
    public static final int MAX_FIELD_NUMBER = 5;

    public static final float IMAGE_WRAPPER_WEIGHT = 0.7f;

    private String[] displayedFields;
    private String[] imageFields;
    private int imageShape;
    private Drawable defaultImage;
    private int defaultImageTint;

    public CustomTableAdapter(List<T> objects, String[] displayedFields) {
        super(objects);
        this.displayedFields = displayedFields;
    }

    public CustomTableAdapter(List<T> objects, String[] displayedFields, String[] imageFields) {
        super(objects);
        this.displayedFields = displayedFields;
        this.imageFields = imageFields;
    }

    public CustomTableAdapter(List<T> objects, String[] displayedFields, String[] imageFields, int imageShape) {
        super(objects);
        this.displayedFields = displayedFields;
        this.imageFields = imageFields;
        this.imageShape = imageShape;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        int color = ContextCompat.getColor(view.getContext(), R.color.table_alternate_color);
        Drawable colorDrawable = new ColorDrawable(color);
        if (viewType == TYPE_ODD) {
            viewHolder.itemTableLayout.setForeground(colorDrawable);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0 || displayedFields == null || displayedFields.length == 0) {
            return;
        }

        int length = displayedFields.length;
        if (length > MAX_FIELD_NUMBER) {
            Log.e(ERROR_TAG, "onBindViewHolder: " + "table column number cannot exceed " + MAX_FIELD_NUMBER);
            return;
        }

        T object = super.getObjects().get(position);
        for (int i = 0; i < length; i ++) {
            ViewGroup itemTableElementWrapper = holder.itemTableElementWrappers[i];
            itemTableElementWrapper.setVisibility(VISIBLE);

            String fieldName = displayedFields[i];
            if (imageFields != null && Arrays.asList(imageFields).contains(fieldName)) {
                CustomImageComponent imageElement;
                if (this.imageShape == ImageShape.OVAL) {
                    imageElement = (CustomImageComponentOval) itemTableElementWrapper.getChildAt(OVAL_IMAGE_INDEX);
                } else {
                    imageElement = (CustomImageComponent) itemTableElementWrapper.getChildAt(RECTANGULAR_IMAGE_INDEX);
                }

                holder.itemTableElementWrappers[i].setLayoutParams(imageLayoutParams());

                imageElement.setVisibility(VISIBLE);

                if (defaultImage != null) {
                    imageElement.setSrc(defaultImage);
                }

                if (defaultImageTint != 0) {
                    imageElement.setImageTint(defaultImageTint);
                }

                imageElement.setUrl(RetrofitManager.getThumbnailUrl(imageElement.getContext(), GenericUtils.getFieldValue(object, fieldName)));
            } else {
                TextView textElement = (TextView) itemTableElementWrapper.getChildAt(TEXT_INDEX);
                textElement.setVisibility(VISIBLE);
                textElement.setText(GenericUtils.getFieldValue(object, fieldName));
            }

            if (1 < length && i < length - 1) {
                itemTableElementWrapper.getChildAt(BORDER_INDEX).setVisibility(VISIBLE);
            }
        }
        holder.itemTableLayout.setOnClickListener(view -> super.onClickItem(view, super.getObjectId(object), position));
    }

    public LinearLayout.LayoutParams imageLayoutParams() {
        return new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                IMAGE_WRAPPER_WEIGHT
        );
    }

    public void setDisplayedFields(String[] displayedFields) {
        this.displayedFields = displayedFields;
        notifyItemRangeChanged(0, getItemCount());
    }

    public String[] getDisplayedFields() {
        return this.displayedFields;
    }

    public void setImageFields(String[] imageFields) {
        this.imageFields = imageFields;
        notifyItemRangeChanged(0, getItemCount());
    }

    public String[] getImageFields() {
        return this.imageFields;
    }

    public void setDefaultImage(Drawable defaultImage) {
        this.defaultImage = defaultImage;
        notifyItemRangeChanged(0, getItemCount());
    }

    public Drawable getDefaultImage() {
        return this.defaultImage;
    }

    public void setDefaultImageTint(int defaultImageTint) {
        this.defaultImageTint = defaultImageTint;
        notifyItemRangeChanged(0, getItemCount());
    }

    public int getDefaultImageTint() {
        return this.defaultImageTint;
    }

    public void setImageShape(int imageShape) {
        this.imageShape = imageShape;
        notifyItemRangeChanged(0, getItemCount());
    }

    public int getImageShape() {
        return this.imageShape;
    }

    public void setData(List<T> objects, String[] displayedFields) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.displayedFields = displayedFields;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());
    }

    public void setData(List<T> objects, String[] displayedFields, String[] imageFields) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.displayedFields = displayedFields;
        this.imageFields = imageFields;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());    }

    public void setData(List<T> objects, String[] displayedFields, String[] imageFields, int imageShape) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.displayedFields = displayedFields;
        this.imageFields = imageFields;
        this.imageShape = imageShape;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemTableLayout;

        ViewGroup itemTableElementWrapper_one;
        ViewGroup itemTableElementWrapper_two;
        ViewGroup itemTableElementWrapper_three;
        ViewGroup itemTableElementWrapper_four;
        ViewGroup itemTableElementWrapper_five;
        ViewGroup[] itemTableElementWrappers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTableLayout = itemView.findViewById(R.id.itemTableLayout);

            itemTableElementWrapper_one = itemView.findViewById(R.id.itemTableElementWrapper_one);
            itemTableElementWrapper_two = itemView.findViewById(R.id.itemTableElementWrapper_two);
            itemTableElementWrapper_three = itemView.findViewById(R.id.itemTableElementWrapper_three);
            itemTableElementWrapper_four = itemView.findViewById(R.id.itemTableElementWrapper_four);
            itemTableElementWrapper_five = itemView.findViewById(R.id.itemTableElementWrapper_five);

            itemTableElementWrappers = new ViewGroup[] {
                    itemTableElementWrapper_one,
                    itemTableElementWrapper_two,
                    itemTableElementWrapper_three,
                    itemTableElementWrapper_four,
                    itemTableElementWrapper_five
            };
        }
    }
}
