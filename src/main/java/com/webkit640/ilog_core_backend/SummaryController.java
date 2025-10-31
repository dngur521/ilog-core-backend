package com.webkit640.ilog_core_backend;

import java.time.Duration;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

/**
 * React 프론트엔드와 외부 요약 서버(AI 서버) 간의 통신을 중개하는 컨트롤러입니다. 오디오 파일을 받아 AI 서버로 전달하고, 그
 * 결과를 다시 프론트엔드로 반환합니다. 요약 재시도 기능도 처리합니다.
 */
@RestController
public class SummaryController {

    // 외부 STT/요약 서버의 주소 (Windows 서버)
    private static final String AI_SERVER_URL = "http://192.168.0.5:8081";

    // 비동기 HTTP 통신을 위한 WebClient 인스턴스
    private final WebClient webClient;

    /**
     * 생성자. WebClient를 초기화하며, 외부 서버와의 통신 타임아웃을 10분으로 설정합니다.
     *
     * @param webClientBuilder Spring Boot가 자동으로 주입해주는 WebClient 빌더
     */
    public SummaryController(WebClient.Builder webClientBuilder) {
        // 응답 타임아웃을 10분으로 설정 (STT 및 요약 처리가 오래 걸릴 수 있음)
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(10));

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // 타임아웃 설정을 포함한 HttpClient 적용
                .baseUrl(AI_SERVER_URL) // 기본 URL 설정
                .build();
    }

    /*
    *
    * TODO: 간단 요약 API 엔드포인트 생성 및 AI 서버 엔드포인트(/summary)로 데이터 전송 및 받아오기
    *
    * */

    /**
     * React로부터 5분 단위 또는 최종 오디오 청크를 받아 AI 서버로 전달합니다.
     *
     * @param meetingId  회의 세션 전체를 식별하는 고유 ID
     * @param startTime  회의 시작 시간 (최종 요약 시 사용)
     * @param isFinal    이 청크가 마지막인지 여부 (true/false)
     * @param audioFiles 오디오 파일 목록
     * @return 중간 요약 (부분) 또는 최종 요약 (전체)
     */
    @PostMapping("/summaries/audio")
    public ResponseEntity<SummarizeResponse> handleAudioChunk(
            @RequestParam("meetingId") String meetingId,
            @RequestParam("startTime") String startTime,
            @RequestParam("isFinal") boolean isFinal, // boolean으로 받음
            @RequestParam("audio_files") List<MultipartFile> audioFiles) {

        System.out.println("오디오 청크 " + audioFiles.size() + "개 수신 (MeetingID: " + meetingId + ", isFinal: " + isFinal + ")");

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("meetingId", meetingId);
            bodyBuilder.part("startTime", startTime);
            bodyBuilder.part("isFinal", String.valueOf(isFinal)); // Desktop으로 전달

            for (MultipartFile file : audioFiles) {
                // MultipartFile을 ByteArrayResource로 변환 (WebClient 전송용)
                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        // 원본 파일 이름 유지
                        return file.getOriginalFilename();
                    }
                };
                bodyBuilder.part("audio_files", resource);
            }
            MultiValueMap<String, HttpEntity<?>> multipartBody = bodyBuilder.build();

            // AI 서버의 엔드포인트(/process-audio-chunk) 호출
            AIResponse sttResponse = this.webClient.post()
                    .uri("/process-audio-chunk") // [수정] Desktop의 새 엔드포인트
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBody))
                    .retrieve()
                    .bodyToMono(AIResponse.class)
                    .block();

            // React 형식으로 변환하여 반환
            SummarizeResponse finalResponse = new SummarizeResponse(
                    sttResponse.getText(), // 중간 또는 최종 요약
                    sttResponse.getError(),
                    sttResponse.getTranscriptId() // 최종 요약 실패 시 meetingId가 반환됨
            );

            System.out.println("AI 서버로부터 응답 수신 완료 (isFinal: " + isFinal + "). React로 전달.");
            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            // 외부 서버 통신 중 예외 발생 시 처리
            System.err.println("AI 서버 통신 중 오류: " + e.getMessage());
            e.printStackTrace();

            // 최종 요약 실패 시 재시도를 위해 meetingId를 transcriptId로 넘겨줌
            return ResponseEntity.internalServerError()
                    .body(new SummarizeResponse(null, "중간 서버 오류", isFinal ? meetingId : null));
        }
    }

    /**
     * React 프론트엔드로부터 요약 재시도 요청을 받아 외부 AI 서버로 전달합니다. (React에서 isRetry: true와 함께
     * JSON으로 전송)
     *
     * @param retryRequest 프론트엔드에서 전송한 JSON 데이터 (startTime, transcriptId,
     * isRetry 포함)
     * @return 재시도된 요약 결과 또는 오류 정보가 담긴 ResponseEntity
     */
    @PostMapping("/summaries/retry")
    public ResponseEntity<SummarizeResponse> handleRetry(@RequestBody RetryRequest retryRequest) {
        System.out.println("최종 요약 재시도 요청 수신: " + retryRequest.getTranscriptId());

        try {
            // 1. WebClient를 사용하여 외부 STT 서버의 재시도 엔드포인트 호출
            //    이번에는 JSON 형식으로 요청 본문을 전송합니다.
            AIResponse sttResponse = this.webClient.post()
                    .uri("/retry-final-summary") // 외부 서버의 재시도 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON) // 요청 타입은 JSON
                    .bodyValue(retryRequest) // RetryRequest DTO를 JSON 본문으로 설정
                    .retrieve()
                    .bodyToMono(AIResponse.class)
                    .block(); // 동기적으로 결과 대기

            // 2. 외부 서버 응답을 React 형식으로 변환
            SummarizeResponse finalResponse = new SummarizeResponse(
                    sttResponse.getText(),
                    sttResponse.getError(),
                    sttResponse.getTranscriptId() // 재시도 실패 시 transcriptId 유지될 수 있음
            );
            System.out.println("AI 서버로부터 재시도 응답 수신 완료. React로 전달.");

            // 3. 최종 응답을 React로 반환
            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            // 재시도 요청 처리 중 예외 발생 시
            System.err.println("AI 서버 재시도 요청 중 오류: " + e.getMessage());
            e.printStackTrace();

            // 오류 응답 반환 (이때 transcriptId는 유지하여 재시도 가능하도록 함)
            return ResponseEntity.internalServerError()
                    .body(new SummarizeResponse(null, "재시도 처리 중 중간 서버에서 오류가 발생했습니다.", retryRequest.getTranscriptId()));
        }
    }

    // --- DTO (Data Transfer Object) 클래스들 ---
    // API 간 데이터 교환을 위한 구조화된 객체들입니다.
    /**
     * React 프론트엔드로 최종 응답을 보낼 때 사용하는 DTO 클래스입니다.
     */
    public static class SummarizeResponse {

        private String summary; // 요약 결과 텍스트
        private String error; // 오류 메시지 (성공 시 null)
        private String transcriptId; // 재시도에 사용될 스크립트 ID (실패 시 반환될 수 있음)

        // 생성자, Getter 메소드 ...
        public SummarizeResponse(String summary, String error, String transcriptId) {
            this.summary = summary;
            this.error = error;
            this.transcriptId = transcriptId;
        }

        public String getSummary() {
            return summary;
        }

        public String getError() {
            return error;
        }

        public String getTranscriptId() {
            return transcriptId;
        }
    }

    /**
     * 외부 서버(AI 서버)로부터 응답을 받을 때 사용하는 DTO 클래스입니다.
     */
    public static class AIResponse {

        private String text; // 외부 서버에서는 요약 결과를 'text' 필드로 반환한다고 가정
        private String error;
        private String transcriptId;

        // 기본 생성자, Getter, Setter 메소드 ...
        public AIResponse() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getTranscriptId() {
            return transcriptId;
        }

        public void setTranscriptId(String transcriptId) {
            this.transcriptId = transcriptId;
        }
    }

    /**
     * React 프론트엔드로부터 재시도 요청을 받을 때 사용하는 DTO 클래스입니다.
     */
    public static class RetryRequest {

        private String startTime; // 회의 시작 시간
        private String transcriptId; // 재시도 대상 스크립트 ID
        private boolean isRetry; // ⬅️ React에서 추가한 필드를 받기 위해 추가

        // Getter, Setter 메소드 ...
        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getTranscriptId() {
            return transcriptId;
        }

        public boolean isRetry() {
            return isRetry;
        } // boolean 타입의 Getter는 is*로 시작하는 것이 관례

        public void setTranscriptId(String transcriptId) {
            this.transcriptId = transcriptId;
        }
    }
}
