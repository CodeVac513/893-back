package com.samyookgoo.palgoosam.deliveryaddress.service;

import com.samyookgoo.palgoosam.auth.service.AuthService;
import com.samyookgoo.palgoosam.deliveryaddress.domain.DeliveryAddress;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressRequestDto;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressResponseDto;
import com.samyookgoo.palgoosam.deliveryaddress.exception.DeliveryAddressNotFoundException;
import com.samyookgoo.palgoosam.deliveryaddress.repository.DeliveryAddressRepository;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.exception.UserForbiddenException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<DeliveryAddressResponseDto> getUserDeliveryAddresses(User currentUser) {
        List<DeliveryAddress> deliveryAddressList = deliveryAddressRepository.findAllByUser_Id(currentUser.getId());

        return deliveryAddressList.stream()
                .map(DeliveryAddressResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUserDeliveryAddress(Long deliveryAddressId, User currentUser) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findByUserAndId(currentUser, deliveryAddressId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        if (deliveryAddress.hasPermission(currentUser.getId())) {
            deliveryAddressRepository.deleteById(deliveryAddress.getId());
        } else {
            throw new UserForbiddenException();
        }
    }

    @Transactional
    public void postUserDeliveryAddress(DeliveryAddressRequestDto requestDto, User currentUser) {
        DeliveryAddress isDeliveryAddressSaveSuccess = DeliveryAddress.from(requestDto, currentUser);
        deliveryAddressRepository.save(isDeliveryAddressSaveSuccess);
    }

    @Transactional
    public void modifyDefaultDeliveryAddress(Long deliveryAddressId, User currentUser) {
        DeliveryAddress target = deliveryAddressRepository.findByUserAndIsDefaultTrue(currentUser)
                .orElseThrow(() -> new DeliveryAddressNotFoundException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        DeliveryAddress deliveryAddressToDefault = deliveryAddressRepository.findById(deliveryAddressId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        if (target.hasPermission(currentUser.getId()) && deliveryAddressToDefault.hasPermission(currentUser.getId())) {
            target.removeDefault();
            deliveryAddressToDefault.setDefault();
        } else {
            throw new UserForbiddenException();
        }
    }
}
