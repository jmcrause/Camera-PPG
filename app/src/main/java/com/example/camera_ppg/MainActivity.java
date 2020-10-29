package com.example.camera_ppg;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

import com.softmoore.android.graphlib.Function;
import com.softmoore.android.graphlib.Graph;
import com.softmoore.android.graphlib.GraphView;
import com.softmoore.android.graphlib.Label;
import com.softmoore.android.graphlib.Point;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback  {

    private Camera mCamera;
    private TextureView mTextureView;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = findViewById(R.id.mTextureView);
        final Button btn = findViewById(R.id.measure_button);
        final ImageView overlay = findViewById(R.id.imageView);

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                if(btn.getText() == getString(R.string.start)){

                }
                else {
                    openCamera(surfaceTexture);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                }

                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });



        //Button press
        //..set what happens on click
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn.getText() == getString(R.string.start)){
                    btn.setText(R.string.stop);

                    overlay.setVisibility(View.INVISIBLE);

                    openCamera(mTextureView.getSurfaceTexture());
                }
                else{
                    btn.setText(R.string.start);

                    mCamera.stopPreview();

                    overlay.setVisibility(View.VISIBLE);
                }

            }
        });


        //Graph drawing

        Point[] points =
                {
                        new Point(0, 3), new Point(1, 4),  new Point(2, 2),
                        new Point(3, 0),   new Point(4, 6),  new Point(5,3),
                        new Point(6,5),    new Point(7, 9),   new Point(8, 6),
                        new Point(9, 9),   new Point(10, 6)
                };

        Graph graph = new Graph.Builder()
                .setWorldCoordinates(0, 10, 0, 10)
                .addLineGraph(points, Color.RED)
                .setYTicks(new double[] {})
                .setXTicks(new double[] {})
                .build();
        GraphView graphView = findViewById(R.id.graph_view);

        graphView.setGraph(graph);

    }

    private void openCamera(SurfaceTexture surface) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestCameraPermission();
            }
        }

        mCamera = Camera.open(0);



        try {
            mCamera.setPreviewTexture(surface);
            int rotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            mCamera.setDisplayOrientation(ORIENTATIONS.get(rotation));
            Camera.Parameters params =  mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            mCamera.setParameters(params);
            mCamera.startPreview();
        } catch (IOException ioe) {
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }




}

