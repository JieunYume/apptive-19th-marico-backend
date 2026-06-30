package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.service.ServiceInquiry;
import com.apptive.marico.entity.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<ServiceInquiry, Long> {

    List<ServiceInquiry> findByMember(Member member);
    List<ServiceInquiry> findByStylistService(Service stylistService);

}
