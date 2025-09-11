//package com.samyookgoo.palgoosam.common.s3;
//
//import com.samyookgoo.palgoosam.auction.exception.AuctionImageException;
//import com.samyookgoo.palgoosam.global.exception.ErrorCode;
//import java.time.Duration;
//import java.util.UUID;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.S3Exception;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
//import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class S3Service {
//    private final S3Presigner s3Presigner;
//    private final S3Client s3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;
//
//    public S3PresignedResponse createPresignedUrl(S3PresignedRequest request) {
//        try {
//            String originalName = request.getFileName();
//            String extension = originalName.substring(originalName.lastIndexOf("."));
//            String storeFileName = UUID.randomUUID() + extension;
//
//            String publicUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + storeFileName;
//
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(storeFileName)
//                    .contentType(request.getContentType())
//                    .build();
//
//            // Presigned URL 생성
//            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
//                    .signatureDuration(Duration.ofMinutes(10))
//                    .putObjectRequest(putObjectRequest)
//                    .build();
//
//            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
//
//            return S3PresignedResponse.builder()
//                    .presignedUrl(presignedRequest.url().toString())
//                    .storeName(storeFileName)
//                    .publicUrl(publicUrl)
//                    .build();
//
//        } catch (S3Exception e) {
//            throw new RuntimeException("S3 URL 생성 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    public void deleteObject(String key) {
//        try {
//            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(key)
//                    .build();
//
//            s3Client.deleteObject(deleteRequest);
//
//            log.info("S3 객체 삭제 성공 - key: {}", key);
//        } catch (Exception e) {
//            log.error("S3 객체 삭제 실패 - key: {}", key, e);
//            throw new AuctionImageException(ErrorCode.AUCTION_IMAGE_DELETE_FAILED);
//        }
//    }
//
//}
