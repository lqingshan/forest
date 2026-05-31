package com.forest.user.user.service.impl;

import com.forest.starter.exception.BusinessException;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.user.platform.service.UserPlatformService;
import com.forest.user.user.avatar.service.UserAvatarService;
import com.forest.user.user.client.service.UserClientService;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.user.service.UserService;
import com.forest.user.user.specification.UserSpecifications;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实现用户查询、绑定与状态管理能力。
 */
@Service
public class UserServiceImpl implements UserService, UserPlatformService, UserClientService {
    private static final String PLATFORM_DEFAULT_NAME = "admin";
    private static final String PLATFORM_ACCOUNT_TYPE = "platform_password";
    private static final String DEFAULT_NAME = "新用户";
    private static final int MIN_CONTACT_KEYWORD_LENGTH = 2;

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final AccountRepository accountRepository;
    private final ObjectProvider<UserAvatarService> userAvatarService;
    private final TransactionTemplate requiresNewTransaction;

    public UserServiceImpl(
        UserRepository userRepository,
        UserAccountRepository userAccountRepository,
        AccountRepository accountRepository,
        ObjectProvider<UserAvatarService> userAvatarService,
        PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.userAccountRepository = userAccountRepository;
        this.accountRepository = accountRepository;
        this.userAvatarService = userAvatarService;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public UserPO getRequiredById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    public UserPO getRequiredActiveById(Long userId) {
        return requireActive(getRequiredById(userId));
    }

    @Override
    public Optional<UserPO> findByAccountId(Long accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return userAccountRepository.findByAccountId(accountId)
            .flatMap(link -> userRepository.findById(link.getUserId()));
    }

    @Override
    public ResolveUserResult findOrCreateByAccountId(Long accountId) {
        if (accountId == null) {
            throw new BusinessException("账号不存在");
        }
        Optional<UserPO> existing = findByAccountId(accountId);
        if (existing.isPresent()) {
            return new ResolveUserResult(requireActive(existing.get()), false);
        }

        boolean created = tryCreateUserBinding(accountId);
        UserPO resolved = findByAccountId(accountId)
            .orElseThrow(() -> new BusinessException("用户绑定失败"));
        return new ResolveUserResult(requireActive(resolved), created);
    }

    @Override
    @Transactional
    public UserPO updateStatus(Long userId, UserPO.Status status) {
        if (status == null) {
            throw new BusinessException("用户状态不能为空");
        }
        UserPO user = getRequiredById(userId);
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserPO ensurePrimaryPhone(Long userId, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("手机号不能为空");
        }
        UserPO user = getRequiredById(userId);
        if (user.getPhone() != null && !user.getPhone().isBlank() && !phone.equals(user.getPhone())) {
            throw new BusinessException("用户手机号绑定冲突");
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            user.setPhone(phone);
            return userRepository.save(user);
        }
        return user;
    }

    @Override
    public Page<UserPO> searchPage(UserPageQuery pageQuery, Pageable pageable) {
        return userRepository.findAll(buildSpecification(pageQuery), pageable);
    }

    @Override
    public List<Long> searchIds(UserPageQuery pageQuery) {
        return userRepository.findAll(buildSpecification(pageQuery)).stream()
            .map(UserPO::getId)
            .toList();
    }

    @Override
    public Map<Long, UserPO> getUserMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(UserPO::getId, Function.identity()));
    }

    @Override
    public Page<UserPO> searchPage(UserPlatformPageQuery pageQuery, Pageable pageable) {
        UserPlatformPageQuery safeQuery = pageQuery == null
            ? new UserPlatformPageQuery(null, null, null, null, null)
            : pageQuery;
        return searchPage(
            new UserPageQuery(safeQuery.id(), safeQuery.name(), safeQuery.phone(), safeQuery.email(), safeQuery.status()),
            pageable
        );
    }

    @Override
    @Transactional
    public UserPO freezeUser(Long targetUserId) {
        UserPO user = getRequiredById(targetUserId);
        if (hasReservedPlatformName(user)) {
            throw new BusinessException("平台用户不能被冻结");
        }
        return updateStatus(targetUserId, UserPO.Status.FROZEN);
    }

    @Override
    @Transactional
    public UserPO activateUser(Long targetUserId) {
        return updateStatus(targetUserId, UserPO.Status.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformProfile getCurrentPlatformProfile(Long userId) {
        UserPO user = getRequiredActiveById(userId);
        return toPlatformProfile(user);
    }

    @Override
    @Transactional
    public PlatformProfile updateCurrentPlatformAvatar(Long userId, String fileNo) {
        UserPO user = requireAvatarService().updateCurrentAvatar(userId, fileNo);
        return toPlatformProfile(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserClientService.UserProfile getCurrentUserProfile(Long userId) {
        UserPO user = getRequiredActiveById(userId);
        return toUserProfile(user);
    }

    @Override
    @Transactional
    public UserClientService.UserProfile updateCurrentAvatar(Long userId, String fileNo) {
        UserPO user = requireAvatarService().updateCurrentAvatar(userId, fileNo);
        return toUserProfile(user);
    }

    private UserClientService.UserProfile toUserProfile(UserPO user) {
        return UserClientService.UserProfile.from(user, avatarFileNo(user), avatarUrl(user));
    }

    private PlatformProfile toPlatformProfile(UserPO user) {
        return PlatformProfile.from(user, platformLoginName(user), avatarFileNo(user), avatarUrl(user));
    }

    private String platformLoginName(UserPO user) {
        return userAccountRepository.findByUserId(user.getId()).stream()
            .map(link -> accountRepository.findById(link.getAccountId()).orElse(null))
            .filter(candidate -> candidate != null)
            .filter(candidate -> PLATFORM_ACCOUNT_TYPE.equals(candidate.getType()))
            .filter(candidate -> AccountPO.GLOBAL_CREDENTIAL_SCOPE.equals(candidate.getCredentialScope()))
            .map(AccountPO::getIdentifier)
            .findFirst()
            .orElseGet(() -> {
                if (user.getPhone() != null && !user.getPhone().isBlank()) {
                    return user.getPhone();
                }
                return user.getName();
            });
    }

    private UserPO requireActive(UserPO user) {
        if (user.getStatus() == UserPO.Status.FROZEN) {
            throw new BusinessException("用户已被冻结");
        }
        if (user.getStatus() == UserPO.Status.DISABLED) {
            throw new BusinessException("用户已被禁用");
        }
        return user;
    }

    private void validateContactKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && keyword.trim().length() < MIN_CONTACT_KEYWORD_LENGTH) {
            throw new BusinessException("手机号或邮箱至少输入 2 个字符");
        }
    }

    private Specification<UserPO> buildSpecification(UserPageQuery pageQuery) {
        UserPageQuery safeQuery = pageQuery == null ? new UserPageQuery(null, null, null, null, null) : pageQuery;
        validateContactKeyword(safeQuery.phone());
        validateContactKeyword(safeQuery.email());
        return Specification
            .where(UserSpecifications.withId(safeQuery.id()))
            .and(UserSpecifications.nameContainsIgnoreCase(safeQuery.name()))
            .and(UserSpecifications.phoneContainsIgnoreCase(safeQuery.phone()))
            .and(UserSpecifications.emailContainsIgnoreCase(safeQuery.email()))
            .and(UserSpecifications.withStatus(safeQuery.status()));
    }

    private boolean hasReservedPlatformName(UserPO user) {
        return user != null && PLATFORM_DEFAULT_NAME.equals(user.getName());
    }

    private String avatarFileNo(UserPO user) {
        UserAvatarService avatarService = userAvatarService.getIfAvailable();
        return avatarService == null ? user.getAvatar() : avatarService.avatarFileNo(user.getAvatar());
    }

    private String avatarUrl(UserPO user) {
        UserAvatarService avatarService = userAvatarService.getIfAvailable();
        return avatarService == null ? null : avatarService.avatarUrl(user.getAvatar());
    }

    private UserAvatarService requireAvatarService() {
        UserAvatarService avatarService = userAvatarService.getIfAvailable();
        if (avatarService == null) {
            throw new BusinessException("用户头像服务未启用");
        }
        return avatarService;
    }

    private boolean tryCreateUserBinding(Long accountId) {
        try {
            return Boolean.TRUE.equals(requiresNewTransaction.execute(status -> {
                UserPO user = new UserPO();
                user.setName(DEFAULT_NAME);
                user.setStatus(UserPO.Status.ACTIVE);
                UserPO savedUser = userRepository.saveAndFlush(user);

                UserAccountPO link = new UserAccountPO();
                link.setUserId(savedUser.getId());
                link.setAccountId(accountId);
                userAccountRepository.saveAndFlush(link);
                return true;
            }));
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }
}
