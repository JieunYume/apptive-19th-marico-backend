package com.apptive.marico.service;
import com.apptive.marico.dto.AccountDto;
import com.apptive.marico.dto.CareerDto;
import com.apptive.marico.dto.stylist.*;
import com.apptive.marico.dto.stylist.service.ServiceCategoryDto;
import com.apptive.marico.dto.stylist.service.StylistServiceDto;
import com.apptive.marico.dto.stylist.service.StylistServiceResponseDto;
import com.apptive.marico.entity.*;
import com.apptive.marico.entity.service.ServiceContent;
import com.apptive.marico.entity.service.Service;
import com.apptive.marico.entity.service.ServiceMatching;
import com.apptive.marico.entity.service.ServiceSchedule;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.repository.*;
import com.apptive.marico.service.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.apptive.marico.exception.ErrorCode.*;

@org.springframework.stereotype.Service
@Transactional
@RequiredArgsConstructor
public class StylistMypageService {
    private final StylistRepository stylistRepository;
    private final CareerRepository careerRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceContentRepository serviceCategoryRepository;
    private final ServiceMatchingRepository serviceMatchingRepository;
    private final ServiceScheduleRepository serviceScheduleRepository;
    private final ReviewRepository reviewRepository;
    private final StyleRepository styleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final ImageUploadService imageUploadService;
    public StylistMypageDto mypage(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
        return StylistMypageDto.toDto(stylist);
    }

    public StylistMypageEditDto getInformation(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        return StylistMypageEditDto.toDto(stylist);
    }

    public String editInformation(String userId, MultipartFile profileImage, StylistMypageEditDto stylistMypageEditDto) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        // 기존 career 삭제 후 다음 career 등록
        careerRepository.deleteByStylist(stylist);
        //연관관계 주인을 통한 새로운 career 저장
        List<CareerDto> careerDtoList = stylistMypageEditDto.getCareerDtoList();
        careerDtoList.stream()
                .map(careerDto -> createCareer(careerDto, stylist))
                .forEach(careerRepository::save);
        String image = imageUploadService.upload(profileImage);
        stylistMypageEditDto.setProfile_image(image);
        stylist.editStylist(stylistMypageEditDto);
        return "정상적으로 입력되었습니다.";
    }

    private static Career createCareer(CareerDto careerDto, Stylist stylist) {
        return Career.builder()
                .organizationName(careerDto.getOrganizationName())
                .content(careerDto.getContent())
                .startYear(careerDto.getStartYear())
                .endYear(careerDto.getEndYear())
                .stylist(stylist)
                .build();
    }

    public StylistServiceResponseDto getServiceList(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Service> serviceList = serviceRepository.findAllByStylistId(stylist.getId());

        List<StylistServiceDto> stylistServiceDtoList = serviceList.stream()
                .map(StylistServiceDto::toDto)
                .collect(Collectors.toList());

        return new StylistServiceResponseDto(stylistServiceDtoList);
    }

    public String addService(String userId, StylistServiceDto stylistServiceDto) {
        Stylist stylist = stylistRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        if (serviceRepository.countByStylist_id(stylist.getId()) >= 5) throw new CustomException(TOO_MANY_SERVICES);

        Service stylistService = Service.builder()
                .serviceName(stylistServiceDto.getServiceName())
                .serviceDescription(stylistServiceDto.getServiceDescription())
                .price(stylistServiceDto.getPrice())
                .stylist(stylist)
                .build();
        serviceRepository.save(stylistService);
        stylistServiceDto.getServiceCategories()
                .forEach(dto -> serviceCategoryRepository.save(createServiceCategory(dto, stylistService)));
        return "서비스가 등록되었습니다.";
    }

    public StylistServiceDto getService(String userId, Long service_id) {
        Stylist stylist = stylistRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Optional<Service> service = serviceRepository.findServiceWithStylistById(service_id);
        if (service.isEmpty()) {
            throw new CustomException(SERVICE_NOT_FOUND);
        }
        if (service.get().getStylist() != stylist) {
            throw new CustomException(STYLIST_NOT_MATCH_SERVICE);
        }
        return StylistServiceDto.toDto(service.get());
    }

    public String editService(String userId, Long serviceId, StylistServiceDto stylistServiceDto) {
        Optional<Stylist> stylist = stylistRepository.findByUserId(userId);
        if (stylist.isEmpty()) throw new CustomException(USER_NOT_FOUND);

        Optional<Service> stylistService = serviceRepository.findServiceWithStylistById(serviceId);
        if (stylistService.isEmpty()) throw new CustomException(SERVICE_NOT_FOUND);
        if (!Objects.equals(stylistService.get().getStylist(), stylist.get())) throw new CustomException(STYLIST_NOT_MATCH_SERVICE);

        serviceCategoryRepository.deleteAllByStylistService(stylistService.get());

        stylistServiceDto.getServiceCategories()
                .forEach(dto -> serviceCategoryRepository.save(createServiceCategory(dto, stylistService.get())));
        stylistService.get().editService(stylistServiceDto);
        return "서비스가 수정되었습니다.";
    }

    public String deleteService(String userId, Long serviceId) {
        Stylist stylist = stylistRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Service stylistService = serviceRepository.findServiceWithStylistById(serviceId)
                .orElseThrow(() -> new CustomException(SERVICE_NOT_FOUND));
        if (!Objects.equals(stylistService.getStylist(), stylist)) throw new CustomException(STYLIST_NOT_MATCH_SERVICE);

        // 서비스에 달린 리뷰는 FK로 막혀 있어 서비스보다 먼저 지워야 하고,
        // 지운 개수만큼 review_count 반정규화 값도 같이 줄여야 함
        long deletedReviewCount = reviewRepository.countByStylistService_Id(serviceId);
        reviewRepository.deleteAllByStylistService_Id(serviceId);

        serviceCategoryRepository.deleteAllByStylistService(stylistService);
        serviceRepository.delete(stylistService);

        if (deletedReviewCount > 0) {
            stylistRepository.addToReviewCount(stylist.getId(), (int) -deletedReviewCount);
        }
        return "서비스가 삭제되었습니다.";
    }

    public String approveMatching(String userId, Long matchingId) {
        Stylist stylist = stylistRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        ServiceMatching matching = serviceMatchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(STYLIST_MATCHING_NOT_FOUND));

        if (!matching.getService().getStylist().getId().equals(stylist.getId())) {
            throw new CustomException(STYLIST_NOT_MATCH_SERVICE);
        }

        matching.approval();

        LocalDateTime now = LocalDateTime.now();
        matching.getService().getServiceCategories().forEach(content ->
                serviceScheduleRepository.save(ServiceSchedule.builder()
                        .serviceMatching(matching)
                        .serviceContent(content)
                        .status("SCHEDULED")
                        .createdAt(now)
                        .build())
        );

        return "매칭이 승인되었습니다.";
    }

    private static ServiceContent createServiceCategory(ServiceCategoryDto categoryDto, Service stylistService) {
        return ServiceContent.builder()
                .serviceType(categoryDto.getServiceType())
                .connectionType(categoryDto.getConnectionType())
                .categoryDescription(categoryDto.getCategoryDescription())
                .stylistService(stylistService)
                .build();
    }

    public StyleDto.DtoList getStyle(String userId) {
        Optional<Stylist> stylist = stylistRepository.findByUserIdWithStyle(userId);
        if(stylist.isEmpty()) throw new CustomException(USER_NOT_FOUND);
        List<Style> styleList = stylist.get().getStyles();
        List<StyleDto> styleDtoList = styleList.stream().map(StyleDto::toDto).collect(Collectors.toList());
        return StyleDto.DtoList.builder().styleDtoList(styleDtoList).build();
    }

    public String addStyle(String userId,MultipartFile image ,StyleDto styleDto) {
        Optional<Stylist> stylist = stylistRepository.findByUserId(userId);
        if (stylist.isEmpty()) throw new CustomException(USER_NOT_FOUND);
        String imgPath = imageUploadService.upload(image);
        styleRepository.save(Style.builder()
                .image(imgPath)
                .category(styleDto.getCategory())
                .stylist(stylist.get())
                .build());
        return "스타일이 등록되었습니다.";
    }

    public String deleteStyle(String userId, DeleteStyleDto deleteStyleDto) {
        Optional<Stylist> stylist = stylistRepository.findByUserIdWithStyle(userId);
        if (stylist.isEmpty()) throw new CustomException(USER_NOT_FOUND);

        List<Style> style = stylist.get().getStyles();
        if (style.isEmpty()) throw new CustomException(STYLE_NOT_FOUND);
        Long[] styleIdList = extractStyleIds(style);

        for (Long deleteStyleId : deleteStyleDto.getDeleteStyleIdList()) {
            if (containsValue(styleIdList, deleteStyleId)) {
                styleRepository.deleteById(deleteStyleId);
            }
            else throw new CustomException(STYLE_NOT_FOUND);
        }

        return "STYLE이 삭제 되었습니다.";
    }

    private static Long[] extractStyleIds(List<Style> styleList) {
        Long[] idArray = new Long[styleList.size()];

        for (int i = 0; i < styleList.size(); i++) {
            idArray[i] = styleList.get(i).getId();
        }

        return idArray;
    }
    private static boolean containsValue(Long[] array, long targetValue) {
        for (long element : array) {
            if (element == targetValue) {
                return true;
            }
        }
        return false;
    }

    public String CheckCurrentPassword(String userId, String currentPassword) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        if(!passwordEncoder.matches(currentPassword, stylist.getPassword())){
            throw new CustomException(PASSWORD_NOT_MATCH);
        }
        return "비밀번호가 일치합니다.";
    }

    public String changePassword(String userId, String newPassword) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        customUserDetailsService.checkPasswordAvailability(newPassword);

        stylist.setPassword(passwordEncoder.encode(newPassword));
        stylistRepository.save(stylist);

        return "비밀번호가 변경되었습니다.";
    }


    public String changeEmail(String userId, String newEmail) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
        stylist.changeEmail(newEmail);
        stylistRepository.save(stylist);
        return "이메일이 정상적으로 변경되었습니다.";
    }
    public String deleteStylist(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));

        stylistRepository.delete(stylist);

        return "회원 탈퇴가 정상적으로 완료되었습니다.";
    }

    public AccountDto loadAccount(String userId) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
        return AccountDto.builder()
                .bank(stylist.getBank())
                .accountHolder(stylist.getAccountHolder())
                .accountNumber(stylist.getAccountNumber())
                .build();
    }

    public String addAccount(String userId, AccountDto accountDto) {
        Stylist stylist = stylistRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND));
        stylist.setAccount(accountDto);
        stylistRepository.save(stylist);
        return "계좌정보가 정상적으로 등록되었습니다.";
    }
}