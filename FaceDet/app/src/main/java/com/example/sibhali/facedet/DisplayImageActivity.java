package com.example.sibhali.facedet;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class DisplayImageActivity extends AppCompatActivity {
    public static ImageView img;
    public static boolean frameChanged = false;
    public static Bitmap frame = null;
    public static Button Exit_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        img = (ImageView) findViewById(R.id.imageView);
        Exit_button = (Button) findViewById(R.id.Exit_button);

        Thread t = new Client();
        t.start();

        Exit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (frameChanged) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageBitmap(frame);
                            }
                        });
                        frameChanged = false;
                    }
                }
            }
        });
        t2.start();

    }
}
