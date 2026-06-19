package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.production.oilcontainersale.repository.OilContainerSaleRepo;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.sharedkernel.Enum.QualityGrades;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OilSaleBillLineBuilder {

    private final OilContainerSaleRepo containerSaleRepo;

    public OilSaleBillLineBuilder(OilContainerSaleRepo containerSaleRepo) {
        this.containerSaleRepo = containerSaleRepo;
    }

    public List<BillLineDto> buildLines(OilSale sale) {
        List<BillLineDto> lines = new ArrayList<>();
        if (sale == null) {
            return lines;
        }

        BigDecimal quantity = sale.getQuantity() == null ? BigDecimal.ZERO : sale.getQuantity();
        BigDecimal unitPrice = sale.getUnitPrice() == null ? BigDecimal.ZERO : sale.getUnitPrice();
        if (quantity.signum() > 0 && unitPrice.signum() > 0) {
            BillLineDto oilLine = new BillLineDto();
            oilLine.setDesignation(oilDesignation(sale.getQualityGrade()));
            oilLine.setQuantity(quantity.setScale(2, RoundingMode.HALF_UP));
            oilLine.setUnit("L");
            oilLine.setUnitPriceExcludingVat(unitPrice.setScale(3, RoundingMode.HALF_UP));
            oilLine.setVatRatePercent(TunisiaVatDefaults.STANDARD_RATE);
            lines.add(oilLine);
        }

        for (OilContainerSale containerSale : containerSaleRepo.findByOilSaleId(sale.getId())) {
            if (containerSale.getCount() == null || containerSale.getCount() <= 0) {
                continue;
            }
            BillLineDto line = new BillLineDto();
            String containerName = containerSale.getContainer() != null && containerSale.getContainer().getName() != null
                    ? containerSale.getContainer().getName()
                    : "Conteneur huile";
            line.setDesignation(containerName);
            line.setQuantity(BigDecimal.valueOf(containerSale.getCount()));
            line.setUnit("U");
            BigDecimal unitPriceHt = containerSale.getUnitPrice() == null
                    ? BigDecimal.ZERO
                    : containerSale.getUnitPrice().setScale(3, RoundingMode.HALF_UP);
            line.setUnitPriceExcludingVat(unitPriceHt);
            line.setVatRatePercent(TunisiaVatDefaults.STANDARD_RATE);
            lines.add(line);
        }

        if (lines.isEmpty()) {
            BillLineDto fallback = new BillLineDto();
            fallback.setDesignation("Vente huile");
            fallback.setQuantity(BigDecimal.ONE);
            fallback.setUnit("U");
            fallback.setUnitPriceExcludingVat(
                    sale.getTotalAmount() == null ? BigDecimal.ZERO : sale.getTotalAmount().setScale(3, RoundingMode.HALF_UP));
            fallback.setVatRatePercent(TunisiaVatDefaults.STANDARD_RATE);
            lines.add(fallback);
        }
        return lines;
    }

    public String containerSummary(UUID oilSaleId) {
        return containerSaleRepo.findByOilSaleId(oilSaleId).stream()
                .filter(line -> line.getCount() != null && line.getCount() > 0)
                .map(line -> {
                    String name = line.getContainer() != null && line.getContainer().getName() != null
                            ? line.getContainer().getName()
                            : "Conteneur";
                    return line.getCount() + " x " + name;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String oilDesignation(QualityGrades grade) {
        if (grade == null) {
            return "Huile d'olive";
        }
        return switch (grade) {
            case EXTRA_VIRGIN -> "Huile d'olive extra vierge";
            case VIRGIN -> "Huile d'olive vierge";
            case LAMPANTE -> "Huile d'olive lampante";
            case REFINED -> "Huile d'olive raffinée";
            case OTHER -> "Huile d'olive";
            case POMACE -> "Huile de grignon d'olive";
        };
    }
}
