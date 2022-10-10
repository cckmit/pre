package com.xd.pre.socekt.s2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PortTransfer extends Thread{

    private static final int bufferSize = 8092;
    private ServerSocket localServerSocket;
    private int localPort ;
    private String remoteHost ;
    private int remotPort;

    public static int soTimeOut = 300000;//5分钟超时
    public static List<String> remoteHosts = Arrays.asList("192.168.2.149");
    public static List<String> remotePorts = Arrays.asList("8080");
    public static List<String> serverFrontPorts = Arrays.asList("8081");

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {

            if ("-remoteHosts".equals(args[i])) {
                remoteHosts = Arrays.asList(args[i + 1].split(","));
                i++;
            } else if ("-serverFrontPorts".equals(args[i])) {
                serverFrontPorts = Arrays.asList(args[i + 1].split(","));
                i++;
            } else if ("-remotePorts".equals(args[i])) {
                remotePorts = Arrays.asList(args[i + 1].split(","));
                i++;
            }

        }

        for (int i = 0; i < remoteHosts.size(); i++) {
            String remoteHost = remoteHosts.get(i);
            int remotePort = Integer.parseInt(remotePorts.get(i));
            int serverFrontPort = Integer.parseInt(serverFrontPorts.get(i));
            new PortTransfer(serverFrontPort,remoteHost,remotePort).start();

        }


    }

    public PortTransfer(int localPort,String remoteHost,int remotPort){
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotPort = remotPort;
    }

    public void run() {
        try {
            localServerSocket = new ServerSocket(localPort);

            log("service started , listen on " +localServerSocket.getInetAddress().getHostAddress()+":"+ localPort + " , Remote:"+remoteHost+":"+remotPort);

            // 一直监听，接收到新连接，则开启新线程去处理
            while (true) {
                Socket localSocket = localServerSocket.accept();
                localSocket.setSoTimeout(soTimeOut); //5分钟内无数据传输、关闭链接
                log(localSocket.getRemoteSocketAddress() + "  conected");
                new SocketThread(localSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final static void log(Object message, Object... args) {
        Date dat = new Date();
        String msg = String.format("%1$tF %1$tT %2$-5s %3$s%n", dat, Thread.currentThread().getId(), String.format(message.toString(),args));
        System.out.print(msg);
    }

    class SocketThread extends Thread{


        private Socket localSocket;
        private Socket remoteSocket;
        private InputStream lin;
        private InputStream rin;
        private OutputStream lout;
        private OutputStream rout;

        public SocketThread(Socket socket) {
            this.localSocket = socket;
        }

        public void run() {
            try {

                remoteSocket = new Socket();
                remoteSocket.setSoTimeout(soTimeOut);//5分钟未收到数据，自动关闭资源
                remoteSocket.connect(new InetSocketAddress(remoteHost, remotPort));

                //设置超时，超过时间未收到客户端请求，关闭资源
                remoteSocket.setSoTimeout(soTimeOut); //5分钟内无数据传输、关闭链接
                rin = remoteSocket.getInputStream();
                rout = remoteSocket.getOutputStream();
                lin = localSocket.getInputStream();
                lout = localSocket.getOutputStream();

                new ReadThread().start();

                //写数据,负责读取客户端发送过来的数据，转发给远程
                byte[] data = new byte[bufferSize];
                int len = 0;
                while((len = lin.read(data)) > 0){
                    //System.out.println("------------>>>" + len);
                    if(len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
                        rout.write(data);
                    }else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
                        byte[] dest = new byte[len];
                        System.arraycopy(data, 0, dest, 0, len);
                        rout.write(dest);
                    }

                }

            } catch (Exception e) {
                //log(localSocket.getLocalAddress()+":"+localSocket.getPort() + " localSocket InputStream disconnected.");
            }finally {
                close();
            }
        }

        //关闭资源
        private void close(){
            try {
                if(remoteSocket !=null && !remoteSocket.isClosed()) {
                    remoteSocket.close();
                    log("remoteSocket>>>>" + remoteSocket.getRemoteSocketAddress() +" socket closed ");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                if(localSocket !=null && !localSocket.isClosed()) {
                    localSocket.close();
                    log("localSocket>>>>" + localSocket.getRemoteSocketAddress() +" socket closed ");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        //读数据线程负责读取远程数据后回写到客户端
        class ReadThread extends Thread {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[bufferSize];
                    int len = 0;
                    while((len = rin.read(data)) > 0){
                        if(len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
                            lout.write(data);
                        }else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
                            byte[] dest = new byte[len];
                            System.arraycopy(data, 0, dest, 0, len);
                            lout.write(dest);
                        }
                    }
                } catch (IOException e) {
                    //log(remoteSocket.getLocalAddress() + ":"+ remoteSocket.getPort() + " remoteSocket InputStream disconnected.");
                } finally {
                    close();
                }
            }

        }


    }

}

