package com.xd.pre.socekt.s1;


import java.io.IOException;
import java.net.ServerSocket;

public class TcpMultiServer {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            new TcpClientHandler(serverSocket.accept()).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args) throws Exception {
        new TcpMultiServer().start(2323);
    }

}
