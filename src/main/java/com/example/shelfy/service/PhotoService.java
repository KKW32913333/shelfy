package com.example.shelfy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
            byte[] imageBytes = resizeImage(file.getInputStream(), file.getContentType());

            String key = "items/" + itemId + "/" + UUID.randomUUID() + ".jpg";

            r2Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build(),
                RequestBody.fromBytes(imageBytes)
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            log.error("写真アップロード失敗: {}", e.getMessage());
            throw new RuntimeException("写真のアップロードに失敗しました: " + e.getMessage());
        }
    }

    private byte[] resizeImage(InputStream inputStream, String contentType) throws Exception {
        BufferedImage original = ImageIO.read(inputStream);

        if (original == null) {
            throw new RuntimeException("画像の読み込みに失敗しました。JPEG/PNG形式でお試しください。");
        }

        int maxSize = 800;
        int width  = original.getWidth();
        int height = original.getHeight();

        if (width > maxSize || height > maxSize) {
            double scale = Math.min((double) maxSize / width, (double) maxSize / height);
            width  = (int) (width  * scale);
            height = (int) (height * scale);
        }

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        return baos.toByteArray();
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
