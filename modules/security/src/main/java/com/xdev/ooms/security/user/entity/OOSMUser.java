package com.xdev.ooms.security.user.entity;

import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationMethod;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;

@Entity
@Table(name = "oosmuser")
public class OOSMUser extends BaseEntity implements UserDetails {
    @Serial
    private static final long serialVersionUID = -7954089139215121063L;

    @Column(unique = true, nullable = false)
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phoneNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
    private boolean isLocked;
    private ConfirmationMethod confirmationMethod;
    private boolean isNewUser;
    @Column(name = "one_signal_player_id")   // nom de la colonne en BDD
    private String oneSignalPlayerId;
    @Column( nullable = false)
    private Boolean enabled = false;

    @Column(columnDefinition = "TEXT")
    private String photoData;

    @Column(length = 50)
    private String photoContentType;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(String.format("%s:%s:%s", permission.getModule().toString().toUpperCase(), permission.getEntity().toUpperCase(), permission.getPermissionName().toUpperCase())))
                .toList();
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public ConfirmationMethod getConfirmationMethod() {
        return confirmationMethod;
    }

    public void setConfirmationMethod(ConfirmationMethod confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getOneSignalPlayerId() {
        return oneSignalPlayerId;
    }

    public void setOneSignalPlayerId(String oneSignalPlayerId) {
        this.oneSignalPlayerId = oneSignalPlayerId;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }
}
