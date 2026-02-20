package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.tag.request.TagCreateRequest;
import org.adt.volunteerscase.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "создание тега",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно создано"),
                    @ApiResponse(responseCode = "409", description = "тэг с таким именем уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createTag(@Valid @RequestBody TagCreateRequest request){
        tagService.createTag(request);
        return ResponseEntity.ok().build();
    }
}
