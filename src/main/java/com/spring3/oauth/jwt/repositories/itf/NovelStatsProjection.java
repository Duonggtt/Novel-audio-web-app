package com.spring3.oauth.jwt.repositories.itf;

public interface NovelStatsProjection {
    String getDate();
    Integer getNewNovels();
    Integer getCompletedNovels();
}
