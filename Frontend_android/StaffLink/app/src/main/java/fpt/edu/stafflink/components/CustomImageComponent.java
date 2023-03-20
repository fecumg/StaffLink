package fpt.edu.stafflink.components;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.tbruyelle.rxpermissions3.RxPermissions;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.enums.ShapeAppearance;
import io.reactivex.rxjava3.disposables.Disposable;

public class CustomImageComponent extends LinearLayout {

    private static final int SHADOW_STEP = 2;
    private static final float ORIGINAL_RATIO = 1f;
    private static final float ZOOM_OUT_RATIO = 0.97f;
    private static final float ZOOM_PIVOT = 0.5f;
    private static final long ZOOM_ANIMATION_DURATION = 300L;
    private int shape;
    private boolean hasShadow;
    private int src;
    private String url;
    private ShapeableImageView customImageComponentMainElement;
    private CardView customImageComponentWrapper;
    private String path;
    private boolean ableToPickImage;
    private RxPermissions rxPermissions;
    private Disposable permissionDisposal;

    public CustomImageComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.setAttributes(attrs);
    }

    private AppCompatActivity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (AppCompatActivity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public void scaleWithAnimation(View view, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF, ZOOM_PIVOT,
                Animation.RELATIVE_TO_SELF, ZOOM_PIVOT);
        anim.setFillAfter(true);
        anim.setDuration(ZOOM_ANIMATION_DURATION);
        view.startAnimation(anim);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void enablePickImage() {
        AppCompatActivity activity = getActivity();
        if (activity != null) {
            rxPermissions = new RxPermissions(activity);

            ActivityResultLauncher<Intent> pickImageActivityResultLauncher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> pickImage(result, activity)
            );

            customImageComponentMainElement.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    scaleWithAnimation(customImageComponentWrapper, ORIGINAL_RATIO, ZOOM_OUT_RATIO);

                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    scaleWithAnimation(customImageComponentWrapper, ZOOM_OUT_RATIO, ORIGINAL_RATIO);

                    customImageComponentMainElement.performClick();

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> permissionDisposal = rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    pickImageActivityResultLauncher.launch(intent);
                                } else {
                                    Toast.makeText(activity, "Gallery access denied", Toast.LENGTH_SHORT).show();
                                }
                            }), ZOOM_ANIMATION_DURATION);

                    return true;
                }
                return false;
            });
        }
    }

    private void pickImage(ActivityResult result, AppCompatActivity activity) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri selectedImage = result.getData().getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = activity.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            this.path = imagePath;
            customImageComponentMainElement.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            permissionDisposal.dispose();
        }
    }

    public View setShape(int shape) {
        this.shape = shape;
        View view;
        if (shape == ShapeAppearance.CIRCLE) {
            view = inflate(getContext(), R.layout.component_image_custom_circle, this);
        } else {
            view = inflate(getContext(), R.layout.component_image_custom_rectangle, this);
        }
        return view;
    }

    public int getShape() {
        return this.shape;
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        if (hasShadow) {
            this.setShadow(shape);
        } else {
            this.removeShadow();
        }
    }

    public boolean hasShadow() {
        return this.hasShadow;
    }

    private void setShadow(int shape) {
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.component_image_background_shadow);
        int numberOfLayers = layerDrawable != null ? layerDrawable.getNumberOfLayers() : 0;
        for (int i = 0; i < numberOfLayers; i ++) {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(i);

            if (shape == ShapeAppearance.CIRCLE) {
                gradientDrawable.setCornerRadius(R.dimen.circle_corner_size);
            } else {
                gradientDrawable.setCornerRadius(getResources().getDimension(R.dimen.rectangle_corner_size));
            }

            if (i < numberOfLayers - 1) {
                gradientDrawable.setPadding(SHADOW_STEP, 0, 0, SHADOW_STEP);
            }
        }
        customImageComponentWrapper.setBackground(layerDrawable);
    }

    private void removeShadow() {
        customImageComponentWrapper.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light));
    }

    public void setSrc(int src) {
        this.src = src;
        customImageComponentMainElement.setImageDrawable(ContextCompat.getDrawable(getContext(), src));
    }

    public int getSrc() {
        return this.src;
    }

    public void setUrl(String url) {
        this.url = url;
        Glide.with(customImageComponentMainElement)
                .load(url)
                .into(customImageComponentMainElement);
    }

    public String getUrl() {
        return this.url;
    }

    public String getPath() {
        return this.path;
    }

    public void setAbleToPickImage(boolean ableToPickImage) {
        this.ableToPickImage = ableToPickImage;
    }

    public boolean isAbleToPickImage() {
        return ableToPickImage;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageComponent, 0, 0);

        shape = typedArray.getInt(R.styleable.CustomImageComponent_shape, ShapeAppearance.RECTANGLE);
        View view = this.setShape(shape);

        customImageComponentMainElement = view.findViewById(R.id.customImageComponentMainElement);
        customImageComponentWrapper = view.findViewById(R.id.customImageComponentWrapper);

        hasShadow = typedArray.getBoolean(R.styleable.CustomImageComponent_hasShadow, true);
        if (hasShadow) {
            this.setShadow(shape);
        }

        if (typedArray.hasValue(R.styleable.CustomImageComponent_android_src)) {
            customImageComponentMainElement.setImageDrawable(typedArray.getDrawable(R.styleable.CustomImageComponent_android_src));
        }

        if (typedArray.hasValue(R.styleable.CustomImageComponent_url)) {
            this.setUrl(typedArray.getString(R.styleable.CustomImageComponent_url));
        }

        ableToPickImage = typedArray.getBoolean(R.styleable.CustomImageComponent_ableToPickImage, false);
        if (ableToPickImage) {
            enablePickImage();
        }

        typedArray.recycle();
    }
}
