package fpt.edu.stafflink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.DimenUtils;
import fpt.edu.stafflink.utilities.GenericUtils;

public class CustomCheckBoxAdapter<T> extends BaseAdapter<T, CustomCheckBoxAdapter.ViewHolder> {
    private static final int PADDING_STEP_IN_DP = 20;
    private String mainField;
    private LinkedList<T> checkedObjects;
    private String parentField;

    RecyclerView mRecyclerView;

    public CustomCheckBoxAdapter(List<T> objects, LinkedList<T> checkedObjects, String mainField) {
        super(objects);
        this.mainField = mainField;
        this.checkedObjects = checkedObjects;
    }

    public void setMainField(String mainField) {
        this.mainField = mainField;
        notifyItemRangeChanged(0, getItemCount());
    }
    public String getMainField() {
        return mainField;
    }

    public void setCheckedObjects(LinkedList<T> checkedObjects) {
        this.checkedObjects = checkedObjects;
        notifyItemRangeChanged(0, getItemCount());
    }
    public List<T> getCheckedObjects() {
        return this.checkedObjects;
    }

    public void setData(List<T> objects, LinkedList<T> checkedObjects, String mainField) {
        int formerItemCount = getItemCount();
        super.objects = objects;
        this.checkedObjects = checkedObjects;
        this.mainField = mainField;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    public String getParentField() {
        return this.parentField;
    }

    private int getObjectLevel(T object, int initialLevel) {
        T parent = this.getParentObject(object);
        if (parent != null) {
            return getObjectLevel(parent, ++ initialLevel);
        } else {
            return initialLevel;
        }
    }

    @SuppressWarnings("unchecked")
    public T getParentObject(T object) {
        if (StringUtils.isNotEmpty(this.parentField)) {
            Class<T> clazz = (Class<T>) object.getClass();
            try {
                Field field = clazz.getDeclaredField(this.parentField);
                field.setAccessible(true);
                Object value =  field.get(object);
                if (value != null && clazz.isInstance(value)) {
                    return clazz.cast(value);
                } else {
                    return null;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public List<T> getChildObjects(T object) {
        if (StringUtils.isNotEmpty(this.parentField)) {
            return super.getObjects().stream()
                    .filter(child -> {
                        int childParentId = super.getObjectId(this.getParentObject(child));
                        return childParentId != 0 && childParentId == super.getObjectId(object);
                    })
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_box, parent, false);
        this.mRecyclerView = (RecyclerView) parent;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }

        T object = getObjects().get(position);
        holder.itemCheckBoxMainElement.setText(this.getMainFieldValue(object));

        int objectId = super.getObjectId(object);

        if (checkedObjects.stream().anyMatch(checkedObject -> super.getObjectId(checkedObject) != 0 && super.getObjectId(checkedObject) == objectId)) {
            holder.itemCheckBoxMainElement.setChecked(true);
        }

        holder.itemCheckBoxMainElement.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                this.checkedObjects.add(object);
                System.out.println(getParentMap(object));
                this.getParentMap(object).forEach((parentPosition, parent) -> {
                    CheckBox parentCheckBox = mRecyclerView.getChildAt(parentPosition).findViewById(R.id.itemCheckBoxMainElement);
                    if (!parentCheckBox.isChecked()) {
                        parentCheckBox.setChecked(true);
                    }
                    this.checkedObjects.add(parent);
                });
            } else {
                this.removeCheckedObject(object);
                this.getChildMap(object).forEach((childPosition, child) -> {
                    CheckBox childCheckBox = mRecyclerView.getChildAt(childPosition).findViewById(R.id.itemCheckBoxMainElement);
                    if (childCheckBox.isChecked()) {
                        childCheckBox.setChecked(false);
                    }
                    this.removeCheckedObject(child);
                });
            }
        });

        if (StringUtils.isNotEmpty(parentField)) {
            int paddingStepInPx = DimenUtils.dpToPx(holder.itemView.getContext(), PADDING_STEP_IN_DP);
            holder.itemCheckBoxLayout.setPadding(paddingStepInPx * this.getObjectLevel(object, 0), 0, 0, 0);
        }
    }

    private HashMap<Integer, T> getParentMap(T object) {
        HashMap<Integer, T> parentMap = new HashMap<>();
        T parent = this.getParentObject(object);
        if (parent != null) {
            int parentPosition = super.getIndexOf(parent);
            if (parentPosition != -1) {
                parentMap.put(parentPosition, parent);
                parentMap.putAll(getParentMap(parent));
            }
        }
        return parentMap;
    }

    private HashMap<Integer, T> getChildMap(T object) {
        HashMap<Integer, T> childMap = new HashMap<>();
        this.getChildObjects(object)
                .forEach(child -> {
                    int childPosition = super.getIndexOf(child);
                    if (childPosition != -1) {
                        childMap.put(childPosition, child);
                        childMap.putAll(getChildMap(child));
                    }
                });
        return childMap;
    }

    private String getMainFieldValue(Object object) {
        return GenericUtils.getFieldValue(object, this.mainField);
    }

    private void removeCheckedObject(T object) {
        int objectId = super.getObjectId(object);
        for (T checkedObject: this.checkedObjects) {
            int checkedId = super.getObjectId(checkedObject);
            if (checkedId != 0 && checkedId == objectId) {
                checkedObjects.remove(checkedObject);
                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemCheckBoxLayout;
        CheckBox itemCheckBoxMainElement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckBoxLayout = itemView.findViewById(R.id.itemCheckBoxLayout);
            itemCheckBoxMainElement = itemView.findViewById(R.id.itemCheckBoxMainElement);
        }
    }
}
