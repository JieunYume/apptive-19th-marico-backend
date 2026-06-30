package com.apptive.marico.repository;

import com.apptive.marico.entity.service.ServiceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ServiceReview, Long> {

    @Query("SELECT r FROM ServiceReview r " +
            "JOIN FETCH r.member " +
            "WHERE r.stylistService.id = :serviceId " +
            "ORDER BY r.createdAt DESC")
    List<ServiceReview> findAllByServiceId(@Param("serviceId") Long serviceId);

    long countByStylistService_Id(Long serviceId);

    void deleteAllByStylistService_Id(Long serviceId);

//    @Query("SELECT r FROM Review r " +
//            "JOIN FETCH r.member " +
//            "JOIN r.stylistService ss " +
//            "WHERE ss.stylist.id = :stylistId " +
//            "ORDER BY r.createdAt DESC")
//    List<Review> findAllByStylistId(@Param("stylistId") Long stylistId);
}
