package fpt.edu.stafflink.components;

import android.content.Context;
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

    public CustomButtonComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = inflate(context, R.layout.component_button_custom, this);
        customButtonComponentMainElement = view.findViewById(R.id.customButtonComponentMainElement);

        this.setAttributes(attrs);
    }

    public void setText(CharSequence text) {
        customButtonComponentMainElement.setText(text);
    }

    public CharSequence getText() {
        return customButtonComponentMainElement.getText();
    }

    public void setBackground(int drawable) {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        customButtonComponentMainElement.setBackground(ContextCompat.getDrawable(getContext(), drawable));
    }

    public Drawable getBackground() {
        return customButtonComponentMainElement.getBackground();
    }

    public void setTextColor(int color) {
        customButtonComponentMainElement.setTextColor(color);
    }

    public int getTextColor() {
        return customButtonComponentMainElement.getCurrentTextColor();
    }

    public void setTextSize(float size) {
        customButtonComponentMainElement.setTextSize(size);
    }

    public float getTextSize() {
        return customButtonComponentMainElement.getTextSize();
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomButtonComponent, 0, 0);

        if (typedArray.hasValue(R.styleable.CustomButtonComponent_android_text)) {
            this.setText(typedArray.getText(R.styleable.CustomButtonComponent_android_text));
        }

        if (typedArray.hasValue(R.styleable.CustomButtonComponent_android_background)) {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
            customButtonComponentMainElement.setBackground(typedArray.getDrawable(R.styleable.CustomButtonComponent_android_background));
        }
        int textColor = typedArray.getColor(R.styleable.CustomButtonComponent_android_textColor, ContextCompat.getColor(getContext(), R.color.light));
        this.setTextColor(textColor);

        float defaultTextSize = getResources().getDimension(R.dimen.default_button_text_size) / getResources().getDisplayMetrics().density;
        float textSize = typedArray.getDimension(R.styleable.CustomButtonComponent_android_textSize, defaultTextSize);
        this.setTextSize(textSize);

        typedArray.recycle();
    }
}
