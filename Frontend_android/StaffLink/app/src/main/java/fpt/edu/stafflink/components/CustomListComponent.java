package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomListAdapter;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomListComponent<T> extends LinearLayout {
    private static final String ERROR_TAG = "CustomListComponent";
    private static final int DEFAULT_ITEM_MAX_LINES = 3;

    RecyclerView customListComponentMainElement;

    TextView customListComponentError;
    public CustomListAdapter<T> adapter;

    private CharSequence error;

    public CustomListComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initView(context);

        this.setAttributes(attrs);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_list_custom, this);
        customListComponentMainElement = view.findViewById(R.id.customListComponentMainElement);
        customListComponentError = view.findViewById(R.id.customListComponentError);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customListComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomListAdapter<>(new ArrayList<>(), null, null);
        customListComponentMainElement.setAdapter(adapter);
    }

    public void setTitleField(String titleField) {
        this.adapter.setTitleField(titleField);
    }

    public void setContentField(String contentField) {
        this.adapter.setContentField(contentField);
    }

    public void setData(List<T> objects, String titleField, String contentField) {
        if (StringUtils.isNotEmpty(this.adapter.getParentField())) {
            objects = GenericUtils.rearrange(objects, null, this.adapter.getParentField());
        }
        this.adapter.setData(objects, titleField, contentField);
    }

    public void setObjects(List<T> objects) {
        if (StringUtils.isNotEmpty(this.adapter.getParentField())) {
            objects = GenericUtils.rearrange(objects, null, this.adapter.getParentField());
        }
        this.adapter.setObjects(objects);
    }

    public void addItem(T object) {
        if (StringUtils.isNotEmpty(this.adapter.getParentField())) {
            List<T> clonedObjects = new ArrayList<>(this.getObjects());
            clonedObjects.add(object);
            clonedObjects = GenericUtils.rearrange(clonedObjects, null, this.adapter.getParentField());

            int position = GenericUtils.getIndexOf(object, clonedObjects);
            if (position != -1) {
                this.adapter.insertItem(position, object);
            } else {
                this.adapter.addNewItem(object);
            }
        } else {
            this.adapter.addNewItem(object);
        }
    }

    public List<T> getObjects() {
        return this.adapter.getObjects();
    }

    public void setAction(String action) {
        this.adapter.setAction(action);
    }

    public String getAction() {
        return this.adapter.getAction();
    }

    public void setParentField(String parentField) {
        this.adapter.setParentField(parentField);
        this.adapter.setObjects(GenericUtils.rearrange(this.adapter.getObjects(), null, this.adapter.getParentField()));
    }

    public void setError(CharSequence error) {
        this.error = error;
        customListComponentError.setText(error);
    }

    public CharSequence getError() {
        return this.error;
    }

    public void scrollTo(int position) {
        if (-1 < position && position < this.getObjects().size()) {
            customListComponentMainElement.smoothScrollToPosition(position);
        }
    }

    public void setItemMaxLines(int maxLines) {
        this.adapter.setMaxLines(maxLines);
    }

    public int getItemMaxLines() {
        return this.adapter.getMaxLines();
    }

    public void setItemHeight(int height) {
        this.adapter.setHeight(height);
    }

    public int getItemHeight() {
        return this.adapter.getHeight();
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomListComponent, 0, 0);
        try {
            int itemMaxLines = typedArray.getInt(R.styleable.CustomListComponent_itemMaxLines, DEFAULT_ITEM_MAX_LINES);
            this.setItemMaxLines(itemMaxLines);

            if (typedArray.hasValue(R.styleable.CustomListComponent_itemHeight)) {
                int itemHeight = typedArray.getDimensionPixelSize(R.styleable.CustomListComponent_itemHeight, getContext().getResources().getDimensionPixelSize(R.dimen.list_item_height));
                this.setItemHeight(itemHeight);
            }
        } finally {
            typedArray.recycle();
        }
    }
}
