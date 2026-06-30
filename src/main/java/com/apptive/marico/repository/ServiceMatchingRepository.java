package com.apptive.marico.repository;

import com.apptive.marico.entity.Member;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceMatchingRepository extends JpaRepository<ServiceMatching, Long> {
    List<ServiceMatching> findByService(Service service);

    Optional<ServiceMatching> findFirstByMemberAndApprovalStatusOrderByIdDesc(Member member, String approvalStatus);
}
