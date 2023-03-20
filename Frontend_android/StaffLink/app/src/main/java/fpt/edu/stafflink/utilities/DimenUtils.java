package fpt.edu.stafflink.utilities;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimenUtils {
    public int pixelToDp(int pixels) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        return (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, pixels, displaymetrics );
    }
}
