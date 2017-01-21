package com.example.sibhali.facedet;

/**
 * Created by Home on 22-01-2017.
 */

import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Thread {
    private String serverName;
    private Socket socket;
    private int port = 6666;
    private InputStream in;
    private OutputStream out;

    Client() {

    }

    public void run() {
        serverName = MainActivity.jIP.getText().toString();
        try {
            while (true) {
                socket = new Socket(serverName, port);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                DisplayImageActivity.frame = BitmapFactory.decodeStream(new FlushedInputStream(in));
                DisplayImageActivity.frameChanged = true;
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(socket!=null)
                    socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

