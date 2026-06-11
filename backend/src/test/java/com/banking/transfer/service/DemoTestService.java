package com.banking.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DemoTestService {

    public String hello(String name) {
        log.debug("Internal service debug before return");
        return "Hello, " + name;
    }

    public void fail() {
        throw new RuntimeException("Boom!");
    }
}
