package com.samilyak.addressservice.repository;

import com.samilyak.addressservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByStreetAndAddressLineAndCityAndCountry(
            String street, String addressLine, String city, String country
    );

    List<Address> findByCityIn(List<String> cities);

    List<Address> findByCity(String city);

    List<Address> findByCountry(String country);

}