package shop.mtcoding.blog.board;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.mtcoding.blog._core.config.security.MyLoginUser;
import shop.mtcoding.blog.user.User;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private final HttpSession session;
    private final BoardRepository boardRepository;

    @PostMapping("/board/{id}/update")
    public String update(@PathVariable int id,BoardRequest.UpdateDTO requestDTO){
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser") ;

        // 2. 권한 체크
        Board board = boardRepository.findById(id);

        if(board == null){
            return "error/400";
        }

        if(board.getUserId() != sessionUser.getId()){
            return "error/403";
        }

        // 3. 핵심 로직
        // update board_tb set title=?, content=? where id=?;
        boardRepository.update(requestDTO, id);

        return "redirect:/board/"+id;
    }

    @GetMapping({ "/"})
    public String index(HttpServletRequest request, @AuthenticationPrincipal MyLoginUser myLoginUser) {

        System.out.println("로그인 되었나? : " + myLoginUser.getUsername());

        List<Board> boardList = boardRepository.findAll();
        request.setAttribute("boardList", boardList);

        return "index";
    }

    @GetMapping("/board/{id}/updateForm")
    public String updateForm(@PathVariable int id, HttpServletRequest request){
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 권한 체크
        // 모델 위임(id로 board를 조회)
        Board board = boardRepository.findById(id);
        if(board.getUserId() != sessionUser.getId()){
            return "error/403";
        }

        // 3. 가방에 담기
        request.setAttribute("board", board);

        return "board/updateForm";
    }

    @GetMapping("/board/saveForm")
    public String saveForm() {
        //   session 영역에 sessionUser 키값에 user 객체 있는지 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        //   값이 null 이면 로그인 페이지로 리다이렉션
        //   값이 null 이 아니면, /board/saveForm 으로 이동
        if(sessionUser == null){
            return "redirect:/loginForm";
        }
        return "board/saveForm";
    }

    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO requestDTO, HttpServletRequest request) {
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 바디 데이터 확인 및 유효성 검사
        System.out.println(requestDTO);

        if (requestDTO.getTitle().length() > 30) {
            request.setAttribute("status", 400);
            request.setAttribute("msg", "title의 길이가 30자를 초과해서는 안되요");
            return "error/40x"; // BadRequest
        }

        // 3. 모델 위임
        // insert into board_tb(title, content, user_id, created_at) values(?,?,?, now());
        boardRepository.save(requestDTO, sessionUser.getId());

        return "redirect:/";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable int id, HttpServletRequest request) {
        //1. 모델 진입 - 상세보기 데이터 가져오기
        BoardResponse.DetailDTO responseDTO = boardRepository.findByIdWithUser(id);

        //2. 페이지 주인 여부 체크 (board의 userId와 sessionUser의 id를 비교)
        User sessionUser = (User) session.getAttribute("sessionUser");

        boolean pageOwner;
        if(sessionUser == null){
            pageOwner = false;
        }else{
            int boardUserId = responseDTO.getUserId();
            int loginUserId = sessionUser.getId();
            pageOwner = boardUserId == loginUserId;
        }

        request.setAttribute("board", responseDTO);
        request.setAttribute("pageOwner", pageOwner);
        return "board/detail";
    }

    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable int id, HttpServletRequest request){
        //바디 데이터 없어서 유효성 검사는 안 해도 된다.
        //1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        //2. 권한 체크 (이걸 테스트 하려면 postman으로만 가능하다.)
        Board board = boardRepository.findById(id);
        if(board.getUserId() != sessionUser.getId()){
            request.setAttribute("status", 403);
            request.setAttribute("msg", "게시글을 삭제할 권한이 없습니다.");
            return "error/40x";
        }


        boardRepository.deleteById(id);

        return "redirect:/";
    }


}