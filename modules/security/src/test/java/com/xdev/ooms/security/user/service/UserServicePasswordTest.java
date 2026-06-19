package com.xdev.ooms.security.user.service;

import com.xdev.ooms.security.confirmationcode.entity.ConfirmationCode;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.security.confirmationcode.service.ConfirmationCodeService;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.user.dto.UpdatePasswordDTO;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.mail.templates.OsmMailComposer;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServicePasswordTest {
    private UserRepository userRepository;
    private ConfirmationCodeService confirmationCodeService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        confirmationCodeService = mock(ConfirmationCodeService.class);
        passwordEncoder = new BCryptPasswordEncoder(4);
        userService = new UserService(
                mock(BaseRepository.class),
                new ModelMapper(),
                userRepository,
                mock(MailService.class),
                mock(OsmMailComposer.class),
                confirmationCodeService,
                passwordEncoder,
                mock(RoleRepository.class),
                mock(CompanyProfileRepository.class)
        );
    }

    @Test
    void initialPasswordChangeRequiresTheTemporaryPassword() {
        UUID userId = UUID.randomUUID();
        OSMUser user = new OSMUser();
        user.setId(userId);
        user.setNewUser(true);
        user.setPassword(passwordEncoder.encode("Temporary1!"));
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        UpdatePasswordDTO request = passwordRequest("wrong-password", null, "Replacement1!");

        assertThatThrownBy(() -> userService.updateInitialPassword(request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void initialPasswordChangeClearsNewUserFlag() {
        UUID userId = UUID.randomUUID();
        OSMUser user = new OSMUser();
        user.setId(userId);
        user.setNewUser(true);
        user.setPassword(passwordEncoder.encode("Temporary1!"));
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.updateInitialPassword(
                passwordRequest("Temporary1!", null, "Replacement1!"), userId);

        assertThat(user.isNewUser()).isFalse();
        assertThat(passwordEncoder.matches("Replacement1!", user.getPassword())).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordConsumesAValidResetCode() {
        UUID userId = UUID.randomUUID();
        OSMUser user = new OSMUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode("Current1!"));
        ConfirmationCode resetCode = new ConfirmationCode();
        resetCode.setCode(passwordEncoder.encode("123456"));
        resetCode.setLastModifiedDate(LocalDateTime.now());
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(confirmationCodeService.getByConfirmationCodeTypeAndUser(
                ConfirmationCodeType.RESETPASSWORD, user)).thenReturn(resetCode);

        userService.updatePassword(passwordRequest(null, "123456", "Replacement1!"), userId);

        assertThat(resetCode.isConsumed()).isTrue();
        verify(confirmationCodeService).persist(resetCode);
        verify(userRepository).save(any(OSMUser.class));
    }

    private UpdatePasswordDTO passwordRequest(String oldPassword, String resetCode, String newPassword) {
        UpdatePasswordDTO request = new UpdatePasswordDTO();
        request.setOldPassword(oldPassword);
        request.setResetCode(resetCode);
        request.setNewPassword(newPassword);
        request.setNewPasswordConfirmation(newPassword);
        return request;
    }
}
