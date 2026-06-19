package com.xdev.ooms.documents;

import com.xdev.ooms.documents.form.FormPdfGeneratorService;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.documents.form.mapper.OilReceptionPdfConfigMapper;
import com.xdev.ooms.documents.form.mapper.OliveReceptionPdfConfigMapper;
import com.xdev.ooms.documents.form.mapper.ProductionPdfConfigMapper;
import com.xdev.ooms.documents.form.mapper.QualityControlPdfConfigMapper;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FormDeliveryDocumentService {

    private final DeliveryRepository deliveryRepository;
    private final FormPdfGeneratorService formPdfGeneratorService;
    private final OliveReceptionPdfConfigMapper oliveReceptionPdfConfigMapper;
    private final OilReceptionPdfConfigMapper oilReceptionPdfConfigMapper;
    private final QualityControlPdfConfigMapper qualityControlPdfConfigMapper;
    private final ProductionPdfConfigMapper productionPdfConfigMapper;

    public FormDeliveryDocumentService(
            DeliveryRepository deliveryRepository,
            FormPdfGeneratorService formPdfGeneratorService,
            OliveReceptionPdfConfigMapper oliveReceptionPdfConfigMapper,
            OilReceptionPdfConfigMapper oilReceptionPdfConfigMapper,
            QualityControlPdfConfigMapper qualityControlPdfConfigMapper,
            ProductionPdfConfigMapper productionPdfConfigMapper) {
        this.deliveryRepository = deliveryRepository;
        this.formPdfGeneratorService = formPdfGeneratorService;
        this.oliveReceptionPdfConfigMapper = oliveReceptionPdfConfigMapper;
        this.oilReceptionPdfConfigMapper = oilReceptionPdfConfigMapper;
        this.qualityControlPdfConfigMapper = qualityControlPdfConfigMapper;
        this.productionPdfConfigMapper = productionPdfConfigMapper;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generate(UUID deliveryId, DocumentType type) {
        UnifiedDelivery delivery = deliveryRepository.findByIdForPdf(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + deliveryId));

        FormPdfConfigDto config = switch (type) {
            case RECEPTION -> mapReception(delivery);
            case QUALITY_CONTROL -> qualityControlPdfConfigMapper.map(delivery);
            case PRODUCTION -> productionPdfConfigMapper.map(delivery);
            default -> throw new IllegalArgumentException("Not a form document type: " + type);
        };

        FormPdfDocument document = formPdfGeneratorService.generate(config);
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }

    private FormPdfConfigDto mapReception(UnifiedDelivery delivery) {
        if (delivery.getDeliveryType() == DeliveryType.OIL) {
            return oilReceptionPdfConfigMapper.map(delivery);
        }
        return oliveReceptionPdfConfigMapper.map(delivery);
    }
}
