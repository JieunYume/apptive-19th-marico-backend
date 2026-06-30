package com.apptive.marico.entity.service;

import com.apptive.marico.dto.stylist.service.StylistServiceDto;
import com.apptive.marico.entity.Stylist;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long id;

    private String serviceName;

    private String serviceDescription;

    private int price;

    @OneToMany(mappedBy = "stylistService", cascade = CascadeType.REMOVE)
    private List<ServiceContent> serviceCategories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stylist_id")
    private Stylist stylist;

    @OneToMany(mappedBy = "stylistService", cascade = CascadeType.REMOVE)
    private List<ServiceInquiry> serviceInquiries = new ArrayList<>();

    public void editService(StylistServiceDto stylistServiceDto){
        this.serviceName = stylistServiceDto.getServiceName();
        this.serviceDescription = stylistServiceDto.getServiceDescription();
        this.price = stylistServiceDto.getPrice();
    }

}
