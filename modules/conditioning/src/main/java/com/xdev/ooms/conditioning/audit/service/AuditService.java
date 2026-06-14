package com.xdev.ooms.conditioning.audit.service;

import com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle;
import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.conditioning.label.entity.LabelSource;
import com.xdev.ooms.conditioning.ordrefabrication.entity.LigneOF;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult;
import com.xdev.ooms.conditioning.projet.entity.Client;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.shipping.entity.ShippingEvent;
import com.xdev.ooms.conditioning.shipping.entity.ShippingInfo;
import com.xdev.ooms.conditioning.shipping.entity.ShippingLine;
import com.xdev.ooms.sharedkernel.dtos.AuditDto;
import com.xdev.ooms.conditioning.expedition.entity.Expedition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<AuditDto> getAllAudits() {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<AuditDto> result = new ArrayList<>();
        List<Class<?>> auditedClasses = List.of(
                OrdreFabrication.class,
                LigneOF.class,
                QCPlan.class,
                QCControlPoint.class,
                QCResult.class,
                LabelContent.class,
                LabelSource.class,
                Expedition.class,
                ExpeditionArticle.class,
                Projet.class,
                Client.class,
                ShippingInfo.class,
                ShippingEvent.class,
                ShippingLine.class
        );

        auditedClasses.forEach(clazz -> {
            List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(clazz, false, true)
                    .addProjection(AuditEntity.id())
                    .addProjection(AuditEntity.property("createdBy"))
                    .addProjection(AuditEntity.property("createdDate"))
                    .addProjection(AuditEntity.property("lastModifiedBy"))
                    .addProjection(AuditEntity.property("lastModifiedDate"))
                    .addProjection(AuditEntity.revisionNumber())
                    .addProjection(AuditEntity.revisionType())
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .getResultList();

            revisions.forEach(row -> {
                AuditDto dto = new AuditDto();
                dto.setEntityName(clazz.getSimpleName());
                Object id = row[0];
                dto.setId(id != null ? id.toString() : null);
                dto.setCreatedBy((String) row[1]);
                dto.setCreatedDate((LocalDateTime) row[2]);
                dto.setLastModifiedBy((String) row[3]);
                dto.setLastModifiedDate((LocalDateTime) row[4]);
                Number revNum = (Number) row[5];
                dto.setRevision(revNum != null ? revNum.intValue() : null);
                RevisionType revType = (RevisionType) row[6];
                dto.setRevisionType(revType != null ? revType.name() : null);
                result.add(dto);
            });
        });
        
        result.sort((a, b) -> {
            Integer revA = a.getRevision();
            Integer revB = b.getRevision();
            if (revA == null && revB == null) return 0;
            if (revA == null) return 1;
            if (revB == null) return -1;
            return revB.compareTo(revA);
        });
        
        return result;
    }
}
