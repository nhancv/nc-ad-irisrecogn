package com.nhancv.irisrecognition;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.activity_main_iv_result);
        try {
            Mat img = Utils.loadResource(this, R.drawable.eye);
            Mat gryimg =  new Mat(new Size(100, 100),CvType.CV_8U);
            Imgproc.cvtColor(img, gryimg, Imgproc.COLOR_RGB2BGRA);
            Bitmap bm = Bitmap.createBitmap(gryimg.cols(), gryimg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(gryimg, bm);
            imageView.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
