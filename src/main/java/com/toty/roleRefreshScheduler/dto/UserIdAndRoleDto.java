package com.toty.roleRefreshScheduler.dto;

import com.toty.user.domain.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserIdAndRoleDto {

    private final Long id;
    private final Role role;

}
