package com.samyookgoo.palgoosam.deliveryaddress.integration_test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import com.samyookgoo.palgoosam.deliveryaddress.domain.DeliveryAddress;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressRequestDto;
import com.samyookgoo.palgoosam.deliveryaddress.dto.DeliveryAddressResponseDto;
import com.samyookgoo.palgoosam.deliveryaddress.exception.DeliveryAddressBadRequestException;
import com.samyookgoo.palgoosam.deliveryaddress.exception.DeliveryAddressNotFoundException;
import com.samyookgoo.palgoosam.deliveryaddress.repository.DeliveryAddressRepository;
import com.samyookgoo.palgoosam.deliveryaddress.service.DeliveryAddressService;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DeliveryAddressService 비즈니스 로직 테스트")
class DeliveryAddressServiceBusinessLogicTest {
    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;


    @BeforeEach
    void setUp() {
        currentUser = createUser("currentUser@test.com", "currentUser");
    }

    @AfterEach
    void tearDown() {
        deliveryAddressRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("사용자는 등록한 배송지를 모두 확인할 수 있다.")
    public void getUserDeliveryAddresses() {
        //given
        String deliveryName = "deliveryName";
        String phoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        createDeliveryAddress(currentUser, deliveryName, phoneNumber, isDefault);
        createDeliveryAddress(currentUser, deliveryName, phoneNumber, !isDefault);

        //when
        List<DeliveryAddressResponseDto> addressResponseDtoList = deliveryAddressService.getUserDeliveryAddresses(
                currentUser);

        //then
        Assertions.assertThat(addressResponseDtoList).hasSize(2);

        DeliveryAddressResponseDto addressResponseDto1 = addressResponseDtoList.get(0);
        DeliveryAddressResponseDto addressResponseDto2 = addressResponseDtoList.get(1);

        Assertions.assertThat(addressResponseDto1.getName()).isEqualTo(deliveryName);
        Assertions.assertThat(addressResponseDto1.getPhoneNumber()).isEqualTo(phoneNumber);

        Assertions.assertThat(addressResponseDto2.getName()).isEqualTo(deliveryName);
        Assertions.assertThat(addressResponseDto2.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("사용자는 배송지를 등록할 수 있다.")
    public void postUserDeliveryAddress() {
        //given
        String defaultDeliveryName = "deliveryName";
        String defaultPhoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        DeliveryAddressRequestDto defaultRequestDto = createDeliveryAddressRequestDto(defaultDeliveryName,
                defaultPhoneNumber,
                isDefault);

        String normalDeliveryName = "normalName";
        String normalPhoneNumber = "010-5678-1234";
        DeliveryAddressRequestDto normalRequestDto = createDeliveryAddressRequestDto(normalDeliveryName,
                normalPhoneNumber,
                !isDefault);

        //when
        deliveryAddressService.postUserDeliveryAddress(defaultRequestDto, currentUser);
        deliveryAddressService.postUserDeliveryAddress(normalRequestDto, currentUser);

        //then
        List<DeliveryAddress> deliveryAddressList = deliveryAddressRepository.findAllByUser_Id(currentUser.getId());

        Assertions.assertThat(deliveryAddressList).hasSize(2);

        DeliveryAddress deliveryAddress1 = deliveryAddressList.get(0);
        DeliveryAddress deliveryAddress2 = deliveryAddressList.get(1);

        Assertions.assertThat(deliveryAddress1.getIsDefault()).isTrue();
        Assertions.assertThat(deliveryAddress2.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("사용자는 기존의 배송지를 삭제할 수 있다.")
    public void deleteUserDeliveryAddress_ValidUser_DeleteDeliveryAddress() {
        //given
        String deliveryName = "deliveryName";
        String phoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        DeliveryAddress created1 = createDeliveryAddress(currentUser, deliveryName, phoneNumber, isDefault);

        //when
        deliveryAddressService.deleteUserDeliveryAddress(created1.getId(), currentUser);

        //then
        List<DeliveryAddress> deliveryAddressList = deliveryAddressRepository.findAllByUser_Id(currentUser.getId());

        Assertions.assertThat(deliveryAddressList).isEmpty();
    }

    @Test
    @DisplayName("사용자가 없는 배송지를 삭제하려고 하면 NOT_FOUND 예외가 발생한다.")
    public void deleteUserDeliveryAddress_NotExisting_ThrowNotFoundException() {
        //given
        String deliveryName = "deliveryName";
        String phoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        DeliveryAddress created1 = createDeliveryAddress(currentUser, deliveryName, phoneNumber, isDefault);

        User invalidUser = createUser("invalidUser@test.com", "invalidUser");

        //when
        Throwable thrown = catchThrowable(
                () -> deliveryAddressService.deleteUserDeliveryAddress(created1.getId(), invalidUser));

        //then
        assertThat(thrown).isInstanceOf(DeliveryAddressNotFoundException.class)
                .hasMessage(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자는 기본 배송지를 변경할 수 있다.")
    public void modifyDefaultDeliveryAddress() {
        //given
        String defaultDeliveryName = "deliveryName";
        String defaultPhoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        DeliveryAddress defaultDeliveryAddress = createDeliveryAddress(currentUser, defaultDeliveryName,
                defaultPhoneNumber, isDefault);

        String normalDeliveryName = "normalName";
        String normalPhoneNumber = "010-5678-1234";
        DeliveryAddress normalDeliveryAddress = createDeliveryAddress(currentUser, normalDeliveryName,
                normalPhoneNumber, !isDefault);

        deliveryAddressRepository.saveAll(List.of(defaultDeliveryAddress, normalDeliveryAddress));

        //when
        deliveryAddressService.modifyDefaultDeliveryAddress(normalDeliveryAddress.getId(), currentUser);

        //then
        List<DeliveryAddress> result = deliveryAddressRepository.findAllByUser_Id(currentUser.getId());
        Assertions.assertThat(result).hasSize(2);

        DeliveryAddress defaultToNormal = result.get(0);
        DeliveryAddress normalToDefault = result.get(1);

        Assertions.assertThat(defaultToNormal.getIsDefault()).isFalse();
        Assertions.assertThat(normalToDefault.getIsDefault()).isTrue();
    }

//    @Test
//    @DisplayName("이미 기본 배송지인 배송지를 변경하려고 시도하면 BadRequestException 예외가 발생한다.")
//    public void modifyDefaultDeliveryAddress_AlreadyDefault_ThrowBadRequestException() {
//        //given
//        String defaultDeliveryName = "deliveryName";
//        String defaultPhoneNumber = "010-1234-5678";
//        Boolean isDefault = true;
//        DeliveryAddress defaultDeliveryAddress = createDeliveryAddress(currentUser, defaultDeliveryName,
//                defaultPhoneNumber, isDefault);
//
//        deliveryAddressRepository.saveAll(List.of(defaultDeliveryAddress));
//
//        //when
//        Throwable thrown = catchThrowable(
//                () -> deliveryAddressService.modifyDefaultDeliveryAddress(defaultDeliveryAddress.getId(), currentUser));
//
//        //then
//        assertThat(thrown).isInstanceOf(DeliveryAddressBadRequestException.class)
//                .hasMessage(ErrorCode.DELIVERY_ADDRESS_ALREADY_DEFAULT_BAD_REQUEST.getMessage())
//                .extracting("errorCode")
//                .isEqualTo(ErrorCode.DELIVERY_ADDRESS_ALREADY_DEFAULT_BAD_REQUEST);
//    }

    @Test
    @DisplayName("없는 배송지를 변경하려고 하면 NotFoundException 예외가 발생한다.")
    public void modifyDefaultDeliveryAddress_NotExisting_ThrowBadRequestException() {
        //given
        String defaultDeliveryName = "deliveryName";
        String defaultPhoneNumber = "010-1234-5678";
        Boolean isDefault = true;
        DeliveryAddress defaultDeliveryAddress = createDeliveryAddress(currentUser, defaultDeliveryName,
                defaultPhoneNumber, isDefault);

        deliveryAddressRepository.saveAll(List.of(defaultDeliveryAddress));

        //when
        Throwable thrown = catchThrowable(
                () -> deliveryAddressService.modifyDefaultDeliveryAddress(defaultDeliveryAddress.getId() + 1,
                        currentUser));

        //then
        assertThat(thrown).isInstanceOf(DeliveryAddressNotFoundException.class)
                .hasMessage(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("기본 배송지가 없을 때 기본 배송지로 변경하려고 시도하면 NotFoundException이 발생한다.")
    public void modifyDefaultDeliveryAddress_NotExistingDefaultAddress_ThrowBadRequestException() {
        //given

        //when
        Throwable thrown = catchThrowable(
                () -> deliveryAddressService.modifyDefaultDeliveryAddress(1L, currentUser));

        //then
        assertThat(thrown).isInstanceOf(DeliveryAddressNotFoundException.class)
                .hasMessage(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    // 헬퍼 함수
    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .profileImage(email + name)
                .providerId(name)
                .provider("LOCAL")
                .build();
        return userRepository.save(user);
    }

    private DeliveryAddress createDeliveryAddress(User tester, String name, String phoneNumber, Boolean isDefault) {
        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .user(tester)
                .name(name)
                .phoneNumber(phoneNumber)
                .addressLine1("서울특별시 강남구 테헤란로 123")
                .addressLine2("삼성동 빌딩 4층")
                .zipCode("12345")
                .isDefault(isDefault)
                .build();

        return deliveryAddressRepository.save(deliveryAddress);
    }

    private DeliveryAddressRequestDto createDeliveryAddressRequestDto(String name, String phoneNumber,
                                                                      Boolean isDefault) {
        return DeliveryAddressRequestDto.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .addressLine1("서울특별시 강남구 테헤란로 123")
                .addressLine2("삼성동 빌딩 4층")
                .zipCode("12345")
                .isDefault(isDefault)
                .build();
    }
}
