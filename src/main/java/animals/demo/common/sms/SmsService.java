package animals.demo.common.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.extern.slf4j.Slf4j;
import com.solapi.sdk.message.model.Message;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.sender-phone}")
    private String senderPhone;

    public void sendSms(String to, String code) {
        DefaultMessageService messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);

        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(to);
        message.setText("[AdoptLink] 인증번호 [" + code + "] 를 입력해주세요.");

        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException e) {
            log.error("SMS 발송 실패 - 실패 목록: {}, 메시지: {}", e.getFailedMessageList(), e.getMessage());
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
