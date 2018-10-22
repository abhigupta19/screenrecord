package com.example.user.screenrecord;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
   private VideoView videoView;
   private ToggleButton t;
   private String videouri="";
   private static final int REQ=1000;
    private static final int REP=1001;
    private static final SparseIntArray a=new SparseIntArray();
    private MediaProjectionManager ma;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCALLBACK mediaProjectionCALLBACK;
    private int mscreendensity;
    private static int displaywidth=720;
    private static int displayheight=1280;
    private MediaRecorder mediaRecorder;
    private RelativeLayout relativeLayout;
    private MediaProjectionManager mediaProjectionManager;
    static {
        a.append(Surface.ROTATION_0,90);
        a.append(Surface.ROTATION_90,0);
        a.append(Surface.ROTATION_180,270);
        a.append(Surface.ROTATION_270,180);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView=(VideoView)findViewById(R.id.vid);
        t=(ToggleButton)findViewById(R.id.togg);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mscreendensity=displayMetrics.densityDpi;
        mediaRecorder=new MediaRecorder();
        mediaProjectionManager=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
           relativeLayout=(RelativeLayout)findViewById(R.id.relativeLayout);
           displaywidth=displayMetrics.widthPixels;
           displayheight=displayMetrics.heightPixels;
         t.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)+
                         ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                 {
                     if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                             ||(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO)))
                     {
                         t.setChecked(false);
                         Snackbar.make(relativeLayout,"Permission",Snackbar.LENGTH_INDEFINITE).
                                 setAction("ENABLE", new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                         Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                         Manifest.permission.RECORD_AUDIO},REP );
                             }

                         }).show();


                     }
                     else
                     {
                         ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                 Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                 Manifest.permission.RECORD_AUDIO},REP );
                     }

                 }

                 else {
                     togglescreenshare(v);
                 }

             }
         });

    }

    private void togglescreenshare(View v) {
        if(((ToggleButton)v).isChecked())
        {
           initRecord();
           recordScreen();
        }
        else
        {
            mediaRecorder.stop();
            mediaRecorder.reset();
            stoprecordscreen();
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videouri));
            videoView.start();


        }

    }

    private void initRecord() {
        try
        {

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            videouri=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-YYYY-hh_mm_ss").format(new Date())) .append(".mp4").toString();
            mediaRecorder.setOutputFile(videouri);
            mediaRecorder.setVideoSize(displaywidth,displayheight);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512*1000);
            mediaRecorder.setVideoFrameRate(30);
            int roation=getWindowManager().getDefaultDisplay().getRotation();
            int orenation=a.get(roation + 90);
            mediaRecorder.setOrientationHint(orenation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=REQ)
      {
          Toast.makeText(MainActivity.this,"Ukn error",Toast.LENGTH_SHORT).show();
          return;
      }
    /*  if(requestCode!=RESULT_OK)
      {
          Toast.makeText(MainActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
          t.setChecked(false);
          return;
      }*/
      mediaProjectionCALLBACK=new MediaProjectionCALLBACK();
        mediaProjection=mediaProjectionManager.getMediaProjection(resultCode,data);
        mediaProjection.registerCallback(mediaProjectionCALLBACK,null);
        virtualDisplay=createVirtualDisplay();
        mediaRecorder.start();

    }

    private void recordScreen()
    {
        if(mediaProjection==null)
        {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQ);
            return;
        }
        virtualDisplay =createVirtualDisplay();
        mediaRecorder.start();

    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("Mainactivity",displaywidth,displayheight,mscreendensity,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        ,mediaRecorder.getSurface(),null,null);
    }


    private class MediaProjectionCALLBACK extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if(t.isChecked()==true)
            {
                t.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection=null;
            stoprecordscreen();
            super.onStop();
        }
    }

    private void stoprecordscreen() {
        if(virtualDisplay==null)
        {
            virtualDisplay.release();
            destroyMediaProjection();
            return;


        }
    }

    private void destroyMediaProjection() {
        mediaProjection.unregisterCallback(mediaProjectionCALLBACK);
        mediaProjection.stop();
        mediaProjection=null;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REP:
            {
                if((grantResults.length>0)&&(grantResults[0]+grantResults[1]==PackageManager.PERMISSION_GRANTED))
                {
                    togglescreenshare(t);
                }
                else
                {
                    t.setChecked(true);
                    Snackbar.make(relativeLayout,"Permission",Snackbar.LENGTH_INDEFINITE).
                            setAction("ENABLE", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.RECORD_AUDIO},REP );
                                }

                            }).show();

                }
                return;
            }
        }
    }
}
