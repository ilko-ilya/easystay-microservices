package com.samilyak.accommodationservice.repository;

import com.samilyak.accommodationservice.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    List<Accommodation> findByAddressIdIn(List<Long> addressIds);

}
