package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;

public class SearchDetailsType {
    private SearchDetails searchDetailKey;
    private SearchType type;

    public SearchDetails getSearchDetailKey() {
        return searchDetailKey;
    }

    public void setSearchDetailKey(SearchDetails searchDetailKey) {
        this.searchDetailKey = searchDetailKey;
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }
}