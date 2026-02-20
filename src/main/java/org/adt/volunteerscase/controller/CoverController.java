package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.service.CoverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cover")
@RequiredArgsConstructor
public class CoverController {

    private final CoverService coverService;

    @Operation(
            summary = "эндпоинт для создания обложки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно создано"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createCover(@Valid @RequestBody CoverCreateRequest request){
        coverService.coverCreateRequest(request);
        return ResponseEntity.ok().build();
    }
}
