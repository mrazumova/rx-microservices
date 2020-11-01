package app.c;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger("C");

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(80), 0);
            server.createContext("/test",
                    httpExchange -> {
                        logger.info("Incoming request..." + httpExchange.getRemoteAddress() + httpExchange.getRequestURI().toString());

                        OutputStream outputStream = httpExchange.getResponseBody();

                        String response = "--- Hello from C --- \n";

                        httpExchange.sendResponseHeaders(200, response.length());

                        outputStream.write(response.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        logger.info("Response OK");
                    });
            server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}