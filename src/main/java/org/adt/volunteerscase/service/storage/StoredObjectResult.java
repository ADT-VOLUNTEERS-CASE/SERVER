package org.adt.volunteerscase.service.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoredObjectResult {
    private final String bucket;
    private final String objectKey;
    private final String link;
    private final String eTag;
}
