package com.xd.pre.socekt.s2;

import java.io.*;

import java.net.InetAddress;

import java.net.ServerSocket;

import java.net.Socket;

import java.nio.ByteBuffer;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import java.util.Arrays;

import java.util.Date;

import java.util.concurrent.CountDownLatch;

/**

 * Created with Intellij IDEA

 *

 * @author: jiaoyiping

 * Mail: jiaoyiping@gmail.com

 * Date: 2018/03/01

 * Time: 22:08

 * To change this template use File | Settings | Editor | File and Code Templates

 */

public class MySocket5Server implements Runnable {

    private static final int SOCKS_PROTOCOL_4 = 0X04;

    private static final int SOCKS_PROTOCOL_5 = 0X05;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private static final byte TYPE_IPV4 = 0x01;

    private static final byte TYPE_IPV6 = 0X02;

    private static final byte TYPE_HOST = 0X03;

    private static final byte ALLOW_PROXY = 0X5A;

    private static final byte DENY_PROXY = 0X5B;

    private Socket sourceSocket;

    @Override

    public void run() {

        String remoteAddress = sourceSocket.getRemoteSocketAddress().toString();

        log("process socket:" + remoteAddress);

        InputStream sourceIn = null, proxyIn = null;

        OutputStream sourceOut = null, proxyOut = null;

        Socket proxySocket = null;

        try {

            sourceIn = sourceSocket.getInputStream();

            sourceOut = sourceSocket.getOutputStream();

            //从协议头中获取socket的类型

            byte[] tmp = new byte[1];

            int n = sourceIn.read(tmp);

            if (n == 1) {

                int protocol = tmp[0];

                //socket4

                if (SOCKS_PROTOCOL_4 == protocol) {

                    proxySocket = convertToSocket4(sourceIn, sourceOut);

                } else if (SOCKS_PROTOCOL_5 == protocol) {

                    proxySocket = convertToSocket5(sourceIn, sourceOut);

                } else {

                    log("Socket协议错误,不是Socket4或者Socket5");

                }

                //socket转换

                if (null != proxySocket) {

                    CountDownLatch countDownLatch = new CountDownLatch(1);

                    proxyIn = proxySocket.getInputStream();

                    proxyOut = proxySocket.getOutputStream();

                    transfer(sourceIn, proxyOut, countDownLatch);

                    transfer(proxyIn, sourceOut, countDownLatch);

                    try {

                        countDownLatch.await();

                    } catch (InterruptedException e) {

                        e.printStackTrace();

                    }

                }

            } else {

                log("SOCKET ERROR: " + tmp.toString());

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            closeIO(sourceIn);

            closeIO(proxyIn);

            closeIO(proxyOut);

            closeIO(proxyIn);

            closeIO(proxySocket);

            closeIO(sourceSocket);

        }

    }

    public MySocket5Server(Socket sourceSocket) {

        this.sourceSocket = sourceSocket;

    }

    private static final void log(String message) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println(dateFormat.format(new Date()) + " - " + message);

    }

    private static void startServer(int port) {

        log("config =>> port=" + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Socket socket = null;

            while ((socket = serverSocket.accept()) != null) {

                new Thread(new MySocket5Server(socket)).start();

            }

            log("close socket(this never happen)");

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private Socket convertToSocket4(InputStream inputStream, OutputStream outputStream) throws IOException {

        Socket proxySocket = null;

        byte[] tmp = new byte[3];

        inputStream.read(tmp);

        // 请求协议|VN1|CD1|DSTPORT2|DSTIP4|NULL1|

        int port = ByteBuffer.wrap(tmp, 1, 2).asShortBuffer().get() & 0xFFFF;

        String host = getHost((byte) 0x01, inputStream);

        inputStream.read();

        //返回一个8字节的响应协议: |VN1|CD1|DSTPORT2|DSTIP 4|

        byte[] response = new byte[8];

        try {

            proxySocket = new Socket(host, port);

            response[1] = ALLOW_PROXY;

            log("connect " + tmp[1] + "host: " + host + " ,port: " + port);

        } catch (Exception e) {

            response[1] = DENY_PROXY;

            log("connect error,host: " + host + " ,port: " + port);

        }

        outputStream.write(response);

        outputStream.flush();

        return proxySocket;

    }

    private Socket convertToSocket5(InputStream inputStream, OutputStream outputStream) throws IOException {

        Socket proxySocket = null;

        //处理SOCKS5头信息(不支持登录)

        byte[] tmp = new byte[2];

        inputStream.read(tmp);

        byte method = tmp[1];

        if (0x02 == tmp[0]) {

            method = 0x00;

            inputStream.read();

        }

        tmp = new byte[]{0x05, method};

        outputStream.write(tmp);

        outputStream.flush();

        byte cmd = 0;

        tmp = new byte[4];

        inputStream.read(tmp);

        log("proxy header is:" + Arrays.toString(tmp));

        cmd = tmp[1];

        String host = getHost(tmp[3], inputStream);

        tmp = new byte[2];

        inputStream.read(tmp);

        int port = ByteBuffer.wrap(tmp).asShortBuffer().get() & 0xFFFF;

        log("connect host: " + host + " :port:" + port);

        ByteBuffer rsv = ByteBuffer.allocate(10);

        rsv.put((byte) 0x05);

        Object resultTmp = null;

        try {

            if (0x01 == cmd) {

                resultTmp = new Socket(host, port);

                rsv.put((byte) 0x00);

            } else if (0x02 == cmd) {

                resultTmp = new ServerSocket(port);

                rsv.put((byte) 0x00);

            } else {

                rsv.put((byte) 0x05);

                resultTmp = null;

            }

        } catch (Exception e) {

            rsv.put((byte) 0x05);

            resultTmp = null;

        }

        rsv.put((byte) 0x00);

        rsv.put((byte) 0x01);

        rsv.put(sourceSocket.getLocalAddress().getAddress());

        Short localPort = (short) ((sourceSocket.getLocalPort()) & 0xFFFF);

        rsv.putShort(localPort);

        tmp = rsv.array();

        outputStream.write(tmp);

        outputStream.flush();

        if (null != resultTmp && 0x02 == cmd) {

            ServerSocket ss = (ServerSocket) resultTmp;

            try {

                resultTmp = ss.accept();

            } catch (Exception e) {

            } finally {

                closeIO(ss);

            }

        }

        return (Socket) resultTmp;

    }

    private void transfer(InputStream in, OutputStream out, CountDownLatch latch) {

        new Thread(() -> {

            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];

            int count = 0;

            try {

                while (0 < (count = in.read(bytes))) {

                    out.write(bytes, 0, count);

                    out.flush();

                }

            } catch (IOException e) {

                log("转换出现错误");

                e.printStackTrace();

            }

            if (latch != null) {

                latch.countDown();

            }

        }).start();

    }

    private void closeIO(Closeable closeable) {

        if (closeable != null) {

            try {

                closeable.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

    }

    private String getHost(byte type, InputStream inputStream) throws IOException {

        String host = null;

        byte[] tmp = null;

        switch (type) {

            case TYPE_IPV4:

                tmp = new byte[4];

                inputStream.read(tmp);

                host = InetAddress.getByAddress(tmp).getHostAddress();

                break;

            case TYPE_IPV6:

                tmp = new byte[16];

                inputStream.read(tmp);

                host = InetAddress.getByAddress(tmp).getHostAddress();

                break;

            case TYPE_HOST:

                int count = inputStream.read();

                tmp = new byte[count];

                inputStream.read(tmp);

                host = new String(tmp);

            default:

                break;

        }

        return host;

    }

    public static void main(String[] args) {

        java.security.Security.setProperty("networkaddress.cache.ttl", "86400");

        MySocket5Server.startServer(8081);

    }

}