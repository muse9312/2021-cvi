package com.backjoongwon.cvi.user.ui;

import com.backjoongwon.cvi.post.application.PostService;
import com.backjoongwon.cvi.post.domain.Filter;
import com.backjoongwon.cvi.post.dto.PostResponse;
import com.backjoongwon.cvi.user.application.UserService;
import com.backjoongwon.cvi.user.auth.AuthenticationPrincipal;
import com.backjoongwon.cvi.user.domain.RequestUser;
import com.backjoongwon.cvi.user.dto.UserRequest;
import com.backjoongwon.cvi.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@RequestBody UserRequest userRequest, HttpServletResponse servletResponse) {
        UserResponse userResponse = userService.signup(userRequest);
        servletResponse.setHeader("Location", "/api/v1/users/" + userResponse.getId());
        return userResponse;
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse findMe(@AuthenticationPrincipal RequestUser user) {
        return userService.findUser(user);
    }

    @GetMapping("/me/posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostResponse> findMyPosts(@RequestParam(defaultValue = "NONE") Filter filter, @AuthenticationPrincipal RequestUser user) {
        return postService.findByFilter(filter, user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse find(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal RequestUser user, @RequestBody UserRequest userRequest) {
        userService.update(user, userRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal RequestUser user) {
        userService.delete(user);
    }
}
