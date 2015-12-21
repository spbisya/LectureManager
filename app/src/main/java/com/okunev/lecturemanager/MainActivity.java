package com.okunev.lecturemanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import android.widget.ZoomControls;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera.PictureCallback jpegCallback;
    FancyButton capture;
    FancyButton cancel;
    String path;
    int maxZoomLevel=0,currentZoomLevel=0;
    Camera.Parameters p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Bundle extras = getIntent().getExtras();
        path = extras.getString("DIR");
       /* recapture = (FancyButton)findViewById(R.id.recapture);
        recapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               refreshCamera();
                recapture.setVisibility(View.INVISIBLE);
                save.setVisibility(View.INVISIBLE);
            }
        });
*/
        cancel = (FancyButton)findViewById(R.id.back);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileBrowser.class);
                intent.putExtra("DIR", path);
                startActivity(intent);
            }
        });


   /*     save = (FancyButton)findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getData(data)) {
                    savePic(data);
                }
            }
        });*/

        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
               /* recapture.setVisibility(View.VISIBLE);
                save.setVisibility(View.VISIBLE);*/
           //     setData(data);
                capture = (FancyButton)findViewById(R.id.button);
                capture.setEnabled(false);
                savePic(data);
            }
        };
    }


/*    public void setData(byte[] data){
        this.data =data;
    }

    public Boolean getData(byte[] data){
if (data!=null){this.data = data; return true;}
        return false;
    }*/

    public void savePic(byte[] data){
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(String.format(path + "/image%d.lecture", System.currentTimeMillis()));
            outStream.write(data);
            outStream.close();
            Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        Toast.makeText(getApplicationContext(), "Picture Saved",Toast.LENGTH_SHORT).show();
    //    data=null;
        refreshCamera();
        capture = (FancyButton)findViewById(R.id.button);
        capture.setEnabled(true);
      /*  recapture.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);*/
    }



    public void captureImage() throws IOException {
        //take the picture
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {

            camera.stopPreview();
            ZoomControls zoomControls = (ZoomControls)findViewById(R.id.zoomControls);

            p = camera.getParameters();
            if (p.isZoomSupported() && p.isSmoothZoomSupported()) {
                //most phones

                maxZoomLevel = p.getMaxZoom();

                zoomControls.setIsZoomInEnabled(true);
                zoomControls.setIsZoomOutEnabled(true);

                zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (currentZoomLevel < maxZoomLevel) {
                            currentZoomLevel++;
                            camera.startSmoothZoom(currentZoomLevel);
                        }
                    }
                });
                zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (currentZoomLevel > 0) {
                            currentZoomLevel--;
                            camera.startSmoothZoom(currentZoomLevel);
                        }
                    }
                });
            } else if (p.isZoomSupported() && !p.isSmoothZoomSupported()){
                //stupid HTC phones
                maxZoomLevel = p.getMaxZoom();

                zoomControls.setIsZoomInEnabled(true);
                zoomControls.setIsZoomOutEnabled(true);

                zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (currentZoomLevel < maxZoomLevel) {
                            currentZoomLevel++;
                            p.setZoom(currentZoomLevel);
                            camera.setParameters(p);

                        }
                    }
                });

                zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (currentZoomLevel > 0) {
                            currentZoomLevel--;
                            p.setZoom(currentZoomLevel);
                            camera.setParameters(p);
                        }
                    }
                });
            }else{
                //no zoom on phone
                zoomControls.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        PackageManager pm = getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
            // True means the camera has autofocus mode on. Do what ever you want to do
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        camera.setParameters(p);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            camera = Camera.open();
        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();
       // param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        // modify parameter
        param.setPreviewSize(352, 288);
        camera.setParameters(param);
        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            camera.setDisplayOrientation(90);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    public void capture(View v) throws IOException {
       captureImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, FileBrowser.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
