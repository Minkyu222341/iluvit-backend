package FIS.iLUVit.repository;

import FIS.iLUVit.config.argumentResolver.ForDB;
import FIS.iLUVit.domain.Board;
import FIS.iLUVit.domain.Center;
import FIS.iLUVit.domain.Kindergarten;
import FIS.iLUVit.domain.embeddable.Area;
import FIS.iLUVit.domain.enumtype.BoardKind;
import FIS.iLUVit.exception.BoardException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(ForDB.class))
class BoardRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Test
    @DisplayName("게시판 저장")
    public void saveBoard() throws Exception {
        //given
        Board board1 = Board.createBoard("board1", BoardKind.NORMAL, null, true);
        //when
        Board savedBoard1 = boardRepository.save(board1);
        //then
        assertThat(board1).isEqualTo(savedBoard1);
        assertThat(board1.getName()).isEqualTo(savedBoard1.getName());
        assertThat(savedBoard1.getId()).isNotNull();
        em.flush();
        em.clear();

//        Board findBoard1 = boardRepository.findByName("board1")
//                .orElseThrow(() -> new BoardException("id 값 오류"));

        Board findBoard1 = boardRepository.findByCenterIsNull().get(0);

//        Board findBoard1 = boardRepository.findById(board1.getId())
//                .orElseThrow(() -> new BoardException("id 값 오류"));

        assertThat(findBoard1.getId()).isEqualTo(savedBoard1.getId());
        assertThat(findBoard1.getName()).isEqualTo("board1");
        assertThat(findBoard1.getBoardKind()).isEqualTo(BoardKind.NORMAL);
        assertThat(findBoard1.getIsDefault()).isEqualTo(true);
    }

    @Test
    @DisplayName("게시판 조회")
    public void findBoard() throws Exception {
        //given
        Board board1 = boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, null, false));
        Board board2 = boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, null, false));

        //when
        Board findBoard1 = boardRepository.findById(board1.getId())
                .orElseThrow(() -> new BoardException("id 값 오류"));
        Board findBoard2 = boardRepository.findById(board2.getId())
                .orElseThrow(() -> new BoardException("id 값 오류"));
        //then
        assertThat(boardRepository.count()).isEqualTo(2);
        assertThat(findBoard1.getName()).isEqualTo("board1");
        assertThat(findBoard1.getBoardKind()).isEqualTo(BoardKind.NORMAL);
        assertThat(findBoard2.getName()).isEqualTo("board2");
        assertThat(findBoard2.getBoardKind()).isEqualTo(BoardKind.MARKET);
    }

    @Test
    @DisplayName("게시판 삭제")
    public void deleteBoard() throws Exception {
        //given
        Board board1 = boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, null, false));
        Board board2 = boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, null, false));

        //when
        boardRepository.delete(board1);

        //then
        assertThat(boardRepository.count()).isEqualTo(1);
        assertThat(boardRepository.findAll().get(0)).isEqualTo(board2);

    }

    @Test
    @DisplayName("센터로 게시판 조회")
    public void findBoardByCenter() throws Exception {
        //given
        Kindergarten center1 = Kindergarten.createKindergarten("떡잎유치원", "민병관", "민병관", "민간", "ㅁㄴㅇ", "2022-02-20", "02-123-1234", "www.www.www", "09:00", "19:00",
                3, 90, "서울시 금천구 뉴티캐슬", "152-052", new Area("서울시", "금천구"), 123.123, 123.123, "흙찡구놀이, 비둘기잡기", 99999, 88888, LocalDate.now(), false,
                false, 0, "gkgkgkgk", 3, 0, "얼쥡", null, null, null, null, null, null);
        Kindergarten savedCenter = centerRepository.save(center1);
        Board board1 = boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, center1, false));
        Board board2 = boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, center1, false));

        //when
        List<Board> boardList = boardRepository.findByCenter(savedCenter.getId());

        //then
        assertThat(boardList).contains(board2, board1);
        assertThat(boardList.size()).isEqualTo(2);
        assertThat(boardList.get(0).getCenter()).isEqualTo(savedCenter);
        assertThat(boardList.get(0).getCenter().getName()).isEqualTo("떡잎유치원");
        assertThat(boardList.get(1).getCenter()).isEqualTo(savedCenter);
        assertThat(boardList.get(1).getCenter().getName()).isEqualTo("떡잎유치원");

    }

    @Test
    @DisplayName("모두의 이야기 게시판 조회")
    public void findBoardByCenterIsNull() throws Exception {
        //given
        boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, null, false));
        boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, null, false));

        //when
        List<Board> boardList = boardRepository.findByCenterIsNull();

        //then
        assertThat(boardList.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("이름으로 게시판 조회")
    public void findBoardByName() throws Exception {
        //given
        Kindergarten center1 = Kindergarten.createKindergarten("떡잎유치원", "민병관", "민병관", "민간", "ㅁㄴㅇ", "2022-02-20", "02-123-1234", "www.www.www", "09:00", "19:00",
                3, 90, "서울시 금천구 뉴티캐슬", "152-052", new Area("서울시", "금천구"), 123.123, 123.123, "흙찡구놀이, 비둘기잡기", 99999, 88888, LocalDate.now(), false,
                false, 0, "gkgkgkgk", 3, 0, "얼쥡", null, null, null, null, null, null);
        Kindergarten savedCenter = centerRepository.save(center1);
        Board board1 = boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, null, false));
        Board board2 = boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, center1, false));

        //when
        Board savedBoard1 = boardRepository.findByName("board1")
                .orElseThrow(() -> new BoardException("게시판 이름 오류 in 모두의 게시판"));
        Board savedBoard2 = boardRepository.findByNameWithCenter("board2", savedCenter.getId())
                .orElseThrow(() -> new BoardException("게시판 이름 오류 in 센터의 게시판"));

        //then
        assertThat(savedBoard1.getBoardKind()).isEqualTo(BoardKind.NORMAL);
        assertThat(savedBoard1.getCenter()).isEqualTo(null);
        assertThat(savedBoard1).isEqualTo(board1);
        assertThat(savedBoard2.getBoardKind()).isEqualTo(BoardKind.MARKET);
        assertThat(savedBoard2.getCenter()).isEqualTo(center1);
        assertThat(savedBoard2).isEqualTo(board2);
    }

    @Test
    @DisplayName("Default 게시판 조회")
    public void findDefaultBoard() throws Exception {
        //given
        Kindergarten center1 = Kindergarten.createKindergarten("떡잎유치원", "민병관", "민병관", "민간", "ㅁㄴㅇ", "2022-02-20", "02-123-1234", "www.www.www", "09:00", "19:00",
                3, 90, "서울시 금천구 뉴티캐슬", "152-052", new Area("서울시", "금천구"), 123.123, 123.123, "흙찡구놀이, 비둘기잡기", 99999, 88888, LocalDate.now(), false,
                false, 0, "gkgkgkgk", 3, 0, "얼쥡", null, null, null, null, null, null);
        Kindergarten savedCenter = centerRepository.save(center1);
        Board board1 = boardRepository.save(Board.createBoard("board1", BoardKind.NORMAL, null, true));
        Board board2 = boardRepository.save(Board.createBoard("board2", BoardKind.MARKET, center1, true));

        //when
        Board findBoard1 = boardRepository.findDefaultByModu().get(0);
        Board findBoard2 = boardRepository.findDefaultByCenter(savedCenter.getId()).get(0);

        //then
        assertThat(findBoard1).isEqualTo(board1);
        assertThat(findBoard1.getCenter()).isNull();
        assertThat(findBoard1.getIsDefault()).isEqualTo(true);

        assertThat(findBoard2).isEqualTo(board2);
        assertThat(findBoard2.getCenter()).isEqualTo(center1);
        assertThat(findBoard2.getIsDefault()).isEqualTo(true);
    }


}