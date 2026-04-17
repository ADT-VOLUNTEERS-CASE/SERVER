package org.adt.volunteerscase.service.storage;

public interface ObjectStorageService {
    StoredObjectResult uploadCover(String originalFileName, String contentType, byte[] content);

    void deleteObject(String objectKey);
}
