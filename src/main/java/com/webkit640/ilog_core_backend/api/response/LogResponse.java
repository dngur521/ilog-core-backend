package com.webkit640.ilog_core_backend.api.response;

import java.time.LocalDateTime;
import java.util.List;

import com.webkit640.ilog_core_backend.domain.model.ActionType;

import lombok.AllArgsConstructor;
import lombok.Data;

public class LogResponse {

    @Data
    @AllArgsConstructor
    public static class Detail {

        private List<Log> logs;
    }

    @Data
    @AllArgsConstructor
    public static class Log {

        private Long id;
        private LocalDateTime createdAt;
        private ActionType status;
    }
//    @Data
//    @AllArgsConstructor
//    public static class Meeting{
//        private Long id;
//        private Long createdAt;
//        private ActionType status;
//    }
//    @Data
//    @AllArgsConstructor
//    public static class Minutes{
//        private Long id;
//        private Long createdAt;
//        private ActionType status;
//    }
//    @Data
//    @AllArgsConstructor
//    public static class Folder{
//        private Long id;
//        private Long createdAt;
//        private ActionType status;
//    }
//    @Data
//    @AllArgsConstructor
//    public static class Participant{
//        private Long id;
//        private Long createdAt;
//        private ActionType status;
//    }
//
//    @Data
//    @AllArgsConstructor
//    public static class Memo{
//        private Long id;
//        private Long createdAt;
//        private ActionType status;
//    }

}
