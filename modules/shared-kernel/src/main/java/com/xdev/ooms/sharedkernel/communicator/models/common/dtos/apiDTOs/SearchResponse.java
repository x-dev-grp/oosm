package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;

import java.util.List;

public class SearchResponse<DTO  extends BaseDto> {
    private long total;
    private List<DTO> data;
    private int totalPages;
    private int page;

    public SearchResponse(long total, List<DTO> data, int totalPages, int page) {
        this.total = total;
        this.data = data;
        this.totalPages = totalPages;
        this.page = page;
    }

    // Getters and setters
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<DTO> getData() {
        return data;
    }

    public void setData(List<DTO> data) {
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}