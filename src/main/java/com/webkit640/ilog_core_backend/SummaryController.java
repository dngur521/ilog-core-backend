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

    /**
     * React로부터 요약할 텍스트를 받아 AI 서버의 /summary 엔드포인트로 전송하고 결과를 반환합니다. (AI 서버가
     * 'text' 필드를 Form Data로 기대하므로, 이를 맞춰서 전송합니다.)
     *
     * @param request React에서 전송한 JSON 데이터 (text 포함)
     * @return 요약 결과 또는 오류 정보가 담긴 ResponseEntity
     */
    @PostMapping("/summaries/simple")
    public ResponseEntity<SummarizeResponse> handleSimpleSummary(@RequestBody SimpleSummaryRequest request) {
        System.out.println(
                "단순 요약 요청 수신: " + request.getText().substring(0, Math.min(request.getText().length(), 50)) + "...");

        // FastAPI 서버는 'text'를 Form Data로 받습니다.
        MultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        formData.add("text", request.getText());

        try {
            // 1. WebClient를 사용하여 외부 AI 서버의 /summary 엔드포인트 호출
            AIResponse aiResponse = this.webClient.post()
                    .uri("/ai/summaries/simple") // 외부 서버의 단순 요약 엔드포인트
                    // FastAPI의 Form Data (@Form)에 맞게 Content-Type을 설정합니다.
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    // MultiValueMap을 사용하여 form-urlencoded 본문을 전송합니다.
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(AIResponse.class) // AI 서버 응답을 AIResponse DTO로 받음
                    .block(); // 동기적으로 결과 대기

            // 2. 외부 서버 응답을 React 형식으로 변환
            SummarizeResponse finalResponse = new SummarizeResponse(
                    aiResponse.getText(), // 단순 요약 결과를 summary 필드에 매핑
                    aiResponse.getError(),
                    null // 단순 요약에는 transcriptId가 필요 없음
            );
            System.out.println("AI 서버로부터 단순 요약 응답 수신 완료. React로 전달.");

            // 3. 최종 응답을 React로 반환
            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            // 외부 서버 통신 중 예외 발생 시 처리
            System.err.println("AI 서버 단순 요약 통신 중 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(new SummarizeResponse(null, "단순 요약 처리 중 서버 오류가 발생했습니다.", null));
        }
    }

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

            // AI 서버의 엔드포인트 호출
            AIResponse sttResponse = this.webClient.post()
                    .uri("/ai/summaries/audio")
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
                    .uri("/ai/summaries/retry") // 외부 서버의 재시도 엔드포인트
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

    /**
     * [RAG] React로부터 텍스트와 meetingId를 받아 AI 서버의 /rag/index 엔드포인트로 전송합니다. (AI 서버가
     * JSON으로 데이터를 기대합니다.)
     *
     * @param request React에서 전송한 JSON 데이터 (meetingId, text 포함)
     * @return 색인 결과 또는 오류 정보가 담긴 ResponseEntity
     */
    @PostMapping("/rag/index")
    public ResponseEntity<IndexResponse> handleRagIndex(@RequestBody IndexRequest request) {
        System.out.println("RAG 색인 요청 수신 (MeetingID: " + request.getMeetingId() + ")");

        try {
            // 1. WebClient를 사용하여 외부 AI 서버의 /rag/index 엔드포인트 호출
            //    FastAPI가 JSON을 기대하므로 .bodyValue() 사용
            IndexResponse aiResponse = this.webClient.post()
                    .uri("/ai/rag/index") // AI 서버의 색인 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request) // IndexRequest DTO를 JSON 본문으로 전송
                    .retrieve()
                    .bodyToMono(IndexResponse.class) // AI 서버 응답을 IndexResponse DTO로 받음
                    .block(); // 동기적으로 결과 대기

            System.out.println("AI 서버로부터 RAG 색인 응답 수신 완료. React로 전달.");

            // 2. AI 서버 응답을 React로 그대로 반환
            return ResponseEntity.ok(aiResponse);

        } catch (Exception e) {
            System.err.println("AI 서버 RAG 색인 통신 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new IndexResponse(null, "RAG 색인 처리 중 서버 오류가 발생했습니다."));
        }
    }

    /**
     * [RAG] React로부터 meetingId와 질문(query)을 받아 AI 서버의 /rag/ask 엔드포인트로 전송합니다. (AI
     * 서버가 JSON으로 데이터를 기대합니다.)
     *
     * @param request React에서 전송한 JSON 데이터 (meetingId, query 포함)
     * @return RAG 답변 또는 오류 정보가 담긴 ResponseEntity
     */
    @PostMapping("/rag/ask")
    public ResponseEntity<AskResponse> handleRagAsk(@RequestBody AskRequest request) {
        System.out.println("RAG 질문 요청 수신 (MeetingID: " + request.getMeetingId() + ", Query: " + request.getQuery() + ")");

        try {
            // 1. WebClient를 사용하여 외부 AI 서버의 /rag/ask 엔드포인트 호출
            AskResponse aiResponse = this.webClient.post()
                    .uri("/ai/rag/ask") // AI 서버의 질문 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request) // AskRequest DTO를 JSON 본문으로 전송
                    .retrieve()
                    .bodyToMono(AskResponse.class) // AI 서버 응답을 AskResponse DTO로 받음
                    .block(); // 동기적으로 결과 대기

            System.out.println("AI 서버로부터 RAG 답변 수신 완료. React로 전달.");

            // 2. AI 서버 응답을 React로 그대로 반환
            return ResponseEntity.ok(aiResponse);

        } catch (Exception e) {
            System.err.println("AI 서버 RAG 질문 통신 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new AskResponse(null, "RAG 질문 처리 중 서버 오류가 발생했습니다."));
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
    public static class SimpleSummaryRequest {

        private String text; // 요약할 텍스트

        // Getter, Setter 메소드 ...
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

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
        }

        public void setTranscriptId(String transcriptId) {
            this.transcriptId = transcriptId;
        }
    }

    // --- RAG 통신을 위한 DTO 클래스들 ---
    /**
     * React -> Spring Boot로 RAG 색인 요청을 보낼 때 사용하는 DTO. (AI 서버의 IndexRequest와 동일한
     * 구조)
     */
    public static class IndexRequest {

        private String meetingId;
        private String text;

        // Getter, Setter
        public String getMeetingId() {
            return meetingId;
        }

        public void setMeetingId(String meetingId) {
            this.meetingId = meetingId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * Spring Boot -> React로 RAG 색인 결과를 반환할 때 사용하는 DTO. (AI 서버의 IndexResponse와
     * 동일한 구조)
     */
    public static class IndexResponse {

        private String message;
        private String error;

        // 기본 생성자 (JSON Deserialization을 위해 필요할 수 있음)
        public IndexResponse() {
        }

        // 모든 필드를 받는 생성자
        public IndexResponse(String message, String error) {
            this.message = message;
            this.error = error;
        }

        // Getter, Setter
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    /**
     * React -> Spring Boot로 RAG 질문 요청을 보낼 때 사용하는 DTO. (AI 서버의 AskRequest와 동일한
     * 구조)
     */
    public static class AskRequest {

        private String meetingId;
        private String query;

        // Getter, Setter
        public String getMeetingId() {
            return meetingId;
        }

        public void setMeetingId(String meetingId) {
            this.meetingId = meetingId;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    /**
     * Spring Boot -> React로 RAG 질문 답변을 반환할 때 사용하는 DTO. (AI 서버의 AskResponse와 동일한
     * 구조)
     */
    public static class AskResponse {

        private String answer;
        private String error;

        // 기본 생성자
        public AskResponse() {
        }

        // 모든 필드를 받는 생성자
        public AskResponse(String answer, String error) {
            this.answer = answer;
            this.error = error;
        }

        // Getter, Setter
        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
    // --- SummaryController 클래스 끝 ---
}
