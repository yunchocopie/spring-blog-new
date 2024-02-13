package shop.mtcoding.blog.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import shop.mtcoding.blog._core.config.security.MyLoginUser;
import shop.mtcoding.blog.user.User;
import shop.mtcoding.blog.user.UserRepository;
import shop.mtcoding.blog.user.UserRequest;

import java.sql.PreparedStatement;

@RequiredArgsConstructor // final 붙은 애들에 대한 생성자 생성
@Controller
public class UserController {

    // 자바는 final 변수는 반드시 초기화 되어야한다.
    private final UserRepository userRepository;
    private final HttpSession session;
    private final BCryptPasswordEncoder passwordEncoder;

//    @PostMapping("/login")
//    public String login(UserRequest.LoginDTO requestDTO) {
//        System.out.println(requestDTO);
//
//        if (requestDTO.getUsername().length() < 3) {
//            return "error/400"; // ViewResolver 설정이 되어 있음 (앞 경로, 뒤 경로)
//        }
//
//        User user = userRepository.findByUsernameAndPassword(requestDTO);
//
//        if (user == null) { // 조회 안됨 (401)
//            return "error/401";
//        } else {
//            session.setAttribute("sessionUser", user); // 락카에 담음 (StateFul)
//        }
//
//        return "redirect:/";
//    }

    @PostMapping("/join")
    public String join(UserRequest.JoinDTO requestDTO) {
        System.out.println(requestDTO);

        String rawPassword = requestDTO.getPassword();
        String encPassword = passwordEncoder.encode(rawPassword);

        userRepository.save(requestDTO); // Request 한 값을 저장 시킨다.
        return "redirect:/loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDTO requestDTO, HttpServletRequest request) {
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/loginForm";
        }

        request.setAttribute("requestUser", sessionUser);
        User requestUser = (User) request.getAttribute("requestUser");

        userRepository.update(requestDTO, requestUser.getId());
        requestUser.setPassword(requestDTO.getPassword());
        session.setAttribute("sessionUser", requestUser);

        return "redirect:/logout";
    }

    @GetMapping("/user/updateForm")
    public String updateForm(HttpServletRequest request, @AuthenticationPrincipal MyLoginUser myLoginUser) {
        User user = userRepository.findByUsername(myLoginUser.getUsername());
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/loginForm";
        }

        return "user/updateForm";
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 세션을 완전히 삭제. 서랍 비우기.
        return "redirect:/";
    }
}