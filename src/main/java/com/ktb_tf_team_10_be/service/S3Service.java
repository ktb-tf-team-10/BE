package com.ktb_tf_team_10_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloudflare.cdn-domain}")
    private String cdnDomain;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * 단일 이미지 업로드
     * @param file 업로드할 파일
     * @param folder S3 폴더 경로 (예: "wedding-images", "style-images")
     * @return S3 URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 고유 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String key = folder + "/" + UUID.randomUUID() + extension;

            // S3 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .cacheControl("public, max-age=31536000")
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String cloudflareUrl = toCloudflareUrl(key);

            log.info("[S3] Uploaded image → {}", cloudflareUrl);

            return cloudflareUrl;

        } catch (IOException e) {
            log.error("[S3] Failed to upload image: {}", e.getMessage());
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 다중 이미지 업로드
     * @param files 업로드할 파일 리스트
     * @param folder S3 폴더 경로
     * @return S3 URL 리스트
     */
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadImage(file, folder);
            if (url != null) {
                urls.add(url);
            }
        }

        log.info("[S3] Uploaded {} images to folder: {}", urls.size(), folder);
        return urls;
    }

    private String toCloudflareUrl(String key) {
        return "https://" + cdnDomain + "/" + key;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
