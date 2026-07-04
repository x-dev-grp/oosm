package com.xdev.ooms.hr.poste.dto;

import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

public class PosteDto extends BaseDto<Poste> {
    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
