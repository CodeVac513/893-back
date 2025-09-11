//package com.samyookgoo.palgoosam.common.s3;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/s3")
//public class S3Controller {
//    private final S3Service s3Service;
//
//    @PostMapping("/presigned-url")
//    public ResponseEntity<S3PresignedResponse> generateUploadUrl(
//            @RequestBody S3PresignedRequest requestDto) {
//        return ResponseEntity.ok(s3Service.createPresignedUrl(requestDto));
//    }
//}
