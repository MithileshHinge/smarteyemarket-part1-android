package com.example.sibhali.facedet;

/**
 * Created by Home on 22-01-2017.
 */

import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class Client extends Thread {
    private String serverName;
    private int udpPort = 6663;
    private int port = 6666;
    private DatagramSocket udpSocket;
    private Socket socket;
    private boolean livefeed = true;

    Client() {

    }

    public void run() {
        try {
            serverName = MainActivity.jIP.getText().toString();
            socket = new Socket(serverName, port);

            while (true) {
                //socket = new Socket(serverName, port);
                //InputStream in = socket.getInputStream();
                //OutputStream out = socket.getOutputStream();
                //DataInputStream din = new DataInputStream(in);
                //int len = din.readInt();

                //out.write(1);

                udpSocket = new DatagramSocket(udpPort);
                byte[] buf = new byte[64000];
                DatagramPacket imgPacket = new DatagramPacket(buf, buf.length);
                udpSocket.receive(imgPacket);
                byte[] imgBuf = imgPacket.getData();

                DisplayImageActivity.frame = BitmapFactory.decodeByteArray(imgBuf, 0, imgBuf.length);
                //DisplayImageActivity.frame = BitmapFactory.decodeStream(new FlushedInputStream(in));
                DisplayImageActivity.frameChanged = true;
                //socket.close();
                udpSocket.close();
                if (!livefeed) {
                    socket.close();
                    livefeed = true;
                    return;
                }
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

    public void end(){
        livefeed = false;
    }
}

