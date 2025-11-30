package com.samyookgoo.palgoosam.auction.controller;

import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionCreateApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionDeleteApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionDetailApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionSearchApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionUpdateApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.AuctionUpdatePageApi;
import com.samyookgoo.palgoosam.auction.api_docs.auction.RelatedAuctionGetApi;
import com.samyookgoo.palgoosam.auction.dto.request.AuctionCreateRequest;
import com.samyookgoo.palgoosam.auction.dto.request.AuctionSearchRequestDto;
import com.samyookgoo.palgoosam.auction.dto.request.AuctionUpdateRequest;
import com.samyookgoo.palgoosam.auction.dto.response.AuctionCreateResponse;
import com.samyookgoo.palgoosam.auction.dto.response.AuctionDetailResponse;
import com.samyookgoo.palgoosam.auction.dto.response.AuctionSearchResponseDto;
import com.samyookgoo.palgoosam.auction.dto.response.AuctionUpdatePageResponse;
import com.samyookgoo.palgoosam.auction.dto.response.AuctionUpdateResponse;
import com.samyookgoo.palgoosam.auction.dto.response.RelatedAuctionResponse;
import com.samyookgoo.palgoosam.auction.file.FileStore;
import com.samyookgoo.palgoosam.auction.file.ResultFileStore;
import com.samyookgoo.palgoosam.auction.service.AuctionService;
import com.samyookgoo.palgoosam.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auctions")
@Tag(name = "경매", description = "경매 상품 관련 API")
public class AuctionController {

    private final AuctionService auctionService;
    private final FileStore fileStore;

    @AuctionSearchApi
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<AuctionSearchResponseDto>> search(
            @Valid AuctionSearchRequestDto auctionSearchRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success("정상적으로 조회되었습니다.",
                auctionService.search(auctionSearchRequestDto)));
    }

    @AuctionCreateApi
    @PostMapping
    public ResponseEntity<BaseResponse<AuctionCreateResponse>> createAuction(
            @RequestPart("request") @Valid AuctionCreateRequest request,
            @RequestPart("images") List<MultipartFile> images) {

        if (images == null || images.size() < 1 || images.size() > 10) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("이미지는 최소 1개, 최대 10개까지 업로드 가능합니다.", null));
        }

        List<ResultFileStore> storedImages = fileStore.storeFiles(images);
        AuctionCreateResponse response = auctionService.createAuction(request, storedImages);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("경매 상품 등록 성공", response));
    }

    @AuctionDetailApi
    @GetMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<AuctionDetailResponse>> getAuction(
            @Parameter(name = "auctionId", description = "상세 정보를 조회할 경매 상품 ID", required = true)
            @PathVariable long auctionId
    ) {
        AuctionDetailResponse response = auctionService.getAuctionDetail(auctionId);
        return ResponseEntity.ok(
                BaseResponse.success("경매 상품 상세 조회 성공", response)
        );
    }

    @AuctionUpdatePageApi
    @GetMapping("/{auctionId}/update")
    public ResponseEntity<BaseResponse<AuctionUpdatePageResponse>> getAuctionUpdatePage(
            @Parameter(name = "auctionId", description = "수정할 경매 상품 ID", required = true)
            @PathVariable long auctionId
    ) {
        AuctionUpdatePageResponse response = auctionService.getAuctionUpdate(auctionId);
        return ResponseEntity.ok(BaseResponse.success("경매 상품 수정페이지 조회 성공", response));
    }

    @AuctionUpdateApi
    @PatchMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<AuctionUpdateResponse>> updateAuction(
            @Parameter(name = "auctionId", description = "수정할 경매 상품 ID", required = true)
            @PathVariable Long auctionId,
            @RequestPart(value = "request", required = false) AuctionUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        AuctionUpdateResponse updated = auctionService.updateAuction(auctionId, request, images);
        return ResponseEntity.ok(
                BaseResponse.success("경매 상품 수정 성공", updated)
        );
    }

    @AuctionDeleteApi
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<BaseResponse<Void>> deleteAuction(
            @Parameter(name = "auctionId", description = "삭제할 경매 상품 ID", required = true)
            @PathVariable long auctionId
    ) {
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.ok(BaseResponse.success("경매 상품 삭제 성공", null));
    }

    @RelatedAuctionGetApi
    @GetMapping("/{auctionId}/related")
    public ResponseEntity<BaseResponse<List<RelatedAuctionResponse>>> getRelatedAuctions(
            @Parameter(name = "auctionId", description = "연관 상품을 조회할 기준 경매 ID", required = true)
            @PathVariable Long auctionId
    ) {
        List<RelatedAuctionResponse> related = auctionService.getRelatedAuctions(auctionId);
        return ResponseEntity.ok(BaseResponse.success("연관 경매 상품 조회 성공", related));
    }
}
