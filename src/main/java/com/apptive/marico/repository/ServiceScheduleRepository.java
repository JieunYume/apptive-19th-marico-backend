package com.apptive.marico.repository;

import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.ServiceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceScheduleRepository extends JpaRepository<ServiceSchedule, Long> {

    boolean existsByServiceMatchingAndServiceContent(ServiceMatching serviceMatching, ServiceContent serviceContent);
}
