package fpt.edu.stafflink.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomCheckBoxAdapter;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomCheckBoxComponent<T> extends LinearLayout {
    private static final String DEFAULT_MAIN_FIELD = "id";

    RecyclerView customCheckBoxComponentMainElement;
    TextView customCheckBoxComponentError;
    CustomCheckBoxAdapter<T> adapter;

    private CharSequence error;

    public CustomCheckBoxComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_check_box_custom, this);
        customCheckBoxComponentMainElement = view.findViewById(R.id.customCheckBoxComponentMainElement);
        customCheckBoxComponentError = view.findViewById(R.id.customCheckBoxComponentError);

//        RecyclerView.LayoutManager layoutManager = new FlexboxLayoutManager(getContext(), FlexDirection.ROW, FlexWrap.WRAP);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customCheckBoxComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomCheckBoxAdapter<>(new ArrayList<>(), new LinkedList<>(), CustomCheckBoxComponent.DEFAULT_MAIN_FIELD);
        customCheckBoxComponentMainElement.setAdapter(adapter);
    }

    public void setData(List<T> objects, LinkedList<T> checkedObjects, String mainField) {
        if (StringUtils.isNotEmpty(this.adapter.getParentField())) {
            objects = this.rearrange(objects, null);
        }
        this.adapter.setData(objects, checkedObjects,  mainField);
    }

    public void setObjects(List<T> objects) {
        if (StringUtils.isNotEmpty(this.adapter.getParentField())) {
            objects = this.rearrange(objects, null);
        }
        this.adapter.setObjects(objects);
    }

    public List<T> getObjects() {
        return this.adapter.getObjects();
    }

    public void setCheckedObjects(LinkedList<T> checkedObjects) {
        this.adapter.setCheckedObjects(checkedObjects);
    }

    public List<T> getCheckedObjects() {
        return this.adapter.getCheckedObjects();
    }

    public void setMainField(String mainField) {
        this.adapter.setMainField(mainField);
    }

    public String getMainField() {
        return this.adapter.getMainField();
    }

    public void setError(CharSequence error) {
        this.error = error;
        customCheckBoxComponentError.setText(error);
    }

    public CharSequence getError() {
        return this.error;
    }

    public void setParentField(String parentField) {
        this.adapter.setParentField(parentField);
        this.adapter.setObjects(rearrange(this.adapter.getObjects(), null));
    }

    private List<T> rearrange(List<T> objects, T parent) {
        List<T> rearrangedList = new ArrayList<>();
        objects.forEach(object -> {
            int parentId = GenericUtils.getObjectId(parent);
            int objectParentId = GenericUtils.getObjectId(this.adapter.getParentObject(object));
            if (parentId == objectParentId) {
                rearrangedList.add(object);
                rearrangedList.addAll(rearrange(objects, object));
            }
        });
        return rearrangedList;
    }
}
