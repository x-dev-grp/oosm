package com.xdev.ooms.security.confirmationcode.repository;

import com.xdev.ooms.security.confirmationcode.entity.ConfirmationCode;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationCodeRepository extends BaseRepository<ConfirmationCode> {
    Optional<ConfirmationCode> findByCode(String code);

    Optional<ConfirmationCode> findByConfirmationCodeTypeAndUser(ConfirmationCodeType confirmationCodeType, OOSMUser user);
}
