package animals.demo.chat.controller;

import animals.demo.chat.dto.ChatRoomListResponseDto;
import animals.demo.chat.dto.CreateChatRoomRequestDto;
import animals.demo.chat.dto.CreateChatRoomResponseDto;
import animals.demo.chat.service.ChatRoomService;
import animals.demo.common.ApiResponse;
import animals.demo.post.dto.CreatePostResponseDto;
import animals.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    //채팅방 생성
    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody CreateChatRoomRequestDto createChatRoomRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        CreateChatRoomResponseDto response = chatRoomService.createChat(userId, createChatRoomRequestDto);

        if (response.isNew()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.created("채팅방이 생성되었습니다.", response));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("기존 채팅방으로 연결되었습니다.", response));
    }

    //채팅방 목록 조회
    @GetMapping
    public ResponseEntity<?> getChatRoom() {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatRoomListResponseDto response = chatRoomService.getChatRoom(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("채팅방 목록 조회에 성공했습니다.", response));
    }

    //채팅방 상세 조회
}
