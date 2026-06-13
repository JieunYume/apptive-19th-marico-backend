package com.apptive.marico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.REMOVE)
    private List<MemberNoticeReadStatus> memberNoticeReadStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "notice", cascade = CascadeType.REMOVE)
    private List<StylistNoticeReadStatus> stylistNoticeReadStatuses = new ArrayList<>();
}
