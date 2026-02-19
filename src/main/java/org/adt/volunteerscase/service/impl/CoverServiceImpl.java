package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverPatchResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.exception.CoverNotFoundException;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.service.CoverService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoverServiceImpl implements CoverService {

    private final CoverRepository coverRepository;

    @Override
    public void coverCreateRequest(CoverCreateRequest request) {
        CoverEntity coverEntity = CoverEntity.builder()
                .link(request.getLink())
                .width(request.getWidth())
                .height(request.getHeight()).build();
        coverRepository.save(coverEntity);
    }

    @Override
    public CoverPatchResponse updateCover(CoverPatchRequest request, Integer coverId) {
        CoverEntity coverEntity = coverRepository.findByCoverId(coverId)
                .orElseThrow(() -> new CoverNotFoundException("cover with id - " + coverId + " not found"));

        if (request.getLink() != null) {
            coverEntity.setLink(request.getLink());
        }

        if (request.getHeight() != null) {
            coverEntity.setHeight(request.getHeight());
        }

        if (request.getWidth() != null) {
            coverEntity.setWidth(request.getWidth());
        }

        return convertToResponse(coverRepository.save(coverEntity));

    }

    @Override
    public void deleteCoverById(Integer coverId) {
        CoverEntity coverEntity = coverRepository.findByCoverId(coverId)
                .orElseThrow(() -> new CoverNotFoundException("cover with id - " + coverId + " not found"));

        coverRepository.delete(coverEntity);
    }

    private CoverPatchResponse convertToResponse(CoverEntity coverEntity) {
        return CoverPatchResponse.builder()
                .coverId(coverEntity.getCoverId())
                .link(coverEntity.getLink())
                .height(coverEntity.getHeight())
                .width(coverEntity.getWidth())
                .build();
    }
}
