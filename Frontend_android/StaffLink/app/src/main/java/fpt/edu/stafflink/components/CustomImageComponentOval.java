package fpt.edu.stafflink.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.DimenUtils;

public class CustomImageComponentOval extends CustomImageComponent{
    public CustomImageComponentOval(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initView() {
        View view = inflate(getContext(), R.layout.component_image_custom_oval, this);

        super.customImageComponentLayout = view.findViewById(R.id.customImageComponentLayout);
        super.customImageComponentMainElement = view.findViewById(R.id.customImageComponentMainElement);
        super.customImageComponentWrapper = view.findViewById(R.id.customImageComponentWrapper);
        super.customImageComponentRemoveButton = view.findViewById(R.id.customImageComponentRemoveButton);
        super.customImageComponentIndex = view.findViewById(R.id.customImageComponentIndex);
    }

    @Override
    protected void setShadow() {
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.component_image_background_shadow);
        int numberOfLayers = layerDrawable != null ? layerDrawable.getNumberOfLayers() : 0;
        for (int i = 0; i < numberOfLayers; i ++) {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(i);

            gradientDrawable.setCornerRadius(R.dimen.oval_corner_size);

            if (i < numberOfLayers - 1) {
                gradientDrawable.setPadding(CustomImageComponent.SHADOW_STEP, 0, 0, CustomImageComponent.SHADOW_STEP);
            }
        }
        customImageComponentWrapper.setBackground(layerDrawable);
    }
}
