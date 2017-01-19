package com.example.sibhali.facedet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sibhali on 12/19/2016.
 */
public class NotifyService extends Service {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;

    NotifyServiceReceiver notifyServiceReceiver;

    private static int MY_NOTIFICATION_ID;
    private static int MY_VIDEO_NOTIFICATION_ID;

    //public static String servername;
    private NotificationManager notificationManager;

    private static Thread t;

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        //Send notification

        final Context context = getApplicationContext();
        String notifTitle = "Someone is at your door!";
        String notifText = "Generating video...";

        String notifVdoTitle = "Someone is at your door! Video generated.";
        String notifVdoText = "Tap to watch video.";

        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif).setContentTitle(notifTitle).setContentText(notifText).setAutoCancel(true);
        final NotificationCompat.Builder notifVdoBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_video).setContentTitle(notifVdoTitle).setContentText(notifVdoText).setAutoCancel(true);

        notifBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notifVdoBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Toast.makeText(this, "Notification service started", Toast.LENGTH_LONG).show();

        final Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String servername = sp.getString("Pref_IP", "0");
        Log.d("servername", servername);

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Socket client = new Socket(servername, 6667);
                        System.out.println("CONNECTED "+"to " + servername);
                        InputStream in = client.getInputStream();
                        OutputStream out = client.getOutputStream();
                        int p = in.read();
                        System.out.println("NOTIF RECIEVED: "+String.valueOf(p));
                        out.write(1);
                        out.flush();
                        DataInputStream din = new DataInputStream(in);
                        MY_NOTIFICATION_ID = din.readInt();
                        client.close();

                        if (p == 1) {
                            notificationManager.notify(MY_NOTIFICATION_ID, notifBuilder.build());

                            Socket clientVdo = new Socket(servername, 6667);
                            InputStream inNotifVdo = clientVdo.getInputStream();
                            OutputStream outNotifVdo = clientVdo.getOutputStream();
                            int p2 = inNotifVdo.read();
                            System.out.print("NOTIF RECIEVED: " + String.valueOf(p2));
                            outNotifVdo.write(1);
                            outNotifVdo.flush();
                            DataInputStream dInNotifVdo = new DataInputStream(inNotifVdo);
                            MY_VIDEO_NOTIFICATION_ID = dInNotifVdo.readInt();
                            clientVdo.close();

                            System.out.println("VIDEO NOTIF ID RECEIVED: " + MY_VIDEO_NOTIFICATION_ID);

                            myIntent.putExtra("video_notif_id", MY_VIDEO_NOTIFICATION_ID);
                            stackBuilder.addNextIntent(myIntent);

                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            notifVdoBuilder.setContentIntent(pendingIntent);

                            System.out.println("GIVING NOTIFICATION NOWW!!.....................................................");

                            if (p2 == 2){
                                notificationManager.notify(MY_VIDEO_NOTIFICATION_ID, notifVdoBuilder.build());
                                System.out.println("NOTIF 2nd GIVEN");
                            }
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("DESTROYEDDD!", "HAHAHA!");
        Toast.makeText(NotifyService.this, "Notification service stopped", Toast.LENGTH_SHORT).show();
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int rqs = intent.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
            }
        }
    }
}
