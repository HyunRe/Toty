package com.toty.chatting.application;

import com.toty.chatting.domain.model.User01;
import com.toty.chatting.domain.repository.User01Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class User01Service {

    @Autowired private User01Repository user01Repository;

    public String findUserAndLogin(HttpSession session, long id) {
        User01 loginUser = user01Repository.findById(id).orElse(null);
        if (loginUser != null) {
            session.setAttribute("loginUser", loginUser);
            return loginUser.getUserName();
        }
        return "로그인 실패";
    }

}
