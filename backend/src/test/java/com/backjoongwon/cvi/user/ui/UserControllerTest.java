package com.backjoongwon.cvi.user.ui;


import com.backjoongwon.cvi.ApiDocument;
import com.backjoongwon.cvi.auth.domain.authorization.SocialProvider;
import com.backjoongwon.cvi.comment.domain.Comment;
import com.backjoongwon.cvi.comment.dto.CommentResponse;
import com.backjoongwon.cvi.common.exception.InvalidInputException;
import com.backjoongwon.cvi.common.exception.NotFoundException;
import com.backjoongwon.cvi.common.exception.UnAuthorizedException;
import com.backjoongwon.cvi.post.application.PostService;
import com.backjoongwon.cvi.post.domain.Filter;
import com.backjoongwon.cvi.post.domain.VaccinationType;
import com.backjoongwon.cvi.post.dto.PostResponse;
import com.backjoongwon.cvi.user.application.UserService;
import com.backjoongwon.cvi.user.domain.AgeRange;
import com.backjoongwon.cvi.user.domain.JwtTokenProvider;
import com.backjoongwon.cvi.user.domain.User;
import com.backjoongwon.cvi.user.dto.UserRequest;
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

@DisplayName("사용자 컨트롤러 Mock 테스트")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest extends ApiDocument {

    private static final String ACCESS_TOKEN = "{ACCESS TOKEN generated by JWT}";
    private static final String BEARER = "Bearer ";
    private static final String NICKNAME = "인비";
    private static final AgeRange AGE_RANGE = AgeRange.TWENTIES;
    private static final String PROFILE_URL = "kakao.com/profile";
    private static final SocialProvider SOCIAL_PROVIDER = SocialProvider.KAKAO;
    private static final String SOCIAL_ID = "{Unique ID received from social provider}";
    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 1L;
    private static final Long COMMENT_ID = 1L;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private User user;
    private UserRequest signinRequest;
    private UserRequest updateRequest;
    private UserResponse userResponse;
    private UserResponse userMeResponse;
    private List<CommentResponse> commentResponses;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(USER_ID)
                .nickname(NICKNAME)
                .ageRange(AgeRange.TWENTIES)
                .socialProvider(SOCIAL_PROVIDER)
                .socialId(SOCIAL_ID)
                .profileUrl(PROFILE_URL)
                .build();

        signinRequest = new UserRequest(NICKNAME, AGE_RANGE, SOCIAL_PROVIDER, SOCIAL_ID, PROFILE_URL);
        updateRequest = new UserRequest(NICKNAME, AGE_RANGE, null, null, PROFILE_URL);
        userResponse = UserResponse.of(user, ACCESS_TOKEN);
        userMeResponse = UserResponse.of(user, null);

        Comment comment1 = Comment.builder().id(COMMENT_ID).content("댓글1").user(user).createdAt(LocalDateTime.now()).build();
        Comment comment2 = Comment.builder().id(COMMENT_ID + 1).content("댓글2").user(user).createdAt(LocalDateTime.now()).build();

        commentResponses = Arrays.asList(CommentResponse.of(comment1), CommentResponse.of(comment2));

        given(jwtTokenProvider.isValidToken(ACCESS_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getPayload(ACCESS_TOKEN)).willReturn(String.valueOf(user.getId()));
        given(userService.findUserById(any(Long.class))).willReturn(user);
    }

    @DisplayName("사용자 가입 - 성공")
    @Test
    void signup() throws Exception {
        //given
        willReturn(userResponse).given(userService).signup(any(UserRequest.class));
        //when
        ResultActions response = 사용자_회원가입_요청(signinRequest);
        //then
        사용자_회원가입_성공함(response, userResponse);
    }

    @DisplayName("사용자 가입 - 실패")
    @Test
    void signupFailure() throws Exception {
        //given
        willThrow(new InvalidInputException("중복된 닉네임이 존재합니다.")).given(userService).signup(any(UserRequest.class));
        //when
        ResultActions response = 사용자_회원가입_요청(signinRequest);
        //then
        사용자_회원가입_실패함(response);
    }

    @DisplayName("사용자 내 정보 조회 - 성공")
    @Test
    void findMe() throws Exception {
        //given
        willReturn(userMeResponse).given(userService).findUser(any());
        //when
        ResultActions response = 사용자_내_정보_조회_요청();
        //then
        사용자_내_정보_조회_성공함(response, userMeResponse);
    }

    @DisplayName("사용자 내 정보 조회 - 실패")
    @Test
    void findMeFailure() throws Exception {
        //given
        willThrow(new UnAuthorizedException("존재하지 않는 사용자입니다.")).given(userService).findUser(any());
        //when
        ResultActions response = 사용자_내_정보_조회_요청();
        //then
        사용자_내_정보_조회_실패함(response);
    }

    @DisplayName("사용자 정보 조회 - 성공")
    @Test
    void find() throws Exception {
        //given
        User otherUser = User.builder()
                .id(1L)
                .nickname(NICKNAME)
                .ageRange(AgeRange.TWENTIES)
                .socialProvider(SOCIAL_PROVIDER)
                .profileUrl(PROFILE_URL)
                .build();
        UserResponse userResponseWithoutPrivacy = UserResponse.of(otherUser, null);
        willReturn(userResponseWithoutPrivacy).given(userService).findById(any(Long.class));
        //when
        ResultActions response = 사용자_조회_요청(userResponseWithoutPrivacy.getId());
        //then
        사용자_조회_성공함(response, userResponseWithoutPrivacy);
    }

    @DisplayName("사용자 정보 조회 - 실패")
    @Test
    void findFailure() throws Exception {
        //given
        willThrow(new NotFoundException("해당 id의 사용자가 존재하지 않습니다.")).given(userService).findById(any(Long.class));
        //when
        ResultActions response = 사용자_조회_요청(userResponse.getId());
        //then
        사용자_조회_실패함(response);
    }

    @DisplayName("사용자 업데이트 -  성공")
    @Test
    void update() throws Exception {
        //given
        willDoNothing().given(userService).update(any(), any(UserRequest.class));
        //when
        ResultActions response = 사용자_업데이트_요청(updateRequest);
        //then
        사용자_업데이트_성공(response);
    }

    @DisplayName("사용자 업데이트 -  실패")
    @Test
    void updateFailure() throws Exception {
        //given
        willThrow(new InvalidInputException("중복된 닉네임이 존재합니다."))
                .given(userService).update(any(), any(UserRequest.class));
        //when
        ResultActions response = 사용자_업데이트_요청(updateRequest);
        //then
        사용자_업데이트_실패함(response);
    }

    @DisplayName("사용자 삭제 -  성공")
    @Test
    void deleteUser() throws Exception {
        //given
        willDoNothing().given(userService).delete(any());
        //when
        ResultActions response = 사용자_삭제_요청();
        //then
        사용자_삭제_성공(response);
    }

    @DisplayName("사용자 삭제 -  실패")
    @Test
    void deleteFailure() throws Exception {
        //given
        willThrow(new NotFoundException("해당 id의 사용자가 존재하지 않습니다.")).given(userService).delete(any());
        //when
        ResultActions response = 사용자_삭제_요청();
        //then
        사용자_삭제_실패(response);
    }

    @DisplayName("내가 작성한 게시글 조회 - 성공")
    @Test
    void findMyPostsWhenFilterIsNone() throws Exception {
        //given
        List<PostResponse> postResponses = Arrays.asList(
                new PostResponse(POST_ID, userResponse, "글 내용1", 55, 5, true, commentResponses, VaccinationType.PFIZER, LocalDateTime.now().minusDays(1L)),
                new PostResponse(POST_ID + 1, userResponse, "글 내용2", 12, 0, false, Collections.emptyList(), VaccinationType.MODERNA, LocalDateTime.now()),
                new PostResponse(POST_ID + 2, userResponse, "글 내용3", 12, 0, false, Collections.emptyList(), VaccinationType.ASTRAZENECA, LocalDateTime.now()));
        Filter filter = Filter.NONE;
        willReturn(postResponses).given(postService).findByUserAndFilter(any(), any(Filter.class));
        //when
        ResultActions response = 내가_쓴_글_조회_요청();
        //then
        마이페이지_글_필터링_조회_요청_성공함(response, postResponses, filter);
    }
//
//    @DisplayName("내가 좋아요 한 글 조회 - 성공")
//    @Test
//    void findMyPostsWhenFilterIsLikes() throws Exception {
//        //given
//        List<PostResponse> postResponses = Arrays.asList(
//                new PostResponse(POST_ID, userResponse, "글 내용1", 55, 5, true, commentResponses, VaccinationType.PFIZER, LocalDateTime.now().minusDays(1L)),
//                new PostResponse(POST_ID + 1, userResponse, "글 내용2", 12, 0, true, Collections.emptyList(), VaccinationType.MODERNA, LocalDateTime.now()));
//        Filter filter = Filter.LIKES;
//        willReturn(postResponses).given(postService).findByUserAndFilter(any(RequestUser.class), any(Filter.class));
//        //when
//        ResultActions response = 마이페이지_글_필터링_조회_요청(filter);
//        //then
//        마이페이지_글_필터링_조회_요청_성공함(response, postResponses, filter);
//    }
//
//    @DisplayName("내가 댓글을 단 게시글 조회 - 성공")
//    @Test
//    void findMyPostsWhenFilterIsComments() throws Exception {
//        //given
//        List<PostResponse> postResponses = Arrays.asList(
//                new PostResponse(POST_ID, userResponse, "글 내용1", 55, 5, true, commentResponses, VaccinationType.PFIZER, LocalDateTime.now().minusDays(1L)),
//                new PostResponse(POST_ID + 1, userResponse, "글 내용2", 12, 0, false, commentResponses, VaccinationType.MODERNA, LocalDateTime.now()));
//        Filter filter = Filter.COMMENTS;
//        willReturn(postResponses).given(postService).findByUserAndFilter(any(RequestUser.class), any(Filter.class));
//        //when
//        ResultActions response = 마이페이지_글_필터링_조회_요청(filter);
//        //then
//        마이페이지_글_필터링_조회_요청_성공함(response, postResponses, filter);
//    }
//
    @DisplayName("내가 작성한 게시글 조회 - 실패")
    @Test
    void findMyPostsFailureWhenFilterIsNone() throws Exception {
        //given
        willThrow(new UnAuthorizedException("유효하지 않은 토큰입니다.")).given(postService).findByUserAndFilter(any(), any(Filter.class));
        Filter filter = Filter.NONE;
        //when
        ResultActions response = 내가_쓴_글_조회_요청();
        //then
        마이페이지_글_필터링_조회_요청_실패함(response, filter);
    }
//
//    @DisplayName("내가 좋아요 한 글 조회 - 실패")
//    @Test
//    void findMyPostsFailureWhenFilterIsLikes() throws Exception {
//        //given
//        willThrow(new UnAuthorizedException("유효하지 않은 토큰입니다.")).given(postService).findByUserAndFilter(any(RequestUser.class), any(Filter.class));
//        Filter filter = Filter.LIKES;
//        //when
//        ResultActions response = 마이페이지_글_필터링_조회_요청(filter);
//        //then
//        마이페이지_글_필터링_조회_요청_실패함(response, filter);
//    }
//
//    @DisplayName("내가 댓글을 단 게시글 조회 - 실패")
//    @Test
//    void findMyPostsFailureWhenFilterIsComments() throws Exception {
//        //given
//        willThrow(new UnAuthorizedException("유효하지 않은 토큰입니다.")).given(postService).findByUserAndFilter(any(RequestUser.class), any(Filter.class));
//        Filter filter = Filter.COMMENTS;
//        //when
//        ResultActions response = 마이페이지_글_필터링_조회_요청(filter);
//        //then
//        마이페이지_글_필터링_조회_요청_실패함(response, filter);
//    }

    private ResultActions 내가_쓴_글_조회_요청() throws Exception {
        return mockMvc.perform(get("/api/v1/users/me/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private ResultActions 마이페이지_글_필터링_조회_요청(Filter filter) throws Exception {
        return mockMvc.perform(get("/api/v1/users/me/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN)
                .queryParam("filter", filter.name()));
    }

    private void 마이페이지_글_필터링_조회_요청_성공함(ResultActions response, List<PostResponse> postResponses, Filter filter) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(postResponses)))
                .andDo(print())
                .andDo(toDocument("user-me-posts-filter-" + filter.name().toLowerCase()));
    }

    private void 마이페이지_글_필터링_조회_요청_실패함(ResultActions response, Filter filter) throws Exception {
        response.andExpect(status().isUnauthorized())
                .andDo(print())
                .andDo(toDocument("user-me-posts-filter-" + filter.name().toLowerCase() + "-failure"));
    }

    private ResultActions 사용자_회원가입_요청(UserRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    private void 사용자_회원가입_성공함(ResultActions response, UserResponse userResponse) throws Exception {
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/" + userResponse.getId()))
                .andExpect(content().json(toJson(userResponse)))
                .andDo(print())
                .andDo(toDocument("user-signup"));
    }

    private void 사용자_회원가입_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(toDocument("user-signup-failure"));
    }

    private ResultActions 사용자_내_정보_조회_요청() throws Exception {
        return mockMvc.perform(get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private void 사용자_내_정보_조회_성공함(ResultActions response, UserResponse userResponse) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(userResponse)))
                .andDo(print())
                .andDo(toDocument("user-find-me"));
    }

    private void 사용자_내_정보_조회_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isUnauthorized())
                .andDo(print())
                .andDo(toDocument("user-find-me-failure"));
    }

    private ResultActions 사용자_조회_요청(Long id) throws Exception {
        return mockMvc.perform(get("/api/v1/users/" + id)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void 사용자_조회_성공함(ResultActions response, UserResponse userResponse) throws Exception {
        response.andExpect(status().isOk())
                .andExpect(content().json(toJson(userResponse)))
                .andDo(print())
                .andDo(toDocument("user-find"));
    }

    private void 사용자_조회_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("user-find-failure"));
    }


    private ResultActions 사용자_업데이트_요청(UserRequest request) throws Exception {
        return mockMvc.perform(put("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private void 사용자_업데이트_성공(ResultActions response) throws Exception {
        response.andExpect(status().isNoContent())
                .andDo(print())
                .andDo(toDocument("user-update"));
    }

    private void 사용자_업데이트_실패함(ResultActions response) throws Exception {
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(toDocument("user-update-failure"));
    }

    private ResultActions 사용자_삭제_요청() throws Exception {
        return mockMvc.perform(delete("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, BEARER + ACCESS_TOKEN));
    }

    private void 사용자_삭제_성공(ResultActions response) throws Exception {
        response.andExpect(status().isNoContent())
                .andDo(print())
                .andDo(toDocument("user-delete"));
    }

    private void 사용자_삭제_실패(ResultActions response) throws Exception {
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andDo(toDocument("user-delete-failure"));
    }
}
