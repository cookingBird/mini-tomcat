package test;

import server.*;

import java.io.IOException;

public class HelloServlet implements Servlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String doc =
                "<!DOCTYPE html> \n"
                        + "<html>\n"
                        + "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                        + "<body bgcolor=\"#f0f0f0\">\n"
                        + "<h1 align=\"center\">"
                        + "Hello World 你好"
                        + "</h1>\n";
        response.getOutput().write(doc.getBytes("UTF-8"));
    }
}
