package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.entity.CoverEntity;
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
}
