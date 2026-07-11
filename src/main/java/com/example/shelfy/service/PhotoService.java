package com.example.shelfy.service;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    private final S3Client r2Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    public PhotoService(S3Client r2Client) {
        this.r2Client = r2Client;
    }

    public String upload(MultipartFile file, Long itemId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                .size(800, 800)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(baos);

            String key = "items/" + itemId + "/" + UUID.randomUUID() + ".jpg";

            r2Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build(),
                RequestBody.fromBytes(baos.toByteArray())
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            log.error("写真アップロード失敗: {}", e.getMessage());
            throw new RuntimeException("写真のアップロードに失敗しました");
        }
    }

    public void delete(String imageUrl) {
        try {
            String key = imageUrl.replace(publicUrl + "/", "");
            r2Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        } catch (Exception e) {
            log.error("写真削除失敗: {}", e.getMessage());
        }
    }
}
