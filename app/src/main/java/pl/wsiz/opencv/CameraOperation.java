package pl.wsiz.opencv;

import android.content.Context;
import android.util.AttributeSet;

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
        System.out.println("Taking picture");
        this.pictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        System.out.println("Saving picture");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        try {
            FileOutputStream fos = new FileOutputStream(context.getFilesDir().getPath() + "/" + pictureFileName);
            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            System.out.println("Error while taking the picture");
        }
    }
}