package com.aratech.moveis.bucket.service.imp;

import com.aratech.moveis.bucket.model.BucketFile;
import com.aratech.moveis.bucket.repository.BucketRepository;
import com.aratech.moveis.bucket.service.BucketService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BucketServiceImp implements BucketService {
    private final BucketRepository repository;

    @Override
    public void upload(BucketFile file) {
        repository.upload(file);
    }

    @Override
    public String getUrl(String fileName) {
        return repository.getUrl(fileName);
    }

    @Override
    public void remove(String objectName) {
        repository.remove(objectName);
    }
}
