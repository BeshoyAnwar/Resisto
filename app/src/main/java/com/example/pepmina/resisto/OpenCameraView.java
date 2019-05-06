package com.example.pepmina.resisto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class OpenCameraView extends JavaCameraView implements Camera.PictureCallback {

    private static final String TAG = OpenCameraView.class.getSimpleName();

    private String mPictureFileName;

    public static int minWidthQuality = 400;

    private Context context;
    private int picNo=0;

    public OpenCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);

        mCamera.takePicture(null, null, this);
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        int xCenter=bitmap.getWidth()/2;
        int yCenter=bitmap.getHeight()/2;
        int rectWidth=(int)(bitmap.getWidth()*0.06);
        int rectHeight=(int)(bitmap.getHeight()*0.06);
        bitmap=Bitmap.createBitmap(bitmap,xCenter-rectWidth/2, yCenter-rectHeight/2,rectWidth, rectHeight);
        Uri uri = Uri.parse(mPictureFileName);

        Log.d(TAG, "selectedImage: " + uri);
        Bitmap bm = null;
        bm = rotate(bitmap, 90);

        Mat mat = new Mat();
        Bitmap bmp32 = bm.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        String savedImageURL = MediaStore.Images.Media.insertImage(
                this.context.getContentResolver(),
                bm,
                "Bird",
                "Image of bird"
        );
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setTitle("Resisto");
        Image a= null;
        try {
            a = new Image(mat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(a.getColorsImages().size()>=3)
        {
            Vector<String> colors=a.getColorsNames();
            Resistance r=new Resistance(colors);
            dlgAlert.setMessage("Resistance value= "+r.toString());

        }
        else
        {
            dlgAlert.setMessage("Please readjust the resistance and capture image again.");
            //dlgAlert.setPositiveButton("Please readjust the resistance and capture image again.", null);
        }

        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void autoFocus()
    {
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        Camera.Parameters params = mCamera.getParameters();
//*EDIT*//params.setFocusMode("continuous-picture");
//It is better to use defined constraints as opposed to String, thanks to AbdelHady
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setPreviewFpsRange(30000, 30000);

        mCamera.setParameters(params);
    }
    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return bmOut;
        }
        return bm;
    }
}
