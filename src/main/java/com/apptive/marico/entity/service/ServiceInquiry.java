package com.apptive.marico.entity.service;

import com.apptive.marico.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service stylistService;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;


    @ElementCollection
    @CollectionTable(name = "service_inquiry_images", joinColumns = @JoinColumn(name = "service_inquiry_id"))
    @Column(name = "inquiry_image")
    private List<String> inquiryImg = new ArrayList<>();

    private String responseContent;

    @ElementCollection
    @CollectionTable(name = "response_images", joinColumns = @JoinColumn(name = "service_inquiry_id"))
    @Column(name = "response_image")
    private List<String> responseImg = new ArrayList<>();

    private boolean answerComplete = false;

    public void addAnswer(String responseContent, List<String> responseImg) {
        this.responseContent = responseContent;
        this.responseImg = responseImg;
        this.answerComplete = true;
    }
}
