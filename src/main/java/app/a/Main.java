package app.a;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.createContext("/test", handle());
            server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static HttpHandler handle() {
        return httpExchange -> {
            Observable<String> callB = asyncCall("http://localhost:8090/test").subscribeOn(Schedulers.computation());
            Observable<String> callC = asyncCall("http://localhost:80/test").subscribeOn(Schedulers.computation());
            
            Observable
                    .zip(callB, callC, String::concat)
                    .subscribe(result -> {
                                httpExchange.sendResponseHeaders(200, 0);
                                OutputStream outputStream = httpExchange.getResponseBody();
                                outputStream.write(result.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            }
                    );
        };
    }

    private static Observable<String> asyncCall(String url) {
        return Observable.fromCallable(
                () -> {
                    URI uri = new URI(url);
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(uri)
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return response.body();
                }
        );
    }
}
