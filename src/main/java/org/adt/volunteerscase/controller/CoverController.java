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
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;
import org.adt.volunteerscase.service.CoverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("api/v1/cover")
@RequiredArgsConstructor
public class CoverController {

    private final CoverService coverService;

    @Operation(
            summary = "загрузка новой обложки",
            responses = {
                    @ApiResponse(responseCode = "201", description = "успешно создано"),
                    @ApiResponse(responseCode = "400", description = "невалидный файл", content
                            = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "413", description = "файл слишком большой для загрузки", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "ошибка загрузки файла в s3", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CoverResponse> createCover(@Valid @ModelAttribute CoverCreateRequest
                                                             request) {
        return
                ResponseEntity.status(HttpStatus.CREATED).body(coverService.createCover(request));
    }

    @Operation(
            summary = "замена файла обложки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно обновлено"),
                    @ApiResponse(responseCode = "404", description = "обложка с таким id не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "невалидный файл", content
                            = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping(value = "/{coverId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CoverResponse> updateCover(
            @PathVariable Integer coverId,
            @Valid @ModelAttribute CoverPatchRequest request
    ) {
        return ResponseEntity.ok(coverService.updateCover(request, coverId));
    }

    @Operation(
            summary = "мягкое удаление обложки"
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{coverId}")
    public ResponseEntity<Void> deleteCoverById(@PathVariable Integer coverId) {
        coverService.deleteCoverById(coverId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "получение обложки по id",
            responses = {
                    @ApiResponse(responseCode = "404", description = "обложка с таким id не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "200", description = "успешно")
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{coverId}")
    public ResponseEntity<CoverResponse> getCoverById(@PathVariable Integer coverId) {
        return ResponseEntity.ok(coverService.getCoverById(coverId));
    }
}
