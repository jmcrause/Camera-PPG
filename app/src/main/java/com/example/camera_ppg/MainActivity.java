package com.example.camera_ppg;


import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import androidx.annotation.RequiresApi;

import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;
import com.softmoore.android.graphlib.Function;
import com.softmoore.android.graphlib.Graph;
import com.softmoore.android.graphlib.GraphView;
import com.softmoore.android.graphlib.Label;
import com.softmoore.android.graphlib.Point;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;



public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback  {

    //private Graph graph;
    //private  GraphView graphView;
    float[] used_points = new float[300];
    float[] window_view =  new float[100];
    int frame_num = 0;
    int count;
    private TextView testText;
    private TextView redText;
    private TextView greenText;
    private TextView blueText;
    private boolean active = Boolean.FALSE;
    private LineChart chart;

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
        //mTextureView = findViewById(R.id.mTextureView);
        final Intent starterIntent = getIntent();

        final CameraView camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        final Button btn = findViewById(R.id.measure_button);
        final ImageView overlay = findViewById(R.id.imageView);

        testText = findViewById(R.id.textView2);
        redText = findViewById(R.id.textRed);
        greenText = findViewById(R.id.textGreen);
        blueText = findViewById(R.id.textBlue);

        chart = findViewById(R.id.graph_view);

        final LineData data = new LineData();

        // add empty data
        chart.setData(data);


        //Button press
        //..set what happens on click
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn.getText() == getString(R.string.start)){
                    active = true;

                    btn.setText(R.string.stop);

                    overlay.setVisibility(View.INVISIBLE);

                    redText.setVisibility(View.VISIBLE);
                    greenText.setVisibility(View.VISIBLE);
                    blueText.setVisibility(View.VISIBLE);

                    //openCamera(mTextureView.getSurfaceTexture());

                    camera.setFlash(Flash.TORCH);

                    frame_num = 0;
                }
                else{
                    finish();
                    startActivity(starterIntent);
                }

            }
        });



        camera.addFrameProcessor(new FrameProcessor() {
                                         @Override
                                         @WorkerThread
                                         public void process(@NonNull Frame frame) {
                                             long time = frame.getTime();
                                             Size size = frame.getSize();
                                             //int format = frame.getFormat();
                                             //int userRotation = frame.getRotationToUser();
                                             //int viewRotation = frame.getRotationToView();
                                             if (frame.getDataClass() == byte[].class) {
                                                 byte[] data = frame.getData();
                                                 
                                                 if (active){
                                                     float redAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), size.getHeight(), size.getWidth());
                                                     float greenAvg = ImageProcessing.decodeYUV420SPtoGreenAvg(data.clone(), size.getHeight(), size.getWidth());
                                                     float blueAvg = ImageProcessing.decodeYUV420SPtoBlueAvg(data.clone(), size.getHeight(), size.getWidth());

                                                     updateColor(redAvg, greenAvg, blueAvg);

                                                     if (redAvg > 200 && greenAvg < 10) {
                                                         addEntry((float)255.0-redAvg, time);
                                                     }
                                                 }
                                             } /*else if (frame.getDataClass() == Image.class) {
                                                 Image data = frame.getData();
                                                 // Process android.media.Image...
                                             }*/

                                         }
        });


        //Graph drawing

    }

    private void addEntry(float imgAvg, float t) {


        //testText.setText(Float.toString(t));

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), imgAvg), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

/*            window_view[frame_num] = imgAvg;
            if (frame_num < 99){
                frame_num = frame_num + 1;
            } else {
                frame_num = 0;
            }

            float max = 0;
            float min = 255;
            float sum = 0;
            int c;
           count++;
            if (count < 100){
                c = count;
            } else {
                c = 100;
            }

            for (int i = 0; i < c; i++)
            {

                if (window_view[i] > max) {
                    max = window_view[i];
                }
                if (window_view[i] < min) {
                    min = window_view[i];
                }
                sum += window_view[i];
            }
            float ave = sum/c;
            float diff = max - min;*/

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(100);
            //chart.setVisibleYRange(1,5,AxisDependency.LEFT);
            //chart.setVisibleYRange((int) Math.floor(diff),(int) Math.ceil(diff),AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
            //chart.moveViewTo(data.getEntryCount(),ave,AxisDependency.LEFT);

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
        //testText.setText("Error");


    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "PPG");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.RED);
        //set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        //set.setCircleRadius(0);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        //set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void updateColor(float redAvg, float greenAvg, float blueAvg) {
        redText.setText(Float.toString(redAvg));
        greenText.setText(Float.toString(greenAvg));
        blueText.setText(Float.toString(blueAvg));
    }

}

