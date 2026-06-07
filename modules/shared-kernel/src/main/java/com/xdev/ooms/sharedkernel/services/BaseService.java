package com.xdev.ooms.sharedkernel.services;

import com.xdev.ooms.sharedkernel.apiDTOs.SearchResponse;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.models.ExportDetails;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.qr.model.QrCodeInfo;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;

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

    Optional<Revision<Integer, E>> findLastRevisionById(UUID id);


    void resolveEntityRelations(E entity);

    Revisions<Integer, E> findRevisionsById(UUID id);

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
        return actions;
    }




    //------QRCode----//

    QrCodeInfo generateQrInfo(String entityType, UUID entityId);

    byte[] generateQrImage(String publicCode);

    QrResolveResponse resolve(String publicCode);

    Optional<QrResolveResponse> searchByCode(String code);


}


