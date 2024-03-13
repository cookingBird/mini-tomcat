package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

public class HttpProcessor implements Runnable {
    Socket socket = null;
    boolean socketAvailable = false;
    HttpConnector connector = null;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(Socket socket) {
        System.out.println("start: " + new Date());
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("end: " + new Date());
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            Request request = new Request(inputStream);
            request.parse();
            Response response = new Response(outputStream);
            response.setRequest(request);
            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            }
            else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            Socket socket = this.await();
            if (socket == null) continue;
            this.process(socket);
            this.connector.recycle(this);
        }

    }

    public synchronized void assign(Socket socket) {
        if (socketAvailable) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.socket = socket;
        this.socketAvailable = true;
        notifyAll();
    }

    private synchronized Socket await() {
        if (!socketAvailable) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Socket socket = this.socket;
        this.socketAvailable = false;
        notifyAll();
        return socket;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
