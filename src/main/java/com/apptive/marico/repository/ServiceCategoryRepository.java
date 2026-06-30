package com.apptive.marico.repository;

import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ServiceCategoryRepository extends JpaRepository<ServiceContent, Long> {

    @Modifying
    @Query("DELETE FROM ServiceContent s WHERE s.stylistService = :stylistService")
    void deleteAllByStylistService(Service stylistService);
}
