package com.samyookgoo.palgoosam.deliveryaddress.repository;

import com.samyookgoo.palgoosam.deliveryaddress.domain.DeliveryAddress;
import com.samyookgoo.palgoosam.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findAllByUser_Id(Long userId);

    Optional<DeliveryAddress> findByUserAndId(User user, Long addressId);

    Optional<DeliveryAddress> findDefaultByUserId(Long userId);

    Optional<DeliveryAddress> findByUserAndIsDefaultTrue(User user);

    Optional<DeliveryAddress> findById(Long id);
}