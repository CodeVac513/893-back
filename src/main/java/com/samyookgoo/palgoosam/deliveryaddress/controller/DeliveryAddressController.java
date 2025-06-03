package com.samyookgoo.palgoosam.deliveryaddress.controller;

import com.samyookgoo.palgoosam.common.response.BaseResponse;
import com.samyookgoo.palgoosam.deliveryaddress.api_docs.DeleteUserDeliveryAddressApi;
import com.samyookgoo.palgoosam.deliveryaddress.api_docs.GetUserDeliveryAddressesApi;
import com.samyookgoo.palgoosam.deliveryaddress.api_docs.PatchUserDefaultAddress;
import com.samyookgoo.palgoosam.deliveryaddress.api_docs.PostUserDeliveryAddress;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressRequestDto;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressResponseDto;
import com.samyookgoo.palgoosam.deliveryaddress.service.DeliveryAddressService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "배송지", description = "회원 배송지 관리 API")
public class DeliveryAddressController {
    private final DeliveryAddressService deliveryAddressService;

    @GetUserDeliveryAddressesApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/addresses")
    public ResponseEntity<BaseResponse<List<DeliveryAddressResponseDto>>> getUserDeliveryAddresses() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("배송지가 정상적으로 조회되었습니다.", deliveryAddressService.getUserDeliveryAddresses()));
    }

    @DeleteUserDeliveryAddressApi
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @DeleteMapping("addresses/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUserDeliveryAddress(
            @Parameter(name = "id", description = "삭제할 배송지 ID", required = true)
            @PathVariable Long id) {
        deliveryAddressService.deleteUserDeliveryAddress(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("배송지가 삭제되었습니다.", null));
    }

    @PostUserDeliveryAddress
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/addresses")
    public ResponseEntity<BaseResponse<Void>> postUserDeliveryAddress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "배송지 등록 요청 정보", required = true)
            @RequestBody DeliveryAddressRequestDto requestDto) {
        deliveryAddressService.postUserDeliveryAddress(requestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("배송지가 등록되었습니다.", null));
    }

    @PatchUserDefaultAddress
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<BaseResponse<Void>> patchUserDefaultAddress(
            @Parameter(name = "id", description = "기본 설정할 배송지 ID", required = true)
            @PathVariable Long id) {
        deliveryAddressService.patchUserDefaultAddress(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("기본 배송지가 변경됐습니다.", null));
    }
}
