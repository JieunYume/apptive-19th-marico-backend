package com.apptive.marico.entity;

import com.apptive.marico.dto.AccountDto;
import com.apptive.marico.dto.stylist.StylistMypageEditDto;
import com.apptive.marico.entity.service.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stylists")
public class Stylist implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stylist_id")
    private Long id;

    // 스타일리스트 이름
    @Column(nullable = false)
    private String name;

    // 이메일
    @Column(updatable = false,unique = true,nullable = false)
    private String email;

    @Column(updatable = false, unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private char gender;

    private LocalDate birthDate;

    private String city;

    private String state;

    private String profileImage;

    private String stageName;

    private String oneLineIntroduction;

    private String stylistIntroduction;

    private String chat_link;

    private String phoneNumber;

    private String bank;

    private String accountNumber;

    private String accountHolder;

    @OneToMany(mappedBy = "stylist" , cascade = CascadeType.ALL, orphanRemoval = true) // 연결이 끊어진 career는 자동 삭제
    private List<Career> career;

    @OneToMany(mappedBy = "stylist", orphanRemoval = true) // 연결이 끊어진 스타일은 자동 삭제
    private List<Style> styles = new ArrayList<>();

    @OneToMany(mappedBy = "stylist", orphanRemoval = true)
    private List<StylistNoticeReadStatus> noticeReadStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "stylist", orphanRemoval = true)
    private List<Service> stylistServices = new ArrayList<>();

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int reviewCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "stylists_roles",
            joinColumns = @JoinColumn(name = "stylist_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void editStylist(StylistMypageEditDto stylistMypageEditDto) {
        this.setProfileImage(stylistMypageEditDto.getProfile_image());
        this.setStageName(stylistMypageEditDto.getStageName());
        this.setOneLineIntroduction(stylistMypageEditDto.getOneLineIntroduction());
        this.setStylistIntroduction(stylistMypageEditDto.getStylistIntroduction());
        this.setCity(stylistMypageEditDto.getCity());
        this.setState(stylistMypageEditDto.getState());
        this.setChat_link(stylistMypageEditDto.getChat_link());
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void setAccount(AccountDto accountDto) {
        this.bank = accountDto.getBank();
        this.accountNumber = accountDto.getAccountNumber();
        this.accountHolder = accountDto.getAccountHolder();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
//    Collectors.toList() 메서드를 이용하여 반환되는 Stream 객체를 List 형태로 변환
                .collect(Collectors.toList());
    }


    @Override
    public String getUsername() {
        return this.userId;
    }

    //    GrantedAuthority 객체를 생성할 때 문자열 변환이 필요하지 않기 때문에 유연성이 높아지며, roles 필드를 추가적으로 변경해야 할 경우, 해당 필드만 수정하면 되므로 유지보수가 용이
    // 계정이 만료됐는지 리턴 -> true는 만료X를 의미
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //  계정이 잠겨있는지 리턴 -> true는 잠기지 않음
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호가 만료됐는지 리턴 -> true는 만료X를 의미
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화돼 있는지 리턴 -> true는 활성화O 의미
    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
