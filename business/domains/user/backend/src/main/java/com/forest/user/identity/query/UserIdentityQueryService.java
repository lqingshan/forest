package com.forest.user.identity.query;

import java.util.Collection;
import java.util.Map;

/**
 * 提供用户身份展示资料查询能力。
 */
public interface UserIdentityQueryService {
    Map<Long, UserIdentityProfile> getProfiles(Collection<Long> userIds);
}
