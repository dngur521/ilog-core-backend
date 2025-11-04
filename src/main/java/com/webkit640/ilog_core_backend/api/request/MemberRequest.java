package com.webkit640.ilog_core_backend.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class MemberRequest {
    @Data
    public static class Create{
        @Email  @NotBlank
        private String email;
        @NotBlank
        private String name;
        @NotBlank
        private String phoneNum;
        @NotBlank
        private String password;
        @NotBlank
        private String checkPassword;
    }
    @Data
    public static class Update{
        private String name;
        private String newPassword;
        private String checkPassword;
    }
    @Data
    public static class PhoneNum{
        @NotBlank
        private String phoneNum;
    }
    @Data
    public static class Verify{
        @Email  @NotBlank
        private String email;
        @NotBlank
        private String phoneNum;
    }

    @Data
    public static class Reset{
        @NotBlank
        private String resetToken;
        @NotBlank
        private String newPassword;
        @NotBlank
        private String checkPassword;
    }
    @Data
    public static class inputPassword {
        @NotBlank
        private String password;
    }
}
