package com.backjoongwon.cvi.post.ui;


import com.backjoongwon.cvi.ApiDocument;
import com.backjoongwon.cvi.auth.domain.authorization.SocialProvider;
import com.backjoongwon.cvi.common.exception.NotFoundException;
import com.backjoongwon.cvi.post.application.PostService;
import com.backjoongwon.cvi.post.domain.VaccinationType;
import com.backjoongwon.cvi.post.dto.PostRequest;
import com.backjoongwon.cvi.post.dto.PostResponse;
import com.backjoongwon.cvi.user.application.UserService;
import com.backjoongwon.cvi.user.domain.AgeRange;
import com.backjoongwon.cvi.user.domain.User;
import com.backjoongwon.cvi.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("게시글 컨트롤러 Mock 테스트")
@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends ApiDocument {

    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 1L;
    private static final String ACCESS_TOKEN = "{ACCESS TOKEN generated by social provider}";
    private static final String BEARER = "Bearer ";

    @MockBean
    private PostService postService;

    @MockBean
    private UserService userService;

    private User user;
    private PostRequest request;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .ageRange(AgeRange.TEENS)
                .nickname("user")
                .socialProvider(SocialProvider.NAVER)
                .profileUrl("naver.com/profile")
                .socialId("{Unique ID received from social provider}")
                .build();
        request = new PostRequest("글 내용", VaccinationType.PFIZER);
        userResponse = UserResponse.of(user, null);
    }

    @DisplayName("게시글 등록 - 성공")
    @Test
    void createPost() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        PostResponse expectedResponse = new PostResponse(POST_ID, userResponse, request.getContent(), 0, request.getVaccinationType(), LocalDateTime.now());
        given(postService.create(any(Long.class), any(PostRequest.class))).willReturn(expectedResponse);
        //when
        ResultActions response = 글_등록_요청(request);
        //then
        글_등록_성공함(response, expectedResponse);
    }

    @DisplayName("게시글 등록 - 실패")
    @Test
    void createPostFailure() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        willThrow(new NotFoundException("해당 id의 사용자가 존재하지 않습니다.")).given(postService).create(any(Long.class), any(PostRequest.class));
        //when
        ResultActions response = 글_등록_요청(request);
        //then
        글_등록_실패함(response);
    }

    @DisplayName("게시글 단일 조회 - 성공")
    @Test
    void find() throws Exception {
        //given
        PostResponse expectedPostResponse = new PostResponse(POST_ID, userResponse, "글 내용", 1, VaccinationType.PFIZER, LocalDateTime.now());
        given(postService.findById(any(Long.class))).willReturn(expectedPostResponse);
        //when
        ResultActions response = 글_단일_조회_요청(POST_ID);
        //then
        글_단일_조회_성공함(response, expectedPostResponse);
    }

    @DisplayName("게시글 단일 조회 - 실패")
    @Test
    void findFailure() throws Exception {
        //given
        willThrow(new NotFoundException("해당 id의 게시글이 존재하지 않습니다.")).given(postService).findById(any(Long.class));
        //when
        ResultActions response = 글_단일_조회_요청(POST_ID);
        //then
        글_단일_조회_실패함(response);
    }

    @DisplayName("게시글 전체 조회 - 성공")
    @Test
    void findAll() throws Exception {
        //given
        UserResponse anotherUserResponse = UserResponse.of(user, null);

        List<PostResponse> postResponses = Arrays.asList(
                new PostResponse(POST_ID + 1, anotherUserResponse, "글 내용2", 12, VaccinationType.MODERNA, LocalDateTime.now()),
                new PostResponse(POST_ID, userResponse, "글 내용1", 55, VaccinationType.PFIZER, LocalDateTime.now().minusDays(1L))
        );
        willReturn(postResponses).given(postService).findByVaccineType(VaccinationType.ALL);
        //when
        ResultActions response = 글_전체_조회_요청();
        //then
        글_전체_조회_성공함(response, postResponses);
    }

    @DisplayName("게시글 전체 조회 - 성공 - 게시글이 하나도 없는 경우")
    @Test
    void findAllWhenPostsIsEmpty() throws Exception {
        //given
        List<PostResponse> postResponses = Collections.emptyList();
        willReturn(postResponses).given(postService).findByVaccineType(VaccinationType.ALL);
        //when
        ResultActions response = 글_전체_조회_요청();
        //then
        글_전체_조회_성공함_게시글없음(response, postResponses);
    }

    @DisplayName("게시글 수정 - 성공")
    @Test
    void updatePost() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        willDoNothing().given(postService).update(any(Long.class), any(Long.class), any(PostRequest.class));
        //when
        ResultActions response = 글_수정_요청(POST_ID, request);
        //then
        글_수정_성공함(response);
    }

    @DisplayName("게시글 수정 - 실패")
    @Test
    void updatePostFailure() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        willThrow(new NotFoundException("해당 id의 게시글이 존재하지 않습니다.")).given(postService).update(any(Long.class), any(Long.class), any(PostRequest.class));
        //when
        ResultActions response = 글_수정_요청(POST_ID, request);
        //then
        글_수정_실패함(response);
    }

    @DisplayName("게시글 삭제 - 성공")
    @Test
    void deletePost() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        willDoNothing().given(postService).delete(any(Long.class), any(Long.class));
        //when
        ResultActions response = 글_삭제_요청(POST_ID);
        //then
        글_삭제_성공함(response);
    }

    @DisplayName("게시글 삭제 - 실패")
    @Test
    void deletePostFailure() throws Exception {
        //given
        given(userService.findUserByAccessToken(ACCESS_TOKEN)).willReturn(user);
        willThrow(new NotFoundException("해당 id의 게시글이 존재하지 않습니다.")).given(postService).delete(any(Long.class), any(Long.class));
        //when
        ResultActions response = 글_삭제_요청(POST_ID);
        //then
        글_삭제_실패함(response);
    }

    @DisplayName("게시글 타입별 조회 - 성공")
    @Test
    void findByVaccineType() throws Exception {
        //given
        willReturn(Arrays.asList(
                new PostResponse(1L, userResponse, "이건 내용입니다.", 100, VaccinationType.PFIZER, LocalDateTime.now()),
                new PostResponse(2L, userResponse, "이건 내용입니다.2", 200, VaccinationType.PFIZER, LocalDateTime.now()),
                new PostResponse(3L, userResponse, "이건 내용입니다.3", 300, VaccinationType.PFIZER, LocalDateTime.now())
        )).given(postService).findByVaccineType(VaccinationType.PFIZER);
        //when
        ResultActions response = 게시글_타입별_조회_요청(VaccinationType.PFIZER);
        //then
        게시글_타입별_조회_요청_성공함(response);
    }

    private ResultActions 글_등록_요청(PostRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));

    }

    private void 글_등록_성공함(ResultActions response, PostResponse postResponse) throws Exception {
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/posts/" + postResponse.getId()))
                .andDo(print())
                .andDo(toDocument("post-create"));
    }

    private void 글_등록_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("post-create-failure"));
    }

    private ResultActions 글_단일_조회_요청(Long id) throws Exception {
        return mockMvc.perform(get("/api/v1/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void 글_단일_조회_성공함(ResultActions response, PostResponse postResponse) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(postResponse)))
                .andDo(print())
                .andDo(toDocument("post-find"));
    }

    private void 글_단일_조회_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("post-find-failure"));
    }

    private ResultActions 글_전체_조회_요청() throws Exception {
        return mockMvc.perform(get("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void 글_전체_조회_성공함(ResultActions response, List<PostResponse> postResponses) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(postResponses)))
                .andDo(print())
                .andDo(toDocument("post-findAll"));
    }

    private void 글_전체_조회_성공함_게시글없음(ResultActions response, List<PostResponse> postResponses) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(postResponses)))
                .andDo(print())
                .andDo(toDocument("post-findAll-when-empty"));
    }

    private ResultActions 글_수정_요청(Long id, PostRequest request) throws Exception {
        return mockMvc.perform(put("/api/v1/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private void 글_수정_성공함(ResultActions response) throws Exception {
        response.andExpect(status().isNoContent())
                .andDo(print())
                .andDo(toDocument("post-update"));
    }

    private void 글_수정_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("post-update-failure"));
    }

    private ResultActions 글_삭제_요청(Long id) throws Exception {
        return mockMvc.perform(delete("/api/v1/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private void 글_삭제_성공함(ResultActions response) throws Exception {
        response.andExpect(status().isNoContent())
                .andDo(print())
                .andDo(toDocument("post-delete"));
    }

    private void 글_삭제_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("post-delete-failure"));
    }

    private ResultActions 게시글_타입별_조회_요청(VaccinationType vaccinationType) throws Exception {
        return mockMvc.perform(get("/api/v1/posts")
                .queryParam("vaccinationType", vaccinationType.name()));
    }

    private void 게시글_타입별_조회_요청_성공함(ResultActions response) throws Exception {
        response.andExpect(status().isOk())
                .andDo(print())
                .andDo(toDocument("post-findByVaccinationType"));
    }
}