package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomTableAdapter;
import fpt.edu.stafflink.constants.ImageShape;

public class CustomTableComponent<T> extends LinearLayout {
    private static final String ERROR_TAG = "CustomTableComponent";
    private static final int TEXT_INDEX = 0;
    private static final int BORDER_INDEX = 1;

    RecyclerView customTableComponentMainElement;

    ViewGroup customTableComponentHeaderWrapper_one;
    ViewGroup customTableComponentHeaderWrapper_two;
    ViewGroup customTableComponentHeaderWrapper_three;
    ViewGroup customTableComponentHeaderWrapper_four;
    ViewGroup customTableComponentHeaderWrapper_five;
    ViewGroup[] customTableComponentHeaderWrappers;

    TextView customTableComponentError;
    public CustomTableAdapter<T> adapter;

    private CharSequence error;

    public CustomTableComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initView(context);

        this.setAttributes(attrs);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_table_custom, this);
        customTableComponentMainElement = view.findViewById(R.id.customTableComponentMainElement);

        customTableComponentHeaderWrapper_one = view.findViewById(R.id.customTableComponentHeaderWrapper_one);
        customTableComponentHeaderWrapper_two = view.findViewById(R.id.customTableComponentHeaderWrapper_two);
        customTableComponentHeaderWrapper_three = view.findViewById(R.id.customTableComponentHeaderWrapper_three);
        customTableComponentHeaderWrapper_four = view.findViewById(R.id.customTableComponentHeaderWrapper_four);
        customTableComponentHeaderWrapper_five = view.findViewById(R.id.customTableComponentHeaderWrapper_five);

        customTableComponentHeaderWrappers = new ViewGroup[] {
                customTableComponentHeaderWrapper_one,
                customTableComponentHeaderWrapper_two,
                customTableComponentHeaderWrapper_three,
                customTableComponentHeaderWrapper_four,
                customTableComponentHeaderWrapper_five,
        };

        customTableComponentError = view.findViewById(R.id.customTableComponentError);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customTableComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomTableAdapter<>(new ArrayList<>(), null);
        customTableComponentMainElement.setAdapter(adapter);
    }

    private void generateHeaders(String[] displayedFields) {
        int fieldNumber = displayedFields.length;
        if (fieldNumber == 0) {
            return;
        }

        if (fieldNumber > CustomTableAdapter.MAX_FIELD_NUMBER) {
            Log.e(ERROR_TAG, "setData: " + "table column number cannot exceed " + CustomTableAdapter.MAX_FIELD_NUMBER);
            return;
        }

        for (int i = 0; i < customTableComponentHeaderWrappers.length; i ++) {
            if (i < fieldNumber) {
                customTableComponentHeaderWrappers[i].setVisibility(VISIBLE);
                TextView textView = (TextView) customTableComponentHeaderWrappers[i].getChildAt(TEXT_INDEX);
                textView.setVisibility(VISIBLE);
                textView.setText(displayedFields[i]);

                if (1 < fieldNumber) {
                    customTableComponentHeaderWrappers[i].getChildAt(BORDER_INDEX).setVisibility(VISIBLE);
                }
            } else {
                customTableComponentHeaderWrappers[i].setVisibility(GONE);
            }
        }
    }

    public void setData(List<T> objects, String[] displayedFields) {
        this.generateHeaders(displayedFields);
        this.adapter.setData(objects, displayedFields);
    }

    public void setData(List<T> objects, String[] displayedFields, String[] imageFields) {
        this.generateHeaders(displayedFields);
        this.adapter.setData(objects, displayedFields, imageFields);
    }

    public void setData(List<T> objects, String[] displayedFields, String[] imageFields, int imageShape) {
        this.generateHeaders(displayedFields);
        this.adapter.setData(objects, displayedFields, imageFields, imageShape);
    }

    public void setObjects(List<T> objects) {
        this.adapter.setObjects(objects);
    }

    public List<T> getObjects() {
        return this.adapter.getObjects();
    }

    public void setDisplayedFields(String[] displayedFields) {
        this.generateHeaders(displayedFields);
        this.adapter.setDisplayedFields(displayedFields);
    }

    public String[] getDisplayedFields() {
        return this.adapter.getDisplayedFields();
    }

    public void setError(CharSequence error) {
        this.error = error;
        customTableComponentError.setText(error);
    }

    public CharSequence getError() {
        return this.error;
    }

    public void setImageFields(String[] imageFields) {
        this.adapter.setImageFields(imageFields);
    }

    public String[] getImageFields() {
        return this.adapter.getImageFields();
    }

    public void setImageShape(int imageShape) {
        this.adapter.setImageShape(imageShape);
    }

    public int getImageShape() {
        return this.adapter.getImageShape();
    }

    public void setDefaultImage(Drawable defaultImage) {
        this.adapter.setDefaultImage(defaultImage);
    }

    public Drawable getDefaultImage() {
        return this.adapter.getDefaultImage();
    }

    public void setDefaultImageTint(int defaultImageTint) {
        this.adapter.setDefaultImageTint(defaultImageTint);
    }

    public int getDefaultImageTint() {
        return this.adapter.getDefaultImageTint();
    }

    public void setAction(String action) {
        this.adapter.setAction(action);
    }

    public String getAction() {
        return this.adapter.getAction();
    }

    public void scrollTo(int position) {
        customTableComponentMainElement.scrollToPosition(position);
    }

    public void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            customTableComponentMainElement.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTableComponent, 0, 0);
        try {
            int imageShape = typedArray.getInt(R.styleable.CustomTableComponent_imageShape, ImageShape.RECTANGLE);
            this.setImageShape(imageShape);

            if (typedArray.hasValue(R.styleable.CustomTableComponent_defaultImage)) {
                this.setDefaultImage(typedArray.getDrawable(R.styleable.CustomTableComponent_defaultImage));
            }

            int defaultImageTint = typedArray.getColor(R.styleable.CustomTableComponent_defaultImageTint, ContextCompat.getColor(getContext(), R.color.secondary));
            this.setDefaultImageTint(defaultImageTint);
        } finally {
            typedArray.recycle();
        }
    }
}
