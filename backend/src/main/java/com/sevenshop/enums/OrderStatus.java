package com.sevenshop.enums;

public enum OrderStatus {
    PENDING(0, "待处理"),
    PROCESSED(1, "已处理"),
    CANCELLED(-1, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
