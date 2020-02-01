package org.streaming.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class MyController {

    @PostMapping(path = "/receive", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity receiveData(final InputStream data) {
        System.out.println("start streaming");
        try {
            int c;
            StringBuilder builder = new StringBuilder();
            while ((c = data.read()) != -1) {
                builder.append((char)c);
            }
            System.out.println("Bytes received " + builder.length());
        } catch(IOException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        System.out.println("end streaming");
        return ResponseEntity.ok().build();
    }
}
