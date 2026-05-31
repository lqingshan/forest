package com.forest.user.account.repository;

import com.forest.user.account.entity.AccountPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提供账号的持久化访问能力。
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountPO, Long> {
    Optional<AccountPO> findByTypeAndCredentialScopeAndIdentifier(String type, String credentialScope, String identifier);

    boolean existsByTypeAndCredentialScopeAndIdentifier(String type, String credentialScope, String identifier);

    List<AccountPO> findByTypeAndIdentifierOrderByIdAsc(String type, String identifier);
}
