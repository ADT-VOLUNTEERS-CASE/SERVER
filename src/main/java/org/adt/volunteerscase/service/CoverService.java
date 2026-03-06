package org.adt.volunteerscase.service;


import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;

public interface CoverService {
    void coverCreateRequest(CoverCreateRequest request);

    CoverResponse updateCover(CoverPatchRequest request, Integer coverId);

    void deleteCoverById(Integer coverId);

    CoverResponse getCoverById(Integer coverId);
}
