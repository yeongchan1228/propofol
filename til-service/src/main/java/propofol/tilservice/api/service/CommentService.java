package propofol.tilservice.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import propofol.tilservice.api.controller.dto.comment.CommentRequestDto;
import propofol.tilservice.api.service.UserService;
import propofol.tilservice.domain.board.entity.Board;
import propofol.tilservice.domain.board.entity.Comment;
import propofol.tilservice.domain.board.repository.BoardRepository;
import propofol.tilservice.domain.board.repository.CommentRepository;
import propofol.tilservice.domain.exception.NotFoundBoardException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserService userService;

    // 부모 댓글 저장
    @Transactional
    public Comment saveParentComment(CommentRequestDto commentDto, Long boardId, String token, String memberId) {
        Board findBoard = boardRepository.findById(boardId).orElseThrow(() -> {
            throw new NotFoundBoardException("게시글을 찾을 수 없습니다.");
        });

        // 닉네임을 가져오기 위해 user-service를 통해 유저 정보 가져오기
        String userNickname = userService.getUserNickname(token, memberId);

        Comment comment = Comment.createComment()
                .content(commentDto.getContent())
                .board(findBoard)
                .nickname(userNickname)
                .build();

//        // board를 수정하면 변경감지 > cascade에 의해 하위 타입인 comment도 함께 업데이트!
//        findBoard.addComment(comment);

        Comment savedComment = commentRepository.save(comment);
        // 부모 댓글의 groupId는 해당 댓글의 id가 그대로 들어가도록 설정.
        savedComment.addGroupInfo(savedComment.getId());

        // 리턴값으로 저장된 댓글 정보 리턴
        return savedComment;
    }

    /******************/

    // 자식 댓글 (대댓글) 저장
    @Transactional
    public Comment saveChildComment(CommentRequestDto commentDto, Long boardId, Long parentId, String token, String memberId) {
        Board findBoard = boardRepository.findById(boardId).orElseThrow(() -> {
            throw new NotFoundBoardException("게시글을 찾을 수 없습니다.");
        });

        String userNickname = userService.getUserNickname(token, memberId);

        Comment comment = Comment.createComment()
                .content(commentDto.getContent())
                .board(findBoard)
                .nickname(userNickname)
                .build();

        // 자식 댓글의 groupId는 parent의 Id를 그대로 따르도록!
        comment.addGroupInfo(parentId);
        return commentRepository.save(comment);
//        // 부모 댓글 가져오기 (최상위 계층 댓글)
//        Comment parentComment = commentRepository.findById(parentId).orElseThrow(() -> {
//            throw new NotFoundCommentException("댓글을 찾을 수 없습니다.");
//        });
//        // 자식 댓글에 대한 부모 추가
//        comment.setParent(parentComment);
//        // board를 수정하면 변경감지 > cascade에 의해 하위 타입인 comment도 함께 업데이트!
//        findBoard.addComment(comment);
    }

    /******************/

    // 댓글 페이징으로 가져오기
    public Page<Comment> getComments(Long boardId, Integer page) {
        // 댓글 페이징의 역시 10개씩 가져오도록 설정!
        // 댓글의 경우 최신 댓글이 가장 아래쪽으로 가기 때문에 오름차순으로 정렬해준다.
        PageRequest pageRequest = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.ASC, "id"));
        return commentRepository.findPageComments(boardId, pageRequest);
    }

    /******************/

    // 댓글 개수 가져오기
    public int getCommentCount(Long boardId) {
        return commentRepository.getCommentCount(boardId);
    }
}
