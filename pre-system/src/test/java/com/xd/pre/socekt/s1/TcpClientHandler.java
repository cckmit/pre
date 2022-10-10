package com.xd.pre.socekt.s1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private InputStream in;
    public TcpClientHandler(Socket socket)
    {
        this.clientSocket=socket;
    }

    public void run()
    {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            in = clientSocket.getInputStream();
            while(true)
            {
                String inputLine="";
                byte[] temp = new byte[1024];
                int length = in.read(temp);
                if(length==-1){
                    continue;
                }
                inputLine+=new String(temp,0,length);
                if(".".equals(inputLine))
                {
                    out.println("bye");
                    break;
                }
                System.err.println(inputLine);
                out.println(inputLine);
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}