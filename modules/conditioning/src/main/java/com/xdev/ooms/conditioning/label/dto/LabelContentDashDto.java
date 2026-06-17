package com.xdev.ooms.conditioning.label.dto;

import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.sharedkernel.Enum.LabelCategory;
import com.xdev.ooms.sharedkernel.Enum.LabelContentStatus;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LabelContentDashDto extends BaseDto<LabelContent> {
    private LabelContentStatus status;
    private LabelLanguage language;
    private LocalDate packagingDate;
    private LabelCategory labelCategory;
    private String legalDenomination;
    private String netQuantity;
    private String lotNumber;
    private List<String> certifications = new ArrayList<>();
    private LocalDateTime finalizedAt;
}
