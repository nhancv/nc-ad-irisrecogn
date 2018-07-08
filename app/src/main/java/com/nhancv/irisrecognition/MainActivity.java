package com.nhancv.irisrecognition;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        ImageView iv1 = findViewById(R.id.activity_main_iv_1);
        ImageView iv2 = findViewById(R.id.activity_main_iv_2);
        try {

            /**
             * img of one eye at least: 300x150 (~12KB)
             */
            Bitmap bm1 = toBitmap(houghTransform(toGrayMode(loadDrawable(R.drawable.eye1))));
            iv1.setImageBitmap(bm1);

            Bitmap bm2 = toBitmap(houghTransform(toGrayMode(loadDrawable(R.drawable.eye2))));
            iv2.setImageBitmap(bm2);

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
        Imgproc.morphologyEx(gray, gray, 4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        //thresholding
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_TRIANGLE);
        //edge detection
//        Imgproc.Canny(gray, gray, 5, 100);
        //gaussian blur reduce noise
//        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 2, 2);
        /** Hough circle detection
         image: 8-bit, single channel image. If working with a color image, convert to grayscale first.
         method: Defines the method to detect circles in images. Currently, the only implemented method is cv2.HOUGH_GRADIENT, which corresponds to the Yuen et al. paper.
         dp: This parameter is the inverse ratio of the accumulator resolution to the image resolution (see Yuen et al. for more details). Essentially, the larger the dp gets, the smaller the accumulator array gets.
         minDist: Minimum distance between the center (x, y) coordinates of detected circles. If the minDist is too small, multiple circles in the same neighborhood as the original may be (falsely) detected. If the minDist is too large, then some circles may not be detected at all.
         param1: Gradient value used to handle edge detection in the Yuen et al. method.
         param2: Accumulator threshold value for the cv2.HOUGH_GRADIENT method. The smaller the threshold is, the more circles will be detected (including false circles). The larger the threshold is, the more circles will potentially be returned.
         minRadius: Minimum size of the radius (in pixels).
         maxRadius: Maximum size of the radius (in pixels).
         */
        Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 400, 250, 15, 2, 80);

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
                Imgproc.circle(dst, pt, 3, new Scalar(255, 0, 0), iLineThickness);
            }

        return dst;
    }

    public Bitmap toBitmap(Mat src) {
        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bm);
        return bm;
    }
}
