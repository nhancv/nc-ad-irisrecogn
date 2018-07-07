package com.nhancv.irisrecognition;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        //OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivCanny = findViewById(R.id.activity_main_iv_canny);
        try {

            Bitmap bm = toBitmap(houghTransform(toGrayMode(loadDrawable(R.drawable.eye2))));
            ivCanny.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView ivHough = findViewById(R.id.activity_main_iv_hough);
        try {

            Bitmap bm = toBitmap(houghTransform(toGrayMode(loadDrawable(R.drawable.eye))));
            ivHough.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Mat loadDrawable(int resId) throws IOException {
        return Utils.loadResource(this, resId);
    }

    public Mat toGrayMode(Mat src) {
        try {
            Mat gray = new Mat(src.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
            return gray;
        } catch (Exception ex) {
            return src;
        }
    }

    public Mat cannyEdgeDetection(Mat gray) {
        Mat edge = new Mat();
        Mat dst = new Mat();
        Imgproc.Canny(gray, edge, 80, 100);
        Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2BGRA, 4);
        return dst;
    }

    /**
     * 1. conversion to greyscale
     * 2. morphological gradient
     * 3. thresholding
     * 4. hough circle detection
     */
    public Mat houghTransform(Mat gray) {
        Mat dst = gray.clone();
        Mat circles = new Mat();
        //morphology
        Imgproc.morphologyEx(gray, gray, 4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));
        //thresholding
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_OTSU);
        //edge detection
        Imgproc.Canny(gray, gray, 5, 100);
        //gaussian blur reduce noise
//        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 2, 2);
        //hough circle detection
        // param1 = gradient value used to handle edge detection
        // param2 = Accumulator threshold value for thecv2.CV_HOUGH_GRADIENT method.
        // The smaller the threshold is, the more circles will bedetected (including false circles).
        // The larger the threshold is, the more circles will potentially be returned.
        double param1 = 5, param2 = 100;
        // min and max radii (set these values as you desire)
        int minRadius = 0, maxRadius = 100;
        Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, gray.rows() / 8, param1, param2, minRadius, maxRadius);

        //draw hough circle
        Log.e(TAG, "houghTransform: " + circles.size());
        int iLineThickness = 2;
        if (circles.cols() > 0)
            for (int x = 0; x < circles.cols(); x++) {
                double vCircle[] = circles.get(0, x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int) Math.round(vCircle[2]);

                // draw the found circle
                Imgproc.circle(dst, pt, radius, new Scalar(255, 0, 0), iLineThickness);
                Imgproc.circle(dst, pt, 3, new Scalar(255, 255, 0), iLineThickness);
            }

        return dst;
    }

    public Bitmap toBitmap(Mat src) {
        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bm);
        return bm;
    }
}
