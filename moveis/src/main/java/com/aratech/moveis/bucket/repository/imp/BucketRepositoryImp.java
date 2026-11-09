package com.aratech.moveis.bucket.repository.imp;

import com.aratech.moveis.bucket.model.BucketFile;
import com.aratech.moveis.bucket.repository.BucketRepository;
import com.aratech.moveis.config.props.MinioProps;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class BucketRepositoryImp implements BucketRepository {

    private final MinioClient client;
    private final MinioProps props;

    public void upload(BucketFile file) {
        try {
            var object =
                    PutObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(file.name())
                            .stream(file.inputStream(), file.size(), -1)
                            .contentType(file.type().toString())
                            .headers(Map.of("Content-Disposition", "attachment"))
                            .build();

            client.putObject(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl(String fileName) {
        try {
            var object =
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(props.getBucketName())
                            .object(fileName)
                            .expiry(1, TimeUnit.HOURS)
                            .build();

            return client.getPresignedObjectUrl(object);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(String objectName) {
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível remover o documento.", e);
        }
    }
}
