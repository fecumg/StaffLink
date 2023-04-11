package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.DimenUtils;

public class CustomInputTextComponent extends LinearLayout {

    private static final int DEFAULT_MAX_LENGTH = 180;
    private static final int ERROR_ENABLE_LAYOUT_BOTTOM_MARGIN_IN_DP = -10;
    TextInputLayout customInputTextComponentLayout;
    TextInputEditText customInputTextComponentMainElement;

    private CharSequence hint;
    private int textColor;
    private Drawable drawable;
    private int color;
    private int maxLength;
    private int inputType;
    private CharSequence error;
    private boolean editable;

    private boolean isFirstFocus = true;

    public CustomInputTextComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = inflate(context, R.layout.component_input_text_custom, this);
        customInputTextComponentLayout = view.findViewById(R.id.customInputTextComponentLayout);
        customInputTextComponentMainElement = view.findViewById(R.id.customInputTextComponentMainElement);

        this.setAttributes(attrs);
    }

    public void setOnTextChanged(TextWatcher textWatcher) {
        customInputTextComponentMainElement.addTextChangedListener(textWatcher);
    }

    public void setHint(CharSequence hint) {
        this.hint = hint;
        customInputTextComponentLayout.setHint(hint);
//        customInputTextComponentMainElement.setHint(hint);
    }
    public CharSequence getHint() {
        return this.hint;
    }

    public void setText(CharSequence text) {
        customInputTextComponentMainElement.setText(text);
    }
    public CharSequence getText() {
        return customInputTextComponentMainElement.getText();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        customInputTextComponentMainElement.setTextColor(textColor);
    }
    public int getTextColor() {
        return this.textColor;
    }

    public void setStartIconDrawable(Drawable drawable) {
        this.drawable = drawable;
        customInputTextComponentLayout.setStartIconDrawable(drawable);
    }
    public Drawable getStartIconDrawable() {
        return this.drawable;
    }

    public void setColor(int color) {
        this.color = color;

        customInputTextComponentLayout.setStartIconTintList(ColorStateList.valueOf(color));
        customInputTextComponentLayout.setDefaultHintTextColor(ColorStateList.valueOf(color));
        customInputTextComponentMainElement.setBackgroundTintList(ColorStateList.valueOf(color));

        customInputTextComponentMainElement.setOnFocusChangeListener((view, b) -> post(() -> {
            if (b && isFirstFocus) {
                view.setBackgroundTintList(null);
                view.setBackgroundTintList(ColorStateList.valueOf(color));
                isFirstFocus = false;
            }
        }));
    }
    public int getColor() {
        return this.color;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        customInputTextComponentMainElement.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
        customInputTextComponentMainElement.setInputType(inputType);
    }

    public int getInputType() {
        return this.inputType;
    }

    public void setError(CharSequence error) {
        this.error = error;
        customInputTextComponentLayout.setError(error);
    }

    public CharSequence getError() {
        return this.error;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        customInputTextComponentMainElement.setEnabled(editable);
        if (!editable) {
            this.setColor(ContextCompat.getColor(getContext(), R.color.secondary));
        }
    }

    public boolean isEditable() {
        return this.editable;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomInputTextComponent, 0, 0);
        try {
            if (typedArray.hasValue(R.styleable.CustomInputTextComponent_android_hint)) {
                this.setHint(typedArray.getText(R.styleable.CustomInputTextComponent_android_hint));
            }

            if (typedArray.hasValue(R.styleable.CustomInputTextComponent_android_text)) {
                this.setText(typedArray.getText(R.styleable.CustomInputTextComponent_android_text));
            }

            int textColor = typedArray.getColor(R.styleable.CustomInputTextComponent_android_textColor, ContextCompat.getColor(getContext(), R.color.dark));
            this.setTextColor(textColor);

            if (typedArray.hasValue(R.styleable.CustomInputTextComponent_startIconDrawable)) {
                this.setStartIconDrawable(typedArray.getDrawable(R.styleable.CustomInputTextComponent_startIconDrawable));
            }

            int color = typedArray.getColor(R.styleable.CustomInputTextComponent_color, ContextCompat.getColor(getContext(), R.color.dark));
            this.setColor(color);

            int maxLength = typedArray.getInt(R.styleable.CustomInputTextComponent_android_maxLength, DEFAULT_MAX_LENGTH);
            this.setMaxLength(maxLength);

            int inputType = typedArray.getInt(R.styleable.CustomInputTextComponent_android_inputType, EditorInfo.TYPE_CLASS_TEXT);
            this.setInputType(inputType);

            if (typedArray.hasValue(R.styleable.CustomInputTextComponent_error)) {
                this.setError(typedArray.getText(R.styleable.CustomInputTextComponent_error));
            }

            this.setEditable(typedArray.getBoolean(R.styleable.CustomInputTextComponent_android_enabled, true));
        } finally {
            typedArray.recycle();
        }
    }
}
