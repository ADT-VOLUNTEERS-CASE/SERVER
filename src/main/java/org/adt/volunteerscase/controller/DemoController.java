package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DemoController {
    /**
     * Responds to a GET request with a simple "pong" message.
     *
     * @return a ResponseEntity containing the body "pong" and HTTP status 200 (OK)
     */

    @Operation(
            summary = "ping pong"
    )
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(
            summary = "ping pong with auth"
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/authping")
    public ResponseEntity<String> authping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(
            summary = "ping pong only with role admin"
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/adminping")
    public ResponseEntity<String> authadminping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(
            summary = "ping pong only with role coordinator"
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinatorping")
    public ResponseEntity<String> authcoordinatorping() {
        return ResponseEntity.ok("pong");
    }

}