package com.apptive.marico.repository.projection;

public interface StylistSearchView {
    Long getStylistId();
    String getProfileImage();
    String getStageName();
    String getOneLineIntroduction();
    String getCity();
    String getState();
    String getGender();
    String getStyleCategories();
    Long getReviewCount();
}
