package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MemberRepository memberRepository;
  
  public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
    this.chatRoomRepository = chatRoomRepository;
    this.chatParticipantRepository = chatParticipantRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.readStatusRepository = readStatusRepository;
    this.memberRepository = memberRepository;
  }
  
  public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
    
    // 보낸 사람 조회
    Member sender = memberRepository.findByEmail(chatMessageReqDto.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
    
    
    // 메세지 저장
    ChatMessage chatMessage = ChatMessage.builder()
        .chatRoom(chatRoom)
        .member(sender)
        .content(chatMessageReqDto.getMessage())
        .build();
    
    chatMessageRepository.save(chatMessage);
    
    // 사용자별로 읽음 여부를 저장
    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
    for (ChatParticipant c : chatParticipants) {
      ReadStatus readStatus = ReadStatus.builder()
          .chatRoom(chatRoom)
          .member(c.getMember())
          .chatMessage(chatMessage)
          .isRead(c.getMember().equals(sender))
          .build();
      readStatusRepository.save(readStatus);
    }
  }
  
  
  public void createGroupRoom(String chatRoomName) {
    Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
    
    // 채팅방 생성
    ChatRoom chatRoom = ChatRoom.builder()
        .name(chatRoomName)
        .isGroupChat("Y")
        .build();
    
    chatRoomRepository.save(chatRoom);
    
    
    // 채팅 참여자로 개설자를 추가
    ChatParticipant chatParticipant = ChatParticipant.builder()
        .chatRoom(chatRoom)
        .member(member)
        .build();
    
    chatParticipantRepository.save(chatParticipant);
  }
  
  public List<ChatRoomListResDto> getGroupChatRooms() {
    List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
    List<ChatRoomListResDto> dtos = new ArrayList<>();
    for (ChatRoom c : chatRooms) {
      ChatRoomListResDto dto = ChatRoomListResDto.builder()
          .roomId(c.getId())
          .roomName(c.getName())
          .build();
      
      dtos.add(dto);
    }
    
    return dtos;
  }
  
  
  public void addParticipantToGroupChat(Long roomId) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
    
    // member조회
    Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member caanot be found"));
    
    // 이미 참여자인지 검증
    Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
    if (!participant.isPresent()) {
      addParticipantToRoom(chatRoom, member);
    }
    
  }
  
  // ChatParticipant 객체 생성
  public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
    ChatParticipant chatParticipant = ChatParticipant.builder()
        .chatRoom(chatRoom)
        .member(member)
        .build();
    
    chatParticipantRepository.save(chatParticipant);
  }
}














