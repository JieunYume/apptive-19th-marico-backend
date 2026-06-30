package com.apptive.marico.entity.service;

import com.apptive.marico.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMatching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_service_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int price;

    @ColumnDefault("'READY'")
    private String approvalStatus;

    private String tid;

    // 결제(구 Order) 정보 — 매칭 신청 시점엔 비어 있고, 결제 승인 시 채워짐
    private LocalDateTime orderedAt;

    private String status;

    public void ready(String tid) {
        this.approvalStatus = "READY";
        this.tid = tid;
    }

    public void waitingForApproval() {
        this.approvalStatus = "WAITING";
    }

    public void approval() {
        this.approvalStatus = "DONE";
    }

    public void denial() {
        this.approvalStatus = "DENY";
    }

    public void completePayment(LocalDateTime orderedAt, int paidPrice) {
        this.orderedAt = orderedAt;
        this.price = paidPrice;
        this.status = "PAID";
    }
}
