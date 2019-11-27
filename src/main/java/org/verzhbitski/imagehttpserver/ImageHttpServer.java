package org.verzhbitski.imagehttpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.*;

public class ImageHttpServer {

    private int port;

    private boolean stopped = true;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);
    private ExecutorCompletionService<SocketChannel> channels = new ExecutorCompletionService<>(executorService);

    public ImageHttpServer(int port) {
        this.port = port;
    }

    private void registerChannels(Selector selector) throws IOException, InterruptedException {
        long now = System.currentTimeMillis();
        Future<SocketChannel> alive;
        while ((alive = channels.poll()) != null) {
            try {
                SocketChannel channel = alive.get();
                if (channel != null) {
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ, now);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private long cleanupChannels(long lastCleanup, Set<SelectionKey> keys) throws IOException {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > 5000) {
            for (SelectionKey key: keys)
                if ((key.interestOps() & SelectionKey.OP_READ) != 0 &&
                        ((Long)key.attachment()) + 20_000 < now) {
                    key.channel().close();
                }
            return now;
        }
        else
            return lastCleanup;
    }

    private void execute(SocketChannel clientChannel) {
        channels.submit(() -> {
            Socket client = clientChannel.socket();
            try {
                SocketHandler socketHandler = new SocketHandler(client);

                boolean keepAlive = socketHandler.handle();

                if (keepAlive)
                    return clientChannel;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!client.isClosed())
                client.close();

            return null;
        });
    }

    public void start() throws IOException, InterruptedException {
        stopped = false;

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        ServerSocket socket = serverSocketChannel.socket();
        socket.bind(new InetSocketAddress(port));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        long lastCleaned = System.currentTimeMillis();
        while (!stopped) {
            registerChannels(selector);
            lastCleaned = cleanupChannels(lastCleaned, selector.keys());

            if (selector.select(20) == 0) continue;

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    clientChannel.configureBlocking(true);
                    execute(clientChannel);

                    key.cancel();
                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    key.cancel();
                    clientChannel.configureBlocking(true);
                    execute(clientChannel);
                }
            }

            selector.selectNow();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
    }

    public void stop() {
        stopped = true;
    }
}
