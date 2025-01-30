package com.toty.sms.application;

import com.toty.following.domain.Following;
import com.toty.following.domain.FollowingRepository;
import com.toty.sms.presentation.request.SmsChatRoomNotificationRequest;
import com.toty.sms.presentation.request.SmsRequest;
import com.toty.sms.presentation.request.PostReplyNotificationRequest;
import com.toty.sms.presentation.request.PostReplyNotificationRequest.Type;
import com.toty.sms.presentation.response.SmsAuthCodeResponse;
import com.toty.sms.presentation.response.SmsResponse;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    final DefaultMessageService messageService;

    private UserRepository userRepository;
    private FollowingRepository followingRepository;

    @Value("${spring.coolsms.api.fromnumber}")
    private String messageFrom;

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecretKey;

    public SmsService(
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize("NCSTN8AA40XJD2G1", "V6JX2HOLWB6PRBUJVEIA2E0YZZUUARBT", "https://api.coolsms.co.kr");
    }
    /*
        특정 수신자에게 메세지 보내기
        todo smsSubscribed 여부 확인
        1. 정보,자유(멘토만 글 쓸 수 있음) 게시글에 좋아요, 댓글 발생 시 작성자에게 -> "..님이 .. 게시글을 좋아합니다.", "..님이 ..게시글에 댓글을 달았습니다."
                                                      -> 누가, 게시판, (like, 댓글) 여부
        2. 질문게시글에 답변 발생 시 작성자에게 -> "님이 ..게시글에 답변을 추가했습니다.
                                          -> 누가, 게시판
        3. 인증번호 전송 -> sms subscribed 안되어있으면 예외처리
     */

    // 인증번호
    public SmsAuthCodeResponse sendAuthCodeMessage(SmsRequest smsRequestDto) {
        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.

        try {
            SmsAuthCodeResponse smsResponseDto = getAuthCodeSmsResponse(smsRequestDto.getPhoneNumber());
            return smsResponseDto;
        } catch (Exception e) {
            log.error("Failed to send AuthCode to number " + smsRequestDto.getPhoneNumber());
            throw new IllegalArgumentException(e);
        }

    }

    public SmsResponse sendAlertMessage(PostReplyNotificationRequest smsRequestDto) {
        User user = validatedSmsUser(smsRequestDto.getId());

        String text;
        // todo msg로 출력
        if (smsRequestDto.getType() == Type.REPLY) {
            text = smsRequestDto.getNickname()+"님이 "+ smsRequestDto.getBoard()+ "게시판 "+ " ["+smsRequestDto.getTitle()+"] "+"에 답변을 추가했습니다.";

        } else { // 좋아요 누른 경우
            text = (smsRequestDto.getNickname()+"님이 "+ smsRequestDto.getBoard()+ "게시판"+ " ["+smsRequestDto.getTitle()+"] "+" 글을 좋아합니다.");
        }
        Message message = createMessage(user.getPhoneNumber(), text);
        try {
            SingleMessageSentResponse response = this.messageService.sendOne(
                    new SingleMessageSendingRequest(message));
            return new SmsResponse(response.getTo(), smsRequestDto.getNickname(), smsRequestDto.getBoard());

        } catch (Exception e) {
            //log.error("Failed to send AuthCode to number " + message.getTo());
            throw new IllegalArgumentException(e);
        }
    }

    @NotNull
    private User validatedSmsUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 전화번호로 등록된 사용자가 없습니다."));

        if (!user.getSubscribeInfo().isSmsSubscribed()) {
            throw new IllegalArgumentException("문자 수신 미동의 회원입니다.");
        }
        if (user.getPhoneNumber() == null) {
            throw new IllegalArgumentException("전화번호 미등록 회원입니다.");
        }
        return user;
    }

    /*
        단체로 메세지 보내기
        멘토 채팅방 개설 시
     */

    public MultipleDetailMessageSentResponse sendChatRoomGroup(SmsChatRoomNotificationRequest request) {
        User mentor = userRepository.findById(request.getMentorId()).orElseThrow(() -> new IllegalArgumentException("해당 전화번호로 등록된 사용자가 없습니다."));
        String mentorNickname = mentor.getNickname();
        String messageText = "멘토 " + mentorNickname + "님이 " + request.getTitle() + " 채팅방을 개설했습니다!";
        List<Following> followerList = followingRepository.findByToUserId(mentor.getId());

        ArrayList<Message> messageList = new ArrayList<>();

        for (Following following : followerList) {
            User follower = validatedSmsUser(following.getFromUser().getId());
            Message message = createMessage(follower.getPhoneNumber(), messageText);
            messageList.add(message);
        }

        try {

            MultipleDetailMessageSentResponse response = this.messageService.send(messageList, false, true);
            System.out.println(response);

            return response;
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    private Message createMessage(String to, String text) {
        Message message = new Message();
        message.setFrom(messageFrom);
        message.setTo(to);
        message.setText(text);
        return message;
    }


    @NotNull
    private SmsAuthCodeResponse getAuthCodeSmsResponse(String phoneNumber) {
        Random rand = new Random();
        String authCode = "";

        for (int i = 0; i < 6; i++) {
            String randomNumber = Integer.toString(rand.nextInt(10));
            authCode += randomNumber;
        }

        String text = ("[Toty] 인증번호[" + authCode + "]를 입력하세요.");
        Message message = createMessage(phoneNumber, text);

        SingleMessageSentResponse response = this.messageService.sendOne(
                new SingleMessageSendingRequest(message));
        System.out.println(response);
        return new SmsAuthCodeResponse(authCode);
    }



    public enum PostCategory {
        QnA("qna"), KNOWLEDGE("Knowledge"), GENERAL("General");

        private String category;

        PostCategory(String category) {
            this.category = category;
        }
    }
}
