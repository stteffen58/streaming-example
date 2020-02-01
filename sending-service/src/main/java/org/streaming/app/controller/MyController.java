package org.streaming.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.streaming.app.service.StreamingService;

@RestController
public class MyController {

    @Autowired
    private StreamingService service;

    @GetMapping(path = "/send")
    public ResponseEntity sendData() {
        service.sendData();
        return ResponseEntity.ok().build();
    }
}
