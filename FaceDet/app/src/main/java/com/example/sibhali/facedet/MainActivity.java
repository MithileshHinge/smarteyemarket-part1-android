package com.example.sibhali.facedet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static ImageView jIV;
    public static Context context;
    public static EditText jIP;
    public static VideoView jVV;
    public static String servername;
    public static Button LiveFeed;
    private static Socket sReceiveVideo;

    private static int videoNotifID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        LiveFeed = (Button) findViewById(R.id.livefeedbutton);

        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String lastIP = sp.getString("Pref_IP", "");
        jIV = (ImageView) findViewById(R.id.xIV);
        jIP = (EditText) findViewById(R.id.xIP);
        jVV = (VideoView) findViewById(R.id.xVV);
        jIP.setText(lastIP);
        servername = lastIP;
        ToggleButton xB = (ToggleButton) findViewById(R.id.xB);
        assert xB != null;

        jVV.setMediaController(new MediaController(this));

        Intent intent = getIntent();
        videoNotifID = intent.getIntExtra("video_notif_id", -1);

        if (videoNotifID != -1){
            jIV.setVisibility(View.VISIBLE);
        }


        File fTempDir = getFilesDir();
        deleteRecursive(fTempDir);


        jIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socketVdo = new Socket(servername, 6668);
                            OutputStream outVdo = socketVdo.getOutputStream();
                            DataOutputStream doutVdo = new DataOutputStream(outVdo);
                            doutVdo.writeInt(videoNotifID);
                            doutVdo.flush();

                            InputStream inVdo = socketVdo.getInputStream();
                            DataInputStream dInVdo = new DataInputStream(inVdo);
                            int filenameSize = dInVdo.readInt();
                            byte[] filenameInBytes = new byte[filenameSize];
                            outVdo.write(1);
                            outVdo.flush();
                            inVdo.read(filenameInBytes);
                            final String filename = new String(filenameInBytes);
                            outVdo.write(1);
                            outVdo.flush();

                            FileOutputStream fileOut = openFileOutput(filename, MODE_PRIVATE);
                            byte[] buffer = new byte[16*1024];
                            int count;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    jIV.setImageResource(R.drawable.ic_schedule_24dp);
                                }
                            });
                            while((count = inVdo.read(buffer)) > 0){
                                fileOut.write(buffer, 0, count);
                            }
                            fileOut.close();
                            socketVdo.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Download successful.", Toast.LENGTH_LONG).show();
                                    jIV.setVisibility(View.GONE);
                                    String filepath = getFilesDir().getPath()+"/"+filename;
                                    jVV.setVideoPath(filepath);
                                    jVV.setVisibility(View.VISIBLE);
                                    jVV.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            jVV.start();
                                        }
                                    });
                                }
                            });
                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Download failed.", Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        xB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    servername = jIP.getText().toString();
                    SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("Pref_IP", servername);
                    editor.commit();

                    Intent intent = new Intent(getBaseContext(), NotifyService.class);
                    MainActivity.this.startService(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setAction(NotifyService.ACTION);
                    intent.putExtra("RQS", NotifyService.RQS_STOP_SERVICE);
                    sendBroadcast(intent);
                }
            }

        });
    }

    private void deleteRecursive(File fTempDir) {
        if (fTempDir.isDirectory()){
            for (File fTemp : fTempDir.listFiles()) {
                deleteRecursive(fTemp);
            }
        }

        fTempDir.delete();
    }

    @Override
    protected void onStop() {
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (jIP == null) editor.putString("Pref_IP", servername);
        else editor.putString("Pref_IP", jIP.getText().toString());
        editor.commit();
        super.onStop();
    }


    public Bitmap getImageBitmap(Context context,String name){
        name=name+".jpg";
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    public void open_livefeed(View view) {
        Intent intent = new Intent(this, DisplayImageActivity.class);
        startActivity(intent);
    }

}
