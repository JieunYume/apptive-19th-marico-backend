package com.apptive.marico.controller.styling;

import com.apptive.marico.dto.schedule.ServiceScheduleRequestDto;
import com.apptive.marico.dto.styling.MemberBasicInformationDto;
import com.apptive.marico.exception.CustomException;
import com.apptive.marico.service.ImageUploadService;
import com.apptive.marico.service.ServiceScheduleService;
import com.apptive.marico.service.styling.MemberStylingService;
import com.apptive.marico.utils.ApiUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.apptive.marico.exception.ErrorCode.BODYSHAPE_IMAGE_NOT_EXIST;

@RestController
@RequestMapping("/api/styling/member")
@RequiredArgsConstructor
public class MemberStylingController {
    private final MemberStylingService memberStylingService;
    private final ImageUploadService imageUploadService;
    private final ServiceScheduleService serviceScheduleService;

    // 담당 스타일리스트 조회
    @GetMapping("/")
    public ResponseEntity<?> findPersonalStylist(Principal principal) {
        return ResponseEntity.ok(memberStylingService.findPersonalStylist(principal.getName()));
    }

    // 기본 정보 등록
    @PostMapping(value = "/basic-information", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerBasicInformation(Principal principal,
                                                      @RequestPart MemberBasicInformationDto memberBasicInformationDto,
                                                      @RequestPart("bodyShapeImage") MultipartFile bodyShapeImage,
                                                      @RequestPart("myStyles") List<MultipartFile> myStyles,
                                                      @RequestPart("referenceImages") List<MultipartFile> referenceImages
                                                      ) {
        if (!bodyShapeImage.isEmpty()) {
            memberBasicInformationDto.setBodyShapeImage(imageUploadService.upload(bodyShapeImage));
        }else
            throw new CustomException(BODYSHAPE_IMAGE_NOT_EXIST);

        List<String> myStyleUrls = new ArrayList<>();
        if (!myStyles.isEmpty()) {
            myStyleUrls = imageUploadService.upload(myStyles);
        }

        List<String> referenceImageUrls = new ArrayList<>();
        if (!referenceImages.isEmpty()) {
            referenceImageUrls = imageUploadService.upload(referenceImages);
        }

        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(memberStylingService.registerBasicInformation(principal.getName(), memberBasicInformationDto, myStyleUrls, referenceImageUrls)));
    }

    // 기본 정보 조회
    @GetMapping("/basic-information")
    public ResponseEntity<?> findBasicInformation(Principal principal){
        return ResponseEntity.ok(memberStylingService.findBasicInformation(principal.getName()));
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> bookSchedule(
            Principal principal,
            @Valid @RequestBody ServiceScheduleRequestDto dto
    ) {
        return ResponseEntity.ok(new ApiUtils.ApiSuccess<>(
                serviceScheduleService.bookSchedule(principal.getName(), dto)
        ));
    }
}
