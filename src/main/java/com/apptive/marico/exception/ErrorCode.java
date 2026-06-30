package com.apptive.marico.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    //400 BAD_REQUEST 잘못된 요청
    PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
    INVALID_ID(400, "아이디는 영문 소문자, 숫자 포함 6~20자 이내이어야 합니다."),
    INVALID_PASSWORD(400, "비밀번호는 영문, 숫자, 특수문자 포함 8~20자 이내이어야 합니다."),
    INVALID_NICKNAME(400, "닉네임은 한글, 영문, 숫자 중 2~10자 이내이어야 합니다."),
    ID_OR_PASSWORD_NOT_MATCH(400, "아이디 혹은 비밀번호가 틀렸습니다."),
    USER_AND_INQUIRY_NOT_MATCH(400, "등록하신 문의사항이 아닙니다."),
    MATCHING_NOT_DONE(400, "승인 완료된 매칭이 아닙니다."),
    SERVICE_CONTENT_NOT_IN_SERVICE(400, "해당 서비스에 속하지 않는 서비스 항목입니다."),
    PREFERRED_STYLE_IMAGE_NOT_EXIST(404, "이미지 파일이 존재하지 않습니다."),
    BODYSHAPE_IMAGE_NOT_EXIST(404, "체형 이미지 파일이 존재하지 않습니다."),


    VERIFICATION_CODE_INVALID(401, "발급 코드가 유효하지 않습니다."),
    EMAIL_DOES_NOT_MATCH(401, "이메일이 일치하지 않습니다."),
    AUTHORIZATION_NOT_FOUND(401, "인증 헤더가 누락되었습니다. 요청에 'AUTHORIZATION' 헤더를 포함해주세요."),

    //403 Forbidden 요청이 이해되었지만 서버가 요청을 거부했음
    TOO_MANY_SERVICES(403, "등록할 수 있는 서비스 갯수를 초과했습니다."),
    MATCHING_MEMBER_NOT_MATCH(403, "해당 매칭에 대한 권한이 없습니다."),

    //404 NOT_FOUND 잘못된 리소스 접근
    SERVICE_CONTENT_NOT_FOUND(404, "존재하지 않는 서비스 항목입니다."),
    EMAIL_NOT_FOUND(404, "존재하지 않는 이메일입니다."),
    USER_NOT_FOUND(404, "존재하지 않는 회원입니다."),
    STYLIST_MATCHING_NOT_FOUND(404, "존재하지 않는 매칭 내역입니다."),
    STYLIST_MATCHING_NOT_WAITING(404, "대기 중인 매칭 내역이 아닙니다."),
    NO_PERSONAL_STYLIST(404, "담당 스타일리스트가 없습니다."),
    NO_MY_CLIENT(404, "담당 고객이 없습니다."),
    STYLIST_NOT_FOUND(404, "존재하지 않는 스타일리스트입니다."),
    MEMBER_NOT_FOUND(404, "존재하지 않는 회원입니다."),
    STYLIST_NOT_MATCH_SERVICE(404, "자신이 등록한 서비스가 아닙니다."),
    SERVICE_NOT_FOUND(404, "존재하지 않는 서비스입니다."),
    STYLE_NOT_FOUND(404, "존재하지 않는 STYLE입니다."),
    USER_NOT_ADMIN(404, "관리자만 접근할 수 있습니다."),
    NOTICE_NOT_FOUND(404, "존재하지 않는 공지사항합니다."),
    TOKEN_NOT_FOUND(404, "존재하지 않는 TOKEN입니다."),
    VALID_ACCESS_TOKEN(404, "ACCESS TOKEN이 존재하지 않거나 기한이 초과하지 않았습니다."),
    INQUIRY_NOT_FOUND(404, "존재하지 않는 문의사항입니다."),
    REVIEW_NOT_FOUND(404, "존재하지 않는 리뷰입니다."),

    //409 CONFLICT 중복된 리소스
    ALREADY_SAVED_EMAIL(409, "이미 저장된 이메일입니다."),
    ALREADY_SAVED_ID(409, "이미 저장된 아이디입니다."),
    ALREADY_SAVED_NICKNAME(409, "이미 저장된 닉네임입니다."),
    SCHEDULE_ALREADY_EXISTS(409, "이미 예약된 스케줄이 존재합니다."),

    //419 Authentication Timeout
    VERIFICATION_CODE_TIMEOUT(419, "발급 코드의 인증 시간이 초과 되었습니다."),

    KAKAO_PAY_FAILED(500, "카카오페이 결제 처리에 실패했습니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, "서버 에러입니다. 서버 팀에 연락주세요!"),
    ROLE_NOT_FOUND(500, "존재하지 않는 ROLE입니다."),
    FILE_UPLOAD_ERROR(500, "파일 업로드에 실패했습니다.");

    private final int status;
    private final String message;
}
