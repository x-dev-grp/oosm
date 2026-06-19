package com.xdev.ooms.sharedkernel.ports;

import java.util.Optional;

public interface CompanyProfileReadPort {

    Optional<CompanyProfileSnapshot> findCurrentTenantProfile();
}
