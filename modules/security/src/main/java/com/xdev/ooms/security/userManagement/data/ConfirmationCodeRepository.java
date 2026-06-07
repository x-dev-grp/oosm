package com.xdev.ooms.security.userManagement.data;

import com.xdev.ooms.security.userManagement.models.ConfirmationCode;
import com.xdev.ooms.security.userManagement.models.OSMUser;
import com.xdev.ooms.security.userManagement.models.enums.ConfirmationCodeType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationCodeRepository extends BaseRepository<ConfirmationCode> {
    Optional<ConfirmationCode> findByCode(String code);

    Optional<ConfirmationCode> findByConfirmationCodeTypeAndUser(ConfirmationCodeType confirmationCodeType, OSMUser user);
}
