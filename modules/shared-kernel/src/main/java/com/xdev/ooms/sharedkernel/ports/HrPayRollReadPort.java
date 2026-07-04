package com.xdev.ooms.sharedkernel.ports;

import java.util.Optional;
import java.util.UUID;

public interface HrPayRollReadPort {

    Optional<HrPayRollSnapshot> findPayRollForPdf(UUID payrollId);
}
