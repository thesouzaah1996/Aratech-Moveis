package com.aratech.moveis.bucket.repository;

import com.aratech.moveis.bucket.model.BucketFile;

public interface BucketRepository {
    void upload(BucketFile file);

    String getUrl(String fileName);

    void remove(String objectName);
}
