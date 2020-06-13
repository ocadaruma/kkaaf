//package com.mayreh.toyka;
//
//import java.io.IOException;
//import java.io.UncheckedIOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class NioSocketServer extends Thread implements AutoCloseable {
//    private static class ChannelBuffer {
//        private final ByteBuffer readBuffer = ByteBuffer.allocate(4);
//        private final ByteBuffer writeBuffer = ByteBuffer.wrap(new byte[] {'p', 'o', 'n', 'g'});
//        private boolean writing;
//    }
//
//    private static final int BUFFER_SIZE = 1024;
//    private static final long SELECT_TIMEOUT_MILLIS = 500L;
//    private static final AtomicInteger sequence = new AtomicInteger(0);
//
//    private final InetSocketAddress address;
//    private Selector selector;
//    private volatile boolean terminated;
//
//    public NioSocketServer(String host, int port) {
//        setName("nio-socket-server-" + sequence.getAndIncrement());
//
//        address = new InetSocketAddress(host, port);
//    }
//
//    @Override
//    public void run() {
//        try {
//            selector = Selector.open();
//            ServerSocketChannel.open()
//                               .bind(address)
//                               .configureBlocking(false)
//                               .register(selector, SelectionKey.OP_ACCEPT);
//
//            while (!terminated) {
//                if (selector.select(SELECT_TIMEOUT_MILLIS) < 1) {
//                    continue;
//                }
//                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
//                while (iter.hasNext()) {
//                    SelectionKey key = iter.next();
//                    iter.remove();
//
//                    if (key.isAcceptable()) {
//                        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
//                        socketChannel.configureBlocking(false)
//                                     .register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE)
//                                     .attach(new ChannelBuffer());
//                    }
//
//                    if (key.isReadable()) {
//                        ChannelBuffer buffer = (ChannelBuffer) key.attachment();
//                        if (!buffer.writing) {
//                            ((SocketChannel) key.channel()).read(buffer.readBuffer);
//                            System.out.println(Arrays.toString(buffer.readBuffer.array()));
//                            if (!buffer.readBuffer.hasRemaining()) {
//                                buffer.readBuffer.rewind();
//                                if ("ping".equals(new String(buffer.readBuffer.array()))) {
//                                    buffer.writing = true;
//                                    buffer.writeBuffer.rewind();
//                                }
//                            }
//                        }
//                    }
//                    if (key.isWritable()) {
//                        ChannelBuffer buffer = (ChannelBuffer) key.attachment();
//                        if (buffer.writing) {
//                            ((SocketChannel) key.channel()).write(buffer.writeBuffer);
//                            if (!buffer.writeBuffer.hasRemaining()) {
//                                buffer.writing = false;
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//        terminated = true;
//        selector.close();
//    }
//}
