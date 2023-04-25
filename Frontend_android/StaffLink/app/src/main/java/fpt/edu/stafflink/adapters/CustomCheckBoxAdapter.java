package fpt.edu.stafflink.adapters;

import android.graphics.drawable.Drawable;
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
    private Drawable buttonDrawable;
    private int textSize;
    private boolean hasBottomLine;

    RecyclerView mRecyclerView;

    private OnCheckChangedHandler<T> onCheckChangedHandler;

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

    public void setButtonDrawable(Drawable buttonDrawable) {
        this.buttonDrawable = buttonDrawable;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setHasBottomLine(boolean hasBottomLine) {
        this.hasBottomLine = hasBottomLine;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setCustomTextSize(int textSize) {
        this.textSize = textSize;
        notifyItemRangeChanged(0, getItemCount());
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
                    .filter(child -> this.getParentObject(child) != null && this.getParentObject(child).equals(object))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public void addNewItem(T object, boolean b) {
        if (b) {
            this.checkedObjects.add(object);
        }
        super.addNewItem(object);
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

        holder.itemCheckBoxMainElement.setChecked(checkedObjects.stream().anyMatch(checkedObject -> checkedObject.equals(object)));

        if (this.buttonDrawable != null) {
            holder.itemCheckBoxMainElement.setButtonDrawable(this.buttonDrawable);
        }

        if (this.textSize > 0) {
            holder.itemCheckBoxMainElement.setTextSize(textSize);
        }

        if (this.hasBottomLine) {
            holder.itemCheckBoxBottomLine.setVisibility(View.VISIBLE);
        } else {
            holder.itemCheckBoxBottomLine.setVisibility(View.GONE);
        }

        holder.itemCheckBoxMainElement.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                this.checkedObjects.add(object);
                if (StringUtils.isNotEmpty(this.parentField)) {
                    this.getParentMap(object).forEach((parentPosition, parent) -> {
                        CheckBox parentCheckBox = mRecyclerView.getChildAt(parentPosition).findViewById(R.id.itemCheckBoxMainElement);
                        if (!parentCheckBox.isChecked()) {
                            parentCheckBox.setChecked(true);
                        }
                        if (GenericUtils.getIndexOf(parent, checkedObjects) == -1) {
                            this.checkedObjects.add(parent);
                        }
                    });
                }
            } else {
                this.removeCheckedObject(object);
                if (StringUtils.isNotEmpty(this.parentField)) {
                    this.getChildMap(object).forEach((childPosition, child) -> {
                        CheckBox childCheckBox = mRecyclerView.getChildAt(childPosition).findViewById(R.id.itemCheckBoxMainElement);
                        if (childCheckBox.isChecked()) {
                            childCheckBox.setChecked(false);
                        }
                        if (GenericUtils.getIndexOf(child, checkedObjects) > -1) {
                            this.removeCheckedObject(child);
                        }
                    });
                }
            }

            if (this.onCheckChangedHandler != null) {
                this.onCheckChangedHandler.handle(object, b);
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
        for (T checkedObject: this.checkedObjects) {
            if (checkedObject.equals(object)) {
                checkedObjects.remove(checkedObject);
                break;
            }
        }
    }

    public void setOnCheckChangedHandler(OnCheckChangedHandler<T> onCheckChangedHandler) {
        this.onCheckChangedHandler = onCheckChangedHandler;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemCheckBoxLayout;
        CheckBox itemCheckBoxMainElement;
        View itemCheckBoxBottomLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckBoxLayout = itemView.findViewById(R.id.itemCheckBoxLayout);
            itemCheckBoxMainElement = itemView.findViewById(R.id.itemCheckBoxMainElement);
            itemCheckBoxBottomLine = itemView.findViewById(R.id.itemCheckBoxBottomLine);
        }
    }

    public interface OnCheckChangedHandler<T> {
        void handle(T object, boolean b);
    }
}
