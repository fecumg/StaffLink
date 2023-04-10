package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomSelectedListAdapter;

public class CustomSelectedListComponent<T> extends LinearLayout {
    private static final String DEFAULT_MAIN_FIELD = "id";

    RecyclerView customSelectedListComponentMainElement;
    CustomSelectedListAdapter<T> adapter;

    public CustomSelectedListComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_selected_list_custom, this);
        customSelectedListComponentMainElement = view.findViewById(R.id.customSelectedListComponentMainElement);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customSelectedListComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomSelectedListAdapter<>(new ArrayList<>(), DEFAULT_MAIN_FIELD);
        customSelectedListComponentMainElement.setAdapter(adapter);
    }

    public void setData(List<T> objects, String mainField) {
        this.adapter.setData(objects,  mainField);
    }

    public void setObjects(List<T> objects) {
        this.adapter.setObjects(objects);
    }

    public List<T> getObjects() {
        return this.adapter.getObjects();
    }

    public void setMainField(String mainField) {
        this.adapter.setMainField(mainField);
    }

    public String getMainField() {
        return this.adapter.getMainField();
    }

    public void setAction(String action) {
        this.adapter.setAction(action);
    }

    public String getAction() {
        return this.adapter.getAction();
    }

    public void setCancellable(boolean cancellable) {
        this.adapter.setRemovable(cancellable);
    }

    public boolean isCancellable() {
        return this.adapter.isRemovable();
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSelectedListComponent, 0, 0);
        try {
            boolean cancellable = typedArray.getBoolean(R.styleable.CustomSelectedListComponent_removable, false);
            this.setCancellable(cancellable);
        } finally {
            typedArray.recycle();
        }
    }
}
