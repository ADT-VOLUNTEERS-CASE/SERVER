package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/demo")
@SecurityRequirement(name = "jwtAuth")
public class DemoController {

    /**
     * Responds to a GET request with a simple "pong" message.
     *
     * @return a ResponseEntity containing the body "pong" and HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}