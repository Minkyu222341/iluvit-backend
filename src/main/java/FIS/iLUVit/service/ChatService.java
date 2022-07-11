package FIS.iLUVit.service;

import FIS.iLUVit.controller.dto.ChatDTO;
import FIS.iLUVit.controller.dto.ChatListDTO;
import FIS.iLUVit.controller.dto.CreateChatRequest;
import FIS.iLUVit.controller.dto.CreateChatRoomRequest;
import FIS.iLUVit.domain.*;
import FIS.iLUVit.domain.alarms.ChatAlarm;
import FIS.iLUVit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChatRoomRepository chatRoomRepository;

    public Long saveChat(Long userId, CreateChatRequest request) {

        if (Objects.equals(userId, request.getReceiver_id())) {
            throw new IllegalStateException("자기 자신에게 쪽지를 보낼 수 없습니다");
        }

        User sendUser = userRepository.getById(userId);
        User receiveUser = userRepository.getById(request.getReceiver_id());

        Long post_id = request.getPost_id();
        Long comment_id = request.getComment_id();

        Post findPost = postRepository.getById(post_id);

        Chat chat1 = new Chat(request.getMessage(), receiveUser, sendUser);
        Chat chat2 = new Chat(request.getMessage(), receiveUser, sendUser);

        ChatRoom chatRoom1 = validateChatRoom(sendUser, receiveUser, comment_id, findPost, chat1);
        ChatRoom chatRoom2 = validateChatRoom(receiveUser, sendUser, comment_id, findPost, chat2);
        chatRoom1.updatePartnerId(chatRoom2.getId());
        chatRoom2.updatePartnerId(chatRoom1.getId());

        AlarmUtils.publishAlarmEvent(new ChatAlarm(receiveUser, sendUser));

        chatRepository.save(chat1);
        chatRepository.save(chat2);

        return chat2.getId();

    }

    private ChatRoom validateChatRoom(User sendUser, User receiveUser, Long comment_id, Post post, Chat chat) {
        ChatRoom findRoom = chatRoomRepository.findByReceiverAndSenderAndPost(receiveUser, sendUser, post)
                .orElse(null);
        if (findRoom == null) {
            // 대화방 없으면 새로 생성
            ChatRoom chatRoom = new ChatRoom(receiveUser, sendUser, post);
            // 댓글 작성자와 쪽지 교환이면 comment 정보도 엮여줌.
            if (comment_id != null) {
                Comment findComment = commentRepository.getById(comment_id);
                chatRoom.updateComment(findComment);
            }
            chatRoomRepository.save(chatRoom);
            chat.updateChatRoom(chatRoom);
            return chatRoom;
        } else {
            chat.updateChatRoom(findRoom);
            return findRoom;
        }
    }

    public Slice<ChatListDTO> findAll(Long userId, Pageable pageable) {
        Slice<ChatRoom> chatList = chatRoomRepository.findByUser(userId, pageable);
        return chatList.map(c -> new ChatListDTO(c));
    }

    public ChatDTO findByOpponent(Long userId, Long roomId, Pageable pageable) {
        ChatRoom findRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 채팅방"));

        // userId와 받는 유저면 otherId는 보내는 유저
        // userId와 보내는 유저면 otherId는 받는 유저

        Slice<Chat> chatList = chatRepository.findByChatRoom(userId, roomId, pageable);

        Slice<ChatDTO.ChatInfo> chatInfos = chatList.map(ChatDTO.ChatInfo::new);
        return new ChatDTO(findRoom, chatInfos);
    }

    public Long deleteChatRoom(Long userId, Long roomId) {
        chatRoomRepository.findById(roomId)
                .ifPresent(cr -> {
                    if (cr.getReceiver().getId() != userId) {
                        throw new IllegalStateException("삭제 권한 없는 유저");
                    }
                    chatRoomRepository.findById(cr.getPartner_id())
                            .ifPresent(c -> c.updatePartnerId(null));
                });
        chatRoomRepository.deleteById(roomId);
        return roomId;
    }

    public Long saveChatInRoom(Long userId, CreateChatRoomRequest request) {

        ChatRoom findRoom = chatRoomRepository.findById(request.getRoom_id())
                .orElseThrow(() -> new IllegalStateException("room_id 값 오류"));

        Long partnerUserId = findRoom.getSender().getId();

        if (Objects.equals(userId, partnerUserId)) {
            throw new IllegalStateException("자기 자신에게 쪽지를 보낼 수 없습니다");
        }

        User sendUser = userRepository.getById(userId);
        User receiveUser = userRepository.getById(partnerUserId);
        Chat chat1 = new Chat(request.getMessage(), receiveUser, sendUser);
        Chat chat2 = new Chat(request.getMessage(), receiveUser, sendUser);

        chat1.updateChatRoom(findRoom);

        // 삭제된 대화방이면 새로 생성
        ChatRoom chatRoom;
        if (findRoom.getPartner_id() == null) {
            chatRoom = new ChatRoom(receiveUser, sendUser, null);
            chatRoomRepository.save(chatRoom);
        } else {
            chatRoom = chatRoomRepository.findById(findRoom.getPartner_id())
                    .orElseThrow(() -> new IllegalStateException("partner_id 값 오류"));
        }
        findRoom.updatePartnerId(chatRoom.getId());
        chatRoom.updatePartnerId(findRoom.getId());
        chat2.updateChatRoom(chatRoom);

        chatRepository.save(chat1);
        chatRepository.save(chat2);

        return chat1.getId();
    }

}
