package com.xd.pre.socekt.s2;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.xd.pre.modules.px.appstorePc.pcScan.Fuzhu;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author yx
 * @date 2020/9/12  18:55
 * Description  本类为tcp代理工具类BIO版本，可以直接用本机的某个端口代理他可以访问到的任意一台主机的任意端口，因为连接会自动超时，
 * * 程序会抛出异常属于正常现象，再次请求会 自动连接，本次测试以代理mysql端口和sftp端口为例
 */
@Slf4j
public class NIOProxy extends Thread {
    ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 1000);
    //用于监听key的请求
    Selector selector;
    InetSocketAddress remote;

    public static void main(String[] args) throws IOException {
        String remoteAddr = "192.168.2.149";
        int remotePort = 8080;
        int localPort = 8081;
        new NIOProxy(localPort, remoteAddr, remotePort).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            Acceptable(key);
                        }
                        //在这个分类中不会涉及到所谓的写key的方法，所以注销了
//                        if (key.isWritable()) {
//                            WriteData(key);
//                        }
                        if (key.isReadable()) {
                            ReadData(key);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public NIOProxy(int localPort, String remoteAddr, int remotePort) {
//        System.out.println("代理为本地端口：" + localPort + "代理" + remoteAddr + "地址的" + remotePort + "端口");
        try {
            selector = Selector.open();
            remote = new InetSocketAddress(remoteAddr, remotePort);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            SocketAddress sockerAddress = new InetSocketAddress(localPort);
            serverSocketChannel.bind(sockerAddress);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Acceptable(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            //用于监听服务端的连接请求的key
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            SocketChannel serverChannel = SocketChannel.open();
            serverChannel.configureBlocking(false);
            // 连接远程服务器。
            serverChannel.connect(remote);
            //这一句不能少，因为请求了连接如果要注入到select中去读取数据需要手动完成连接
            serverChannel.finishConnect();
            clientChannel.register(selector, SelectionKey.OP_READ, serverChannel);
            serverChannel.register(selector, SelectionKey.OP_READ, clientChannel);
        } catch (Exception e) {
//            System.out.println("连接异常");
        }
    }

    /**
     * 不要考虑所谓的读写分方法了，转发的原理就是直接从一个通道里获取输入流然后直接写入到另外一个通道就行了，没必要分方法
     *
     * @param key
     */
    private void ReadData(SelectionKey key) throws IOException {
        SocketChannel otherChannel = (SocketChannel) key.attachment();
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            //因为存在连接可能还没有来得及连接成功就运行到这里所以为了处理报错手动判断一次是否连接成功,判断是否完成连接，
            // 如果没有完成就重新发起 连接请求，直到连接成功才进行下一步,这两个循环的代码主要是为了处理http请求和https请求的，
            // 因为都是无状态协议，而且http请求是长连接,不能确定客户端与服务端什么时候会真正断开并且释放连接（有时候断开了服务端也不一定会立即释放连接）,
            // 如果查询到客户端断开了捕获到异常会把服务端也一块断开，不过这么处理的话http的请求有可能会长期无法释放占用资源，不是一个好的处理方式，
            // 而且http请求比较频繁，每次发送一个次数据都会重新建立连接发送请求，会把大量资源用于创建连接上，基于tcp端口的代理应该把更多的资源用于数据转发上，
            // 因此不建议大家用来代理无状态的http协议，具体怎么使用请大家自行斟酌修改，
            // 关于重试连接的次数以及重试的间隔根据自己的环境及网络延迟调整
            int resetSocket = 0;
            while (true) {
                if (socketChannel.isConnected() || resetSocket > 10) {
                    break;
                }
                System.out.println("请求完成连接");
                socketChannel.finishConnect();
                Thread.sleep(10);
            }
            int reset = 0;
            while (true) {
                if (otherChannel.isConnected() || reset > 10) {
                    break;
                }
                System.out.println("请求完成连接");
                otherChannel.finishConnect();
                Thread.sleep(10);
            }
            readBuffer.clear();
            int read = socketChannel.read(readBuffer);
            if (read == -1) {
                key.channel().close();
                key.cancel();
                otherChannel.close();
                return;
            }
            readBuffer.flip();
            byte[] datas = new byte[readBuffer.remaining()];
            readBuffer.get(datas);
            if (new String(datas).contains("room")) {
//                datas = uncompress(datas);
                log.info("创建房间接口:{}");
            }
            if (datas.length > 10) {
//                System.out.println("=============================");
                List<Integer> xxxList = Arrays.stream(Fuzhu.byte2Int(datas)).boxed().collect(Collectors.toList());
                System.out.println(JSON.toJSONString(xxxList));
                List<Integer> paixing = new ArrayList<>();
                for (int i = 0; i < xxxList.size(); i++) {
                    if (i > 3 && xxxList.get(i) >= 11 && xxxList.get(i) <= 39 && xxxList.get(i - 1) == 0 && xxxList.get(i - 2) == 0 && xxxList.get(i - 3) == 0) {
                        System.out.println(xxxList.get(i));
                        paixing.add(xxxList.get(i));
                    }
                }
                paixing = paixing.stream().sorted().collect(Collectors.toList());
                if(CollUtil.isNotEmpty(paixing)){
                    System.out.println("排序值++++++++++++++++"+JSON.toJSONString(paixing));
                }
                if (xxxList.get(0) == -128 && xxxList.get(1) == 0 && xxxList.get(2) == 79 && xxxList.get(3) == 18) {
                    Integer data = xxxList.get(51);
                    if (data >= 11 && data <= 19) {
                        log.info("当前打的牌是万:{}", data - 10);
                    }
                    if (data >= 21 && data <= 29) {
                        log.info("当前打的牌是条:{}", data - 20);
                    }
                    if (data >= 31 && data <= 39) {
                        log.info("当前打的牌是筒:{}", data - 30);
                    }
                }
                if (xxxList.get(0) == -128 && xxxList.get(1) == 0 && xxxList.get(2) == -24 && xxxList.get(3) == 18 && xxxList.get(4) == 0 && xxxList.get(5) == 3
                        && xxxList.size() == 235) {
                    System.out.println(xxxList.size());
                    Integer data = xxxList.get(101);
                    if (data >= 11 && data <= 19) {
                        log.info("对面打的牌是万:{}", data - 10);
                    }
                    if (data >= 21 && data <= 29) {
                        log.info("当前打的牌是条:{}", data - 20);
                    }
                    if (data >= 31 && data <= 39) {
                        log.info("当前打的牌是筒:{}", data - 30);
                    }
                }
                if (xxxList.get(0) == -128 && xxxList.get(1) == 0 && xxxList.get(2) == -24 && xxxList.get(3) == 18 && xxxList.get(4) == 0 && xxxList.get(5) == 3
                        && xxxList.size() == 235) {
                    System.out.println(xxxList.size());
                    Integer data = xxxList.get(162);
                    if (data >= 11 && data <= 19) {
                        log.info("自己摸的牌是万:{}", data - 10);
                    }
                    if (data >= 21 && data <= 29) {
                        log.info("自己摸的牌是条:{}", data - 20);
                    }
                    if (data >= 31 && data <= 39) {
                        log.info("自己摸的牌是筒:{}", data - 30);
                    }
                }
                if (xxxList.get(0) == -128 && xxxList.get(1) == 0 && xxxList.get(2) == -113 && xxxList.get(3) == 18 && xxxList.get(4) == 0 && xxxList.get(5) == 3
                        && xxxList.size() >= 500) {
                    System.out.println(xxxList.size());
                  /*  List<Integer> paixing = new ArrayList<>();
                    for (int i = 0; i < xxxList.size(); i++) {
                        if (i > 3 && xxxList.get(i) >= 11 && xxxList.get(i) <= 39 && xxxList.get(i - 1) == 0 && xxxList.get(i - 2) == 0 && xxxList.get(i - 3) == 0) {
                            System.out.println(xxxList.get(i));
                            paixing.add(xxxList.get(i));
                        }
                    }*/
                    paixing = paixing.stream().sorted().collect(Collectors.toList());
                    System.out.println(JSON.toJSONString(paixing));
                    Integer data = xxxList.get(248);
                    if (data >= 11 && data <= 19) {
                        log.info("自己摸的牌是万:{}", data - 10);
                    }
                    if (data >= 21 && data <= 29) {
                        log.info("自己摸的牌是条:{}", data - 20);
                    }
                    if (data >= 31 && data <= 39) {
                        log.info("自己摸的牌是筒:{}", data - 30);
                    }
                }
                String dataStr = new String(datas);
                if(dataStr.contains("st") && dataStr.contains("cds") && dataStr.contains("uid") && dataStr.contains("wt")){
                    log.info("赢了");
//                    key.channel().close();
//                    key.cancel();
                }
                System.out.println("=========收到消息:" + new String(datas));
            }
            readBuffer.clear();
            readBuffer.put(datas);
            readBuffer.flip();
            otherChannel.write(readBuffer);
            // 当读取完数据后重新注册一个读取的key到selector中继续等待读操作。
            socketChannel.register(selector, SelectionKey.OP_READ, otherChannel);
        } catch (Exception e) {
            key.channel().close();
            key.cancel();
            otherChannel.close();
            e.printStackTrace();
        }
    }

    /**
     * 解压返回字符串
     *
     * @param bytes 待解压byte数组
     * @param encoding 编码
     * @return
     */
    private static final int BYTE_LEN = 1024;
    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";


    /**
     * 解压
     *
     * @param bytes 待解压byte数组
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[BYTE_LEN];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {

        }
        return null;
    }

}

