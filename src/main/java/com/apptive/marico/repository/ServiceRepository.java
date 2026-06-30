package com.apptive.marico.repository;

import com.apptive.marico.entity.service.ServiceType;
import com.apptive.marico.entity.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT s FROM Service s JOIN FETCH s.serviceCategories WHERE s.stylist.id = :stylist_id")
    List<Service> findAllByStylistId(Long stylist_id);

    @Query("SELECT s FROM Service s JOIN FETCH s.serviceCategories WHERE s.stylist.userId = :userId")
    List<Service> findAllByStylistUserId(String userId);

    int countByStylist_id(Long stylist_id);

    @Query("SELECT s FROM Service s JOIN FETCH s.stylist WHERE s.id = :service_id")
    Optional<Service> findServiceWithStylistById(Long service_id);

    @Query("SELECT DISTINCT s FROM Service s " +
            "JOIN FETCH s.serviceCategories sc " +
            "JOIN FETCH s.stylist st " +
            "LEFT JOIN st.styles style " +
            "WHERE (:serviceType IS NULL OR sc.serviceType = :serviceType) " +
            "AND (:styleCategory IS NULL OR style.category = :styleCategory) " +
            "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice)")
    List<Service> searchByFilters(
            @Param("serviceType") ServiceType serviceType,
            @Param("styleCategory") String styleCategory,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice
    );
}
