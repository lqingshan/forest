package com.forest.user.auth.service;

import com.forest.user.session.service.LoginRequestContext;
import com.forest.user.user.entity.UserPO;

public interface LoginAccessGuard {
    void check(UserPO user, LoginRequestContext context);
}
