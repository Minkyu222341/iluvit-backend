package FIS.iLUVit.service;

import FIS.iLUVit.controller.dto.BookmarkMainDTO;
import FIS.iLUVit.domain.*;
import FIS.iLUVit.exception.BoardException;
import FIS.iLUVit.exception.BookmarkException;
import FIS.iLUVit.exception.UserException;
import FIS.iLUVit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    public BookmarkMainDTO search(Long userId) {
        BookmarkMainDTO dto = new BookmarkMainDTO();

        // 유저의 즐찾 게시판에서 최신 글 하나씩 뽑아옴.
        List<Post> posts = bookmarkRepository.findPostByBoard(userId);
        // stream groupingBy가 null 키 값을 허용하지 않아서 임시 값으로 생성한 센터
        Center tmp = new Center();

        // 최신 글 리스트를 센터로 그루핑함.
        Map<Center, List<Post>> centerPostMap = posts.stream()
                .collect(Collectors.groupingBy(p -> p.getBoard().getCenter() == null ?
                        tmp : p.getBoard().getCenter()));

        // ~의 이야기 DTO의 리스트
        List<BookmarkMainDTO.StoryDTO> storyDTOS = new ArrayList<>();

        // 센터(이야기)-게시글리스트 Map 루프 돌림.
        centerPostMap.forEach((c, pl) -> {
            BookmarkMainDTO.StoryDTO storyDTO = new BookmarkMainDTO.StoryDTO();
            // (~의 이야기안의 게시판 + 최신글 1개씩) DTO를 모아 리스트로 만듬.
            List<BookmarkMainDTO.BoardDTO> boardDTOS = pl.stream()
                    .map(p -> new BookmarkMainDTO.BoardDTO(
                            p.getBoard().getId(), p.getBoard().getName(), p.getTitle()))
                    .collect(Collectors.toList());
            // ~의 이야기에 (게시판+최신글) DTO 리스트 넣어줌.
            storyDTO.setBoardDTOList(boardDTOS);
            // 센터 아이디 널이면 모두, 아니면 시설 이야기
            if (c.getId() == null) {
                storyDTO.setCenter_id(null);
                storyDTO.setStory_name("모두의 게시판");
                dto.getStories().add(storyDTO);
            } else {
                storyDTO.setCenter_id(c.getId());
                storyDTO.setStory_name(c.getName());
                storyDTOS.add(storyDTO);
            }
        });

        // 시설의 이야기 리스트는 아이디로 정렬 후
        List<BookmarkMainDTO.StoryDTO> newDTO = storyDTOS.stream()
                .sorted(Comparator.comparing(BookmarkMainDTO.StoryDTO::getCenter_id))
                .collect(Collectors.toList());

        // 최종 결과 dto에 넣어서 반환함.
        newDTO.forEach(s -> dto.getStories().add(s));

        return dto;
    }

    public Long create(Long userId, Long boardId) {
        // 가장 최근에 추가한 북마크의 order 가져옴.
        int max = bookmarkRepository.findMaxOrder(userId)
                .orElse(0);

        User findUser = userRepository.getById(userId);
        Board findBoard = boardRepository.getById(boardId);
        Bookmark bookmark = new Bookmark(max + 1, findBoard, findUser);
        return bookmarkRepository.save(bookmark).getId();
    }

    public Long delete(Long userId, Long bookmarkId) {
        Bookmark findBookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new BookmarkException("존재하지 않는 북마크"));
        if (!Objects.equals(findBookmark.getUser().getId(), userId)) {
            throw new UserException("삭제 권한 없는 유저");
        }
        bookmarkRepository.delete(findBookmark);
        return bookmarkId;
   }
}
