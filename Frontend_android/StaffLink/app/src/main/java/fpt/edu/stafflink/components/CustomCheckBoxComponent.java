package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomCheckBoxAdapter;
import fpt.edu.stafflink.utilities.DimenUtils;

public class CustomCheckBoxComponent<T> extends LinearLayout {
    private static final String DEFAULT_MAIN_FIELD = "id";

    RecyclerView customCheckBoxComponentMainElement;
    TextView customCheckBoxComponentError;
    CustomCheckBoxAdapter<T> adapter;

    private boolean ableToChangePositions;
    private boolean ableToRemoveItems;
    private CharSequence error;

    private OnPositionChangedHandler onPositionChangedHandler;
    private OnRemovedHandler<T> onRemovedHandler;

    public CustomCheckBoxComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
        this.setAttributes(attrs);
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

    private void initItemTouchHelper(boolean ableToChangePositions, boolean ableToRemoveItems) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT) {
            int dragFrom = -1;
            int dragTo = -1;
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                if(dragFrom == -1) {
                    dragFrom =  fromPosition;
                }
                dragTo = toPosition;

                adapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    adapter.getObjects().add(dragTo, adapter.getObjects().remove(dragFrom));
                    if (onPositionChangedHandler != null) {
                        onPositionChangedHandler.handle();
                    }
                }
                dragFrom = dragTo = -1;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem(viewHolder, direction);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return ableToChangePositions;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return ableToRemoveItems;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(customCheckBoxComponentMainElement);
    }

    private void removeItem(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        T object = adapter.getObjects().get(position);

        if (direction == ItemTouchHelper.LEFT) {
            adapter.removeItem(position);

            if (getCheckedObjects().contains(object)) {
                adapter.removeCheckedObject(object);
            }

            if (this.onRemovedHandler != null) {
                this.onRemovedHandler.handle(object);
            }
        }
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

    public void addNewItem(T object) {
        this.adapter.addNewItem(object);
    }

    public void insertItem(int position, T object) {
        this.adapter.insertItem(position, object);
    }

    public void addNewItem(T object, boolean b) {
        this.adapter.addNewItem(object, b);
    }

    public void scrollTo(int position) {
        if (-1 < position && position < this.getObjects().size()) {
            this.customCheckBoxComponentMainElement.smoothScrollToPosition(position);
        }
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

    public void setOnCheckChangedHandler(CustomCheckBoxAdapter.OnCheckChangedHandler<T> onCheckChangedHandler) {
        this.adapter.setOnCheckChangedHandler(onCheckChangedHandler);
    }

    private List<T> rearrange(List<T> objects, T parent) {
        List<T> rearrangedList = new ArrayList<>();
        objects.forEach(object -> {
            if ((this.adapter.getParentObject(object) == null && parent == null) || (this.adapter.getParentObject(object) != null && this.adapter.getParentObject(object).equals(parent))) {
                rearrangedList.add(object);
                rearrangedList.addAll(rearrange(objects, object));
            }
        });
        return rearrangedList;
    }

    public void setButtonDrawable(Drawable buttonDrawable) {
        this.adapter.setButtonDrawable(buttonDrawable);
    }

    public void setCustomTextSize(int textSize) {
        this.adapter.setCustomTextSize(textSize);
    }

    public void setAbleToRemoveItems(boolean ableToRemoveItems) {
        this.ableToRemoveItems = ableToRemoveItems;
        this.initItemTouchHelper(this.ableToChangePositions, this.ableToRemoveItems);
    }

    public void setAbleToChangePositions(boolean ableToChangePositions) {
        this.ableToChangePositions = ableToChangePositions;
        this.initItemTouchHelper(this.ableToChangePositions, this.ableToRemoveItems);
    }

    public void setHasBottomLine(boolean hasBottomLine) {
        this.adapter.setHasBottomLine(hasBottomLine);
    }

    public void setCheckEnabled(boolean enabled) {
        this.adapter.setCheckEnabled(enabled);
    }

    public void setOnPositionChangedHandler(OnPositionChangedHandler onPositionChangedHandler) {
        this.onPositionChangedHandler = onPositionChangedHandler;
    }

    public void setOnRemovedHandler(OnRemovedHandler<T> onRemovedHandler) {
        this.onRemovedHandler = onRemovedHandler;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCheckBoxComponent, 0, 0);
        try {
            if (typedArray.hasValue(R.styleable.CustomCheckBoxComponent_android_textSize)) {
                int textSizeInPx = typedArray.getDimensionPixelSize(R.styleable.CustomCheckBoxComponent_android_textSize, (int) customCheckBoxComponentError.getTextSize());
                int textSizeInDp = DimenUtils.pxToDp(getContext(), textSizeInPx);
                this.setCustomTextSize(textSizeInDp);
            }

            if (typedArray.hasValue(R.styleable.CustomCheckBoxComponent_buttonDrawable)) {
                this.setButtonDrawable(typedArray.getDrawable(R.styleable.CustomCheckBoxComponent_buttonDrawable));
            }

            boolean ableToRemoveItems = typedArray.getBoolean(R.styleable.CustomCheckBoxComponent_ableToRemoveItems, false);
            this.setAbleToRemoveItems(ableToRemoveItems);

            boolean ableToChangePositions = typedArray.getBoolean(R.styleable.CustomCheckBoxComponent_ableToChangePositions, false);
            this.setAbleToChangePositions(ableToChangePositions);

            boolean hasBottomLine = typedArray.getBoolean(R.styleable.CustomCheckBoxComponent_hasBottomLine, false);
            this.setHasBottomLine(hasBottomLine);

            boolean enabled = typedArray.getBoolean(R.styleable.CustomCheckBoxComponent_enabled, true);
            this.setCheckEnabled(enabled);
        } finally {
            typedArray.recycle();
        }
    }

    public interface OnRemovedHandler<T> {
        void handle(T object);
    }

    public interface OnPositionChangedHandler {
        void handle();
    }
}
