package com.fadhil.fashion.model;

public record ApiResponse(boolean success, String message, Object data) {

    public static ApiResponse ok(Object data) {
        return new ApiResponse(true, null, data);
    }

    public static ApiResponse message(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse created(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(false, message, null);
    }
}
