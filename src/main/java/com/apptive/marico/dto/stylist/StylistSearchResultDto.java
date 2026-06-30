package com.apptive.marico.dto.stylist;

import com.apptive.marico.entity.Stylist;
import com.apptive.marico.repository.projection.StylistSearchView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class StylistSearchResultDto {

    private Long id;
    private String profileImage;
    private String stageName;
    private String oneLineIntroduction;
    private String city;
    private String state;
    private char gender;
    private List<String> styleCategories;
    private long reviewCount;

    public static StylistSearchResultDto from(StylistSearchView view) {
        List<String> categories = (view.getStyleCategories() != null && !view.getStyleCategories().isBlank())
                ? Arrays.asList(view.getStyleCategories().split(","))
                : List.of();
        return new StylistSearchResultDto(
                view.getStylistId(),
                view.getProfileImage(),
                view.getStageName(),
                view.getOneLineIntroduction(),
                view.getCity(),
                view.getState(),
                view.getGender() != null ? view.getGender().charAt(0) : '\0',
                categories,
                view.getReviewCount() != null ? view.getReviewCount() : 0L
        );
    }

    public static StylistSearchResultDto from(Stylist stylist) {
        List<String> categories = stylist.getStyles().stream()
                .map(style -> style.getCategory())
                .distinct()
                .toList();
        return new StylistSearchResultDto(
                stylist.getId(),
                stylist.getProfileImage(),
                stylist.getStageName(),
                stylist.getOneLineIntroduction(),
                stylist.getCity(),
                stylist.getState(),
                stylist.getGender(),
                categories,
                0L
        );
    }
}
