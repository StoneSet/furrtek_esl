package org.furrtek.pricehax;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements Callback {
    private AutoFocusCallback autoFocusCallback;
    private Camera mCamera;
    private SurfaceHolder mHolder = getHolder();
    private PreviewCallback previewCallback;

    public CameraPreview(Context context, Camera camera, PreviewCallback previewCallback, AutoFocusCallback autoFocusCallback) {
        super(context);
        this.mCamera = camera;
        this.previewCallback = previewCallback;
        this.autoFocusCallback = autoFocusCallback;
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (this.mHolder.getSurface() != null) {
            try {
                this.mCamera.stopPreview();
            } catch (Exception e) {
            }
            try {
                this.mCamera.setDisplayOrientation(90);
                this.mCamera.setPreviewDisplay(this.mHolder);
                this.mCamera.setPreviewCallback(this.previewCallback);
                this.mCamera.startPreview();
                this.mCamera.autoFocus(this.autoFocusCallback);
            } catch (Exception e2) {
                Log.d("DBG", "Error starting camera preview: " + e2.getMessage());
            }
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (this.mCamera != null) {
                this.mCamera.setPreviewDisplay(surfaceHolder);
            }
        } catch (IOException e) {
            Log.d("DBG", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
}
