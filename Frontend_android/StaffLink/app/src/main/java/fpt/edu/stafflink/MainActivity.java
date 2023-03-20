package fpt.edu.stafflink;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import fpt.edu.stafflink.components.CustomButtonComponent;
import fpt.edu.stafflink.components.CustomImageComponent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomButtonComponent customButton = findViewById(R.id.customButton);

//        customButton.setTextColor(ContextCompat.getColor(this, R.color.black));
        customButton.setBackground(R.drawable.component_button_background_primary);

        CustomImageComponent image = findViewById(R.id.image);

//        image.setHasShadow(false);
        image.setUrl("https://developer.accuweather.com/sites/default/files/02-s.png");

    }
}