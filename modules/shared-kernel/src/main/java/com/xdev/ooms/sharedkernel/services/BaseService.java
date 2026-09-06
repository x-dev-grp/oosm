package com.xdev.ooms.sharedkernel.services;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.SearchResponse;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.models.ExportDetails;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.qr.model.QrCodeInfo;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import org.springframework.data.domain.Page;

import java.util.*;

public interface BaseService<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> {
    Class<E> getEntityClass();

    Class<INDTO> getInDTOClass();

    Class<OUTDTO> getOutDTOClass();

    OUTDTO findById(UUID id);

    List<OUTDTO> findAll();

    Page<OUTDTO> findAll(int page, int size, String sort, String direction);

    OUTDTO save(INDTO request);

    List<OUTDTO> save(List<INDTO> request);

    OUTDTO update(INDTO entity);

    void remove(UUID id);

    OUTDTO delete(UUID entity);

    void removeAll(Collection<INDTO> entities);


    void resolveEntityRelations(E entity);


    SearchResponse<E, OUTDTO> search(SearchData searchData);

    byte[] exportToPdf(ExportDetails exportDetails);

    byte[] exportToCsv(ExportDetails exportDetails);

    byte[] exportToExcel(ExportDetails exportDetails);

    default Set<Action> actionsMapping(E entity) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.CREATE);
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.REGENERATE_QR);
        return actions;
    }


    //------QRCode----//

    QrCodeInfo generateQrInfo(String entityType, UUID entityId);

    default QrCodeInfo generateQrInfo(String entityType, UUID entityId, boolean forceRegenerate) {
        return generateQrInfo(entityType, entityId);
    }

    byte[] generateQrImage(String publicCode);

    QrResolveResponse resolve(String publicCode);

    Optional<QrResolveResponse> searchByCode(String code);


}


