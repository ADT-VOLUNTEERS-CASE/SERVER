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
import org.adt.volunteerscase.service.CoverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/cover")
@RequiredArgsConstructor
public class CoverController {

    private final CoverService coverService;

    @Operation(
            summary = "эндпоинт для создания обложки",
            responses = {
                    @ApiResponse(responseCode = "201", description = "успешно создано"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createCover(@Valid @RequestBody CoverCreateRequest request) {
        coverService.coverCreateRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "обновление полей обложки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно обновлено"),
                    @ApiResponse(responseCode = "404", description = "обложка с таким id не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{coverId}")
    public ResponseEntity<?> updateCover(
            @PathVariable Integer coverId,
            @Valid @RequestBody CoverPatchRequest request
    ) {
        return ResponseEntity.ok().body(coverService.updateCover(request, coverId));
    }

    @Operation(
            summary = "удаление обложки",
            responses = {
                    @ApiResponse(responseCode = "204", description = "успешно удалено"),
                    @ApiResponse(responseCode = "404", description = "обложка с таким id не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{coverId}")
    public ResponseEntity<?> deleteCoverById(
            @PathVariable Integer coverId
    ) {
        coverService.deleteCoverById(coverId);
        return ResponseEntity.noContent().build();
    }
}
