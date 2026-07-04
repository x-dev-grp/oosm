package com.xdev.ooms.security.internal;

import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyProfileReadPortImpl implements CompanyProfileReadPort {

    private final CompanyProfileRepository companyProfileRepository;

    public CompanyProfileReadPortImpl(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    @Override
    public Optional<CompanyProfileSnapshot> findCurrentTenantProfile() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return Optional.empty();
        }
        List<CompanyProfile> profiles = companyProfileRepository.findActiveByIdOrTenantIdIn(List.of(tenantId));
        if (profiles.isEmpty()) {
            return Optional.empty();
        }
        CompanyProfile profile = profiles.get(0);
        return Optional.of(toSnapshot(profile));
    }

    private CompanyProfileSnapshot toSnapshot(CompanyProfile profile) {
        String address = buildAddress(profile);
        return new CompanyProfileSnapshot(
                nullToEmpty(profile.getLegalName()),
                address,
                nullToEmpty(profile.getTaxId()),
                nullToEmpty(profile.getPhone()),
                nullToEmpty(profile.getWebsite()),
                profile.getLogoData(),
                profile.getLogoContentType(),
                nullToEmpty(profile.getCnssNumber()),
                nullToEmpty(profile.getRegistrationNumber()));
    }

    private String buildAddress(CompanyProfile profile) {
        String line1 = nullToEmpty(profile.getAddressLine1());
        String city = nullToEmpty(profile.getCity());
        String postal = nullToEmpty(profile.getPostalCode());
        StringBuilder sb = new StringBuilder(line1);
        if (!city.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(city);
        }
        if (!postal.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(postal);
        }
        return sb.toString().trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
