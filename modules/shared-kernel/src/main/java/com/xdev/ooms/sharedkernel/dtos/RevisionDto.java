package com.xdev.ooms.sharedkernel.dtos;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import org.springframework.data.history.RevisionMetadata;

import java.io.Serializable;

public class RevisionDto<E extends BaseEntity> implements Serializable {
    private RevisionMetadata<Integer> revisionMetadata;
    private BaseDto<E> data;

    public RevisionDto() {
    }

    public RevisionDto(RevisionMetadata<Integer> revisionMetadata, BaseDto<E> data) {
        this.revisionMetadata = revisionMetadata;
        this.data = data;
    }

    public RevisionMetadata<Integer> getRevisionMetadata() {
        return revisionMetadata;
    }

    public void setRevisionMetadata(RevisionMetadata<Integer> revisionMetadata) {
        this.revisionMetadata = revisionMetadata;
    }

    public BaseDto<E> getData() {
        return data;
    }

    public void setData(BaseDto<E> data) {
        this.data = data;
    }
}
