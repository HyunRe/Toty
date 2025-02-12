package com.toty.user.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EditInfoResponse {
    private Long id;
    private String nickname;
    private List<String> subscriptionAllowed;

}
