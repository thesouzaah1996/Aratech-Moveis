package com.aratech.moveis.bucket.service;

import com.aratech.moveis.bucket.model.BucketFile;

public interface BucketService {

    void upload(BucketFile file);

    String getUrl(String fileName);

    void remove(String objectName);
}
