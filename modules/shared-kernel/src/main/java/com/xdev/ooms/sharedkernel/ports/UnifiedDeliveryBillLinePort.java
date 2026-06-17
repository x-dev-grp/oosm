package com.xdev.ooms.sharedkernel.ports;

import java.util.Optional;

public interface UnifiedDeliveryBillLinePort {

    Optional<UnifiedDeliveryBillLineDto> resolve(UnifiedDeliveryBillLineQuery query);
}
