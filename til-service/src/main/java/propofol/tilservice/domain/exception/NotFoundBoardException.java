package propofol.tilservice.domain.exception;

import java.util.NoSuchElementException;

// 게시글을 조회할 수 없을 때 처리되는 예외
public class NotFoundBoardException extends NoSuchElementException {
    public NotFoundBoardException() {
        super();
    }

    public NotFoundBoardException(String s) {
        super(s);
    }
}
