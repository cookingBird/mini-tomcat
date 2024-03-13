package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class HttpConnector implements Runnable {
    int minProcessors = 3;
    int maxProcessors = 10;
    int curProcessors = 0;
    Deque processors = new ArrayDeque<>();

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // initialize processors pool
        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor processor = new HttpProcessor(this);
            processor.start();
            processors.push(processor);
        }
        curProcessors = minProcessors;
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                //得到一个新的processor，这个processor从池中获取(池中有可能新建)
                HttpProcessor processor = createProcessor();
                if (processor == null) {
                    socket.close();
                    continue;
                }
                processor.assign(socket);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HttpProcessor createProcessor() {
        synchronized (processors) {
            if (processors.size() > 0) {
                //获取一个
                return ((HttpProcessor) processors.pop());
            }
            if (curProcessors < maxProcessors) {
                //新建一个
                return (newProcessor());
            }
            else {
                return (null);
            }
        }
    }

    private HttpProcessor newProcessor() {
        HttpProcessor initProcessor = new HttpProcessor(this);
        initProcessor.start();
        processors.push(initProcessor);
        curProcessors++;
        return ((HttpProcessor) processors.pop());
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void recycle(HttpProcessor processor) {
        processors.push(processor);
    }
}
