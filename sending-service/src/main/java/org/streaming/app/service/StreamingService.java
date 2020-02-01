package org.streaming.app.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.Random;

/**
 * Service which creates 500 bytes with a constant rate every 500ms. Meanwhile data is directly streamed to another
 * endpoint using {@link RestTemplate}.
 */
@Service
public class StreamingService {

    private final RestTemplate restTemplate;
    private byte[] data;
    private final PipedOutputStream pipedOutputStream;
    private final PipedInputStream pipedInputStream;

    private static final int size = 5000;
    private static final int maxBytes = 1000000;

    public StreamingService() throws IOException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false); // import !!! without this option, the request buffers the data
        restTemplate = new RestTemplate(requestFactory);
        data = new byte[size];
        pipedOutputStream = new PipedOutputStream();
        try {
            pipedInputStream = new PipedInputStream(pipedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void simulateDataInput() throws IOException {
        Random rand = new Random();
        rand.nextBytes(data);
        pipedOutputStream.write(data);
        try {
            Thread.sleep(500); // throttle, to simulate slow data creation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendData() {
        Runnable run = () -> {
            int bytesCreated = 0;
            try {
                while (bytesCreated < maxBytes) {
                    simulateDataInput();
                    bytesCreated += size;
                    //System.out.println("Bytes created " + bytesCreated);
                }
                pipedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(run).start();

        RequestCallback requestCallback = (request) -> {
            request.getHeaders().add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            InputStream is = new ByteArrayInputStream(data);
            int c;
            int count = 0;
            OutputStream outputStream = request.getBody();

            while ((c = pipedInputStream.read()) != -1) {
                outputStream.write(c);
                //outputStream.flush();
                count++;
            }
            System.out.println("Bytes sent " + count);
            is.close();
            outputStream.close();
        };

        restTemplate.execute("http://localhost:8080/receive", HttpMethod.POST, requestCallback, null);
    }
}
