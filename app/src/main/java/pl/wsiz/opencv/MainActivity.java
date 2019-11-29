package pl.wsiz.opencv;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    public static final int JAVA_DETECTOR = 0;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraOperation mOpenCvCameraView;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            mJavaDetector = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnClickListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        mDetectorName = new String[1];
        mDetectorName[JAVA_DETECTOR] = "JAVA";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        permissionCamera();
        super.onCreate(savedInstanceState);
        addWindowFlags();
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraOperation) findViewById(R.id.my_camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);

        Intent serviceIntent = new Intent(this, SpeechRecognizeService.class);
        startService(serviceIntent);
    }

    private void addWindowFlags() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        List<Rect> findFaces = faces.toList();
        findFaces.forEach(f -> Imgproc.rectangle(mRgba, f.tl(), f.br(), FACE_RECT_COLOR, 3));

        return mRgba;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        stopService(new Intent(this, SpeechRecognizeService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        stopService(new Intent(this, SpeechRecognizeService.class));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            System.out.println("OpencCV is configuered successfully");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            System.out.println("OpencCV is not configuered successfully");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, baseLoaderCallback);
        }
        startService(new Intent(this, SpeechRecognizeService.class));
    }

    public void permissionCamera() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        System.out.println("TOUCH");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = "Image_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);

      //  speechRecognizer();
    }

    private void speechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            SpeechIntentRecognizer speechRecognizer = new SpeechIntentRecognizer();
            switch (requestCode) {
                case 1:
                    int intFound = speechRecognizer.getIntentFromResult(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
                    if (intFound == 0)
                        showToast("Success!");
                    else
                        showToast("Sorry, I didn't catch that! Please try again");
                    break;
            }
        } else
            showToast("Failed to recognize speech!");
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        View toastView = toast.getView();
        toast.setView(toastView);
        toast.show();
    }
}
