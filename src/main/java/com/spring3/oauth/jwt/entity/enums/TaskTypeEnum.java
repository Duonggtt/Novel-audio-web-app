package com.spring3.oauth.jwt.entity.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {
    READ_CHAPTER("Đọc chương truyện", "Đọc {count} chương truyện"),
    DAILY_CHECK_IN("Điểm danh", "Điểm danh hàng ngày"),
    COMMENT("Bình luận", "Viết {count} bình luận"),
    RATE_STORY("Đánh giá", "Đánh giá {count} truyện"),
    SHARE_STORY("Chia sẻ", "Chia sẻ truyện lên mạng xã hội"),
    ADD_TO_LIBRARY("Thêm vào thư viện", "Thêm {count} truyện vào thư viện"),
    READ_TIME("Thời gian đọc", "Đọc truyện trong {count} phút"),
    CONSECUTIVE_LOGIN("Đăng nhập liên tiếp", "Đăng nhập {count} ngày liên tiếp"),
    UPDATE_PROFILE("Cập nhật hồ sơ", "Hoàn thiện thông tin cá nhân"),
    FIRST_COMMENT("Bình luận đầu tiên", "Viết bình luận đầu tiên"),
    READING_STREAK("Duy trì đọc truyện", "Đọc truyện {count} ngày liên tiếp"),
    COINS_SPENT("Chi tiêu xu", "Chi tiêu {count} xu"),
    INVITE_FRIEND("Mời bạn bè", "Mời {count} bạn tham gia"),
    COMPLETE_STORY("Đọc hoàn thành", "Đọc hoàn thành {count} truyện"),
    WEEKEND_READING("Đọc cuối tuần", "Đọc truyện vào cuối tuần");

    private final String displayName;
    private final String description;

    TaskTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getFormattedDescription(int count) {
        return description.replace("{count}", String.valueOf(count));
    }
}
