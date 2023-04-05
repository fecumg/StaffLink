package fpt.edu.stafflink.components;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.imageview.ShapeableImageView;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.io.File;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.utilities.ActivityUtils;
import fpt.edu.stafflink.utilities.DimenUtils;
import io.reactivex.rxjava3.disposables.Disposable;

public class CustomImageComponent extends RelativeLayout {

    protected static final int SHADOW_STEP = 2;
    private static final float ORIGINAL_RATIO = 1f;
    private static final float ZOOM_OUT_RATIO = 0.97f;
    private static final float ZOOM_PIVOT = 0.5f;
    private static final long ZOOM_ANIMATION_DURATION = 300L;
    private static final int CANCEL_BUTTON_SPACING = 20;
    private static final int PADDING_TO_INDEX_IN_DP = 2;

    RelativeLayout customImageComponentLayout;
    ShapeableImageView customImageComponentMainElement;
    CardView customImageComponentWrapper;
    ImageButton customImageComponentRemoveButton;
    TextView customImageComponentIndex;

    private boolean hasShadow;
    private Drawable src;
    private String url;
    private File file;
    private boolean ableToPickImage;
    private int tint;
    private boolean cancellable;
    private int index;

    private RxPermissions rxPermissions;
    private Disposable permissionDisposal;

    public CustomImageComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initView();
        this.setAttributes(attrs);
        this.setIndexDimensions();
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
    private void enablePickImage() {
        AppCompatActivity activity = ActivityUtils.getActivity(getContext());
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

                    postDelayed(() -> permissionDisposal = rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouch(OnTouchEventHandler handler) {
        if (this.ableToPickImage) {
            return;
        }
        customImageComponentMainElement.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                scaleWithAnimation(customImageComponentWrapper, ORIGINAL_RATIO, ZOOM_OUT_RATIO);

                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                scaleWithAnimation(customImageComponentWrapper, ZOOM_OUT_RATIO, ORIGINAL_RATIO);
                customImageComponentMainElement.performClick();

                postDelayed(handler::handle, ZOOM_ANIMATION_DURATION);

                return true;
            }
            return false;
        });
    }

    public interface OnTouchEventHandler {
        void handle();
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

            this.url = null;
            Glide.with(customImageComponentMainElement)
                    .clear(customImageComponentMainElement);

            this.file = new File(imagePath);
            customImageComponentMainElement.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            customImageComponentMainElement.setImageTintList(null);

            permissionDisposal.dispose();
        }
    }

    public void initView() {
        View view = inflate(getContext(), R.layout.component_image_custom_rectangular, this);

        customImageComponentLayout = view.findViewById(R.id.customImageComponentLayout);
        customImageComponentMainElement = view.findViewById(R.id.customImageComponentMainElement);
        customImageComponentWrapper = view.findViewById(R.id.customImageComponentWrapper);
        customImageComponentRemoveButton = view.findViewById(R.id.customImageComponentRemoveButton);
        customImageComponentIndex = view.findViewById(R.id.customImageComponentIndex);
    }

    public void setIndexDimensions() {
        ViewTreeObserver viewTreeObserver = customImageComponentMainElement.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                customImageComponentMainElement.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = customImageComponentMainElement.getMeasuredHeight();
                int indexHeightInDp = DimenUtils.pxToDp(getContext(), height) - PADDING_TO_INDEX_IN_DP;
                int indexHeight = DimenUtils.dpToPx(getContext(), indexHeightInDp);

                customImageComponentIndex.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, indexHeight));
            }
        });
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        if (hasShadow) {
            this.setShadow();
            this.setIndex(0);
        } else {
            this.removeShadow();
        }
    }

    public boolean hasShadow() {
        return this.hasShadow;
    }

    protected void setShadow() {
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.component_image_background_shadow);
        int numberOfLayers = layerDrawable != null ? layerDrawable.getNumberOfLayers() : 0;
        for (int i = 0; i < numberOfLayers; i ++) {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(i);

            gradientDrawable.setCornerRadius(getResources().getDimension(R.dimen.rectangle_corner_size));

            if (i < numberOfLayers - 1) {
                gradientDrawable.setPadding(SHADOW_STEP, 0, 0, SHADOW_STEP);
            }
        }
        customImageComponentWrapper.setBackground(layerDrawable);
    }

    private void removeShadow() {
        customImageComponentWrapper.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light));
    }

    public void setSrc(Drawable src) {
        this.src = src;
        customImageComponentMainElement.setImageDrawable(src);
    }

    public Drawable getSrc() {
        return this.src;
    }

    public void setUrl(String url) {
        this.url = url;
        this.file = null;
        Glide.with(customImageComponentMainElement)
                .load(url)
                .placeholder(src)
                .error(src)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        customImageComponentMainElement.setImageTintList(null);
                        return false;
                    }
                })
                .into(customImageComponentMainElement);
    }

    public String getUrl() {
        return this.url;
    }

    public File getFile() {
        return this.file;
    }

    public void setAbleToPickImage(boolean ableToPickImage) {
        this.ableToPickImage = ableToPickImage;
        if (ableToPickImage) {
            this.enablePickImage();
        }
    }

    public boolean isAbleToPickImage() {
        return ableToPickImage;
    }

    public void setImageTint(int tint) {
        this.tint = tint;
        customImageComponentMainElement.setImageTintList(ColorStateList.valueOf(tint));
    }

    public int getImageTint() {
        return this.tint;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        if (cancellable) {
            params.setMargins(0, DimenUtils.dpToPx(getContext(), CANCEL_BUTTON_SPACING), DimenUtils.dpToPx(getContext(), CANCEL_BUTTON_SPACING), 0);
            customImageComponentWrapper.setLayoutParams(params);

            customImageComponentRemoveButton.setVisibility(VISIBLE);

            customImageComponentRemoveButton.setOnClickListener(view -> cancelImage());

            this.setIndex(0);
        } else {
            params.setMargins(0, 0, 0, 0);
            customImageComponentWrapper.setLayoutParams(params);

            customImageComponentRemoveButton.setVisibility(GONE);
        }
    }

    private void cancelImage() {
        customImageComponentMainElement.setImageDrawable(src);
        customImageComponentMainElement.setImageTintList(ColorStateList.valueOf(tint));
        this.file = null;
        this.url = null;
    }

    public boolean isCancellable() {
        return this.cancellable;
    }

    public void setIndex(int index) {
        this.index = index;
        if (index > 0) {
            customImageComponentIndex.setVisibility(VISIBLE);
            customImageComponentIndex.setText(String.valueOf(index));

            this.setCancellable(false);
            this.setHasShadow(false);
        } else {
            customImageComponentIndex.setVisibility(GONE);
        }
    }

    public int getIndex() {
        return this.index;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageComponent, 0, 0);
        try {
            this.hasShadow = typedArray.getBoolean(R.styleable.CustomImageComponent_hasShadow, true);
            if (this.hasShadow) {
                this.setShadow();
            }

            if (typedArray.hasValue(R.styleable.CustomImageComponent_android_src)) {
                this.setSrc(typedArray.getDrawable(R.styleable.CustomImageComponent_android_src));
            }

            if (typedArray.hasValue(R.styleable.CustomImageComponent_url)) {
                this.setUrl(typedArray.getString(R.styleable.CustomImageComponent_url));
            }

            this.ableToPickImage = typedArray.getBoolean(R.styleable.CustomImageComponent_ableToPickImage, false);
            this.setAbleToPickImage(ableToPickImage);

            int tint = typedArray.getColor(R.styleable.CustomImageComponent_tint, ContextCompat.getColor(getContext(), R.color.light));
            this.setImageTint(tint);

            boolean cancellable = typedArray.getBoolean(R.styleable.CustomImageComponent_cancellable, false);
            this.setCancellable(cancellable);

            if (typedArray.hasValue(R.styleable.CustomImageComponent_index)) {
                this.setIndex(typedArray.getIndex(R.styleable.CustomImageComponent_index));
            }
        } finally {
            typedArray.recycle();
        }
    }
}
