package pl.wsiz.opencv;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;

/**
 * Created by jakub on 21.11.2019.
 */

public class CameraOperation extends JavaCameraView implements android.hardware.Camera.PictureCallback {

    private String pictureFileName;
    private Context context;

    public CameraOperation(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void takePicture(final String fileName) {
        Log.d("photo", "Taking picture");
        this.pictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        Log.d("photo", "Saving picture");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        try {
         //   FileOutputStream fos = new FileOutputStream(context.getFilesDir().getPath() + "/" + pictureFileName);
            FileOutputStream fos = new FileOutputStream("/storage/emulated/0/Pictures/openCV/" + pictureFileName);
            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("photo", "Error while taking the picture");
        }
    }
}