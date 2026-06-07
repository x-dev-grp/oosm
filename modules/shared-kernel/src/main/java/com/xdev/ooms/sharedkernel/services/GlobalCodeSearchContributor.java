package com.xdev.ooms.sharedkernel.services;

import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;

import java.util.Optional;

public interface GlobalCodeSearchContributor {
    Optional<QrResolveResponse> searchByCode(String code);
}
