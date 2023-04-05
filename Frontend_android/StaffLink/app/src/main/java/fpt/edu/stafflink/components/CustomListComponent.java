package fpt.edu.stafflink.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomListAdapter;

public class CustomListComponent<T> extends LinearLayout {
    private static final String ERROR_TAG = "CustomListComponent";

    RecyclerView customListComponentMainElement;

    TextView customListComponentError;
    public CustomListAdapter<T> adapter;

    private CharSequence error;

    public CustomListComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initView(context);
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
        this.adapter.setData(objects, titleField, contentField);
    }

    public void setObjects(List<T> objects) {
        this.adapter.setObjects(objects);
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

    public void setError(CharSequence error) {
        this.error = error;
        customListComponentError.setText(error);
    }

    public CharSequence getError() {
        return this.error;
    }
}
