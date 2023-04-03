package fpt.edu.stafflink.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import fpt.edu.stafflink.R;

public class CustomButtonComponent extends LinearLayout {

    Button customButtonComponentMainElement;

    private CharSequence text;
    private Drawable background;
    private int textColor;
    private float textSize;
    private Drawable foreground;
    private int foregroundTint;

    public CustomButtonComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View customView = inflate(context, R.layout.component_button_custom, this);
        customButtonComponentMainElement = customView.findViewById(R.id.customButtonComponentMainElement);

        this.setAttributes(attrs);
    }

    public void setOnClick(View.OnClickListener onClickListener) {
        customButtonComponentMainElement.setOnClickListener(onClickListener);
    }

    public void setText(CharSequence text) {
        this.text = text;
        customButtonComponentMainElement.setText(text);
    }

    public CharSequence getText() {
        return this.text;
    }

    public void setDrawableBackground(Drawable background) {
        this.background = background;
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        customButtonComponentMainElement.setBackground(background);
    }

    public Drawable getDrawableBackground() {
        return this.background;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        customButtonComponentMainElement.setTextColor(textColor);
    }

    public int getTextColor() {
        return this.textColor;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        customButtonComponentMainElement.setTextSize(textSize);
    }

    public float getTextSize() {
        return this.textSize;
    }

    public void setDrawableForeground(Drawable foreground) {
        this.foreground = foreground;
        setForeground(null);
        customButtonComponentMainElement.setForeground(foreground);
    }

    public Drawable getDrawableForeground() {
        return this.foreground;
    }

    public void setForegroundTint(int foregroundTint) {
        this.foregroundTint = foregroundTint;
        customButtonComponentMainElement.setForegroundTintList(ColorStateList.valueOf(foregroundTint));
    }

    public int getForegroundTint() {
        return this.foregroundTint;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomButtonComponent, 0, 0);
        try {
            if (typedArray.hasValue(R.styleable.CustomButtonComponent_android_text)) {
                this.setText(typedArray.getText(R.styleable.CustomButtonComponent_android_text));
            }

            if (typedArray.hasValue(R.styleable.CustomButtonComponent_android_background)) {
                this.setDrawableBackground(typedArray.getDrawable(R.styleable.CustomButtonComponent_android_background));
            }

            int textColor = typedArray.getColor(R.styleable.CustomButtonComponent_android_textColor, ContextCompat.getColor(getContext(), R.color.light));
            this.setTextColor(textColor);

            float defaultTextSize = getResources().getDimension(R.dimen.default_button_text_size) / getResources().getDisplayMetrics().density;
            float textSize = typedArray.getDimension(R.styleable.CustomButtonComponent_android_textSize, defaultTextSize);
            this.setTextSize(textSize);

            if (typedArray.hasValue((R.styleable.CustomButtonComponent_android_foreground))) {
                this.setDrawableForeground(typedArray.getDrawable(R.styleable.CustomButtonComponent_android_foreground));
            }

            int foregroundTint = typedArray.getColor(R.styleable.CustomButtonComponent_android_foregroundTint, ContextCompat.getColor(getContext(), R.color.light));
            this.setForegroundTint(foregroundTint);
        } finally {
            typedArray.recycle();
        }
    }
}
