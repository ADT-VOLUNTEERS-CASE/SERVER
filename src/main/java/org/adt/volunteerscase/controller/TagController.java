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
import org.adt.volunteerscase.dto.tag.request.TagUpdateRequest;
import org.adt.volunteerscase.dto.tag.response.TagGetResponse;
import org.adt.volunteerscase.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createTag(@Valid @RequestBody TagCreateRequest request) {
        tagService.createTag(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "эндпоинт для обновления полей тега",
            responses = {
                    @ApiResponse(responseCode = "200", description = "поля успешно обновлены"),
                    @ApiResponse(responseCode = "404", description = "тег с таким id не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "тег с таким именем уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{tagId}")
    public ResponseEntity<?> updateTag(
            @Valid @RequestBody TagUpdateRequest request,
            @PathVariable Integer tagId
    ) {
        tagService.updateTag(request, tagId);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "эндпоинт для удаления тега по его id",
            responses = {
                    @ApiResponse(responseCode = "204", description = "тег успешно удалён"),
                    @ApiResponse(responseCode = "404", description = "тег с таким id не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/id/{tagId}")
    public ResponseEntity<?> deleteTagById(
            @PathVariable Integer tagId
    ) {
        tagService.deleteById(tagId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "эндпоинт для удаления тега по его имени",
            responses = {
                    @ApiResponse(responseCode = "204", description = "тег успешно удалён"),
                    @ApiResponse(responseCode = "404", description = "тег с таким именем не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/name/{tagName}")
    public ResponseEntity<?> deleteTagById(
            @PathVariable String tagName
    ) {
        tagService.deleteByName(tagName);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "получение информации о теге по его id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно"),
                    @ApiResponse(responseCode = "404", description = "тег с таким id не найден"),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/id/{tagId}")
    public ResponseEntity<TagGetResponse> getTagById(
            @PathVariable Integer tagId
    ) {
        return ResponseEntity.ok().body(tagService.getById(tagId));
    }

    @Operation(
            summary = "получение информации о теге по его имени",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно"),
                    @ApiResponse(responseCode = "404", description = "тег с таким именем не найден"),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/name/{tagName}")
    public ResponseEntity<TagGetResponse> getTagByName(
            @PathVariable String tagName
    ) {
        return ResponseEntity.ok().body(tagService.getByName(tagName));
    }

}
