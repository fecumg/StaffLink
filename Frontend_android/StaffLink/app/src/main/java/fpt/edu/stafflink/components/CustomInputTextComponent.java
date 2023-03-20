package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fpt.edu.stafflink.R;

public class CustomInputTextComponent extends LinearLayout {

    private static final int DEFAULT_MAX_LENGTH = 180;
    TextInputLayout customInputTextComponentLayout;
    TextInputEditText customInputTextComponentMainElement;

    boolean isFirstFocus = true;

    public CustomInputTextComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = inflate(context, R.layout.component_input_text_custom, this);
        customInputTextComponentLayout = view.findViewById(R.id.customInputTextComponentLayout);
        customInputTextComponentMainElement = view.findViewById(R.id.customInputTextComponentMainElement);

        this.setAttributes(attrs);
    }

    public void setHint(CharSequence hint) {
        customInputTextComponentLayout.setHint(hint);
        customInputTextComponentMainElement.setHint(hint);
    }
    public CharSequence getHint() {
        return customInputTextComponentLayout.getHint();
    }

    public void setText(CharSequence charSequence) {
        customInputTextComponentMainElement.setText(charSequence);
    }
    public CharSequence getText() {
        return customInputTextComponentMainElement.getText();
    }

    public void setTextColor(int color) {
        customInputTextComponentMainElement.setTextColor(color);
    }
    public int getTextColor() {
        return customInputTextComponentMainElement.getCurrentTextColor();
    }

    public void setStartIconDrawable(int drawable) {
        customInputTextComponentLayout.setStartIconDrawable(ContextCompat.getDrawable(getContext(), drawable));
    }
    public Drawable getStartIconDrawable() {
        return customInputTextComponentLayout.getStartIconDrawable();
    }

    public void setColor(int color) {
        customInputTextComponentLayout.setStartIconTintList(ColorStateList.valueOf(color));
        customInputTextComponentLayout.setDefaultHintTextColor(ColorStateList.valueOf(color));
        customInputTextComponentMainElement.setBackgroundTintList(ColorStateList.valueOf(color));

        customInputTextComponentMainElement.setOnFocusChangeListener((view, b) -> {
            if (b && isFirstFocus) {
                view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.transparent)));
                view.setBackgroundTintList(ColorStateList.valueOf(color));
                isFirstFocus = false;
            }
        });
    }
    public int getColor() {
        ColorStateList colorStateList = customInputTextComponentLayout.getDefaultHintTextColor();
        if (colorStateList != null) {
            return colorStateList.getDefaultColor();
        } else {
            return 0;
        }
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomInputTextComponent, 0, 0);

        if (typedArray.hasValue(R.styleable.CustomInputTextComponent_android_hint)) {
            this.setHint(typedArray.getText(R.styleable.CustomInputTextComponent_android_hint));
        }

        if (typedArray.hasValue(R.styleable.CustomInputTextComponent_android_text)) {
            this.setText(typedArray.getText(R.styleable.CustomInputTextComponent_android_text));
        }

        int textColor = typedArray.getColor(R.styleable.CustomInputTextComponent_android_textColor, ContextCompat.getColor(getContext(), R.color.dark));
        this.setTextColor(textColor);

        if (typedArray.hasValue(R.styleable.CustomInputTextComponent_startIconDrawable)) {
            customInputTextComponentLayout.setStartIconDrawable(typedArray.getDrawable(R.styleable.CustomInputTextComponent_startIconDrawable));
        }

        int color = typedArray.getColor(R.styleable.CustomInputTextComponent_color, ContextCompat.getColor(getContext(), R.color.dark));
        this.setColor(color);

        int maxLength = typedArray.getInt(R.styleable.CustomInputTextComponent_android_maxLength, DEFAULT_MAX_LENGTH);
        customInputTextComponentMainElement.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        typedArray.recycle();
    }
}
