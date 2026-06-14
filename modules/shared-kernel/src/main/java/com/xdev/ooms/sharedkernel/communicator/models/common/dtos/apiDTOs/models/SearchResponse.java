package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;

import java.util.List;
import java.util.Map;

public class SearchResponse<T extends BaseEntity, D extends BaseDto<T>> {
    private long total;
    private List<D> data;
    private int totalPages;
    private int page;
    private Map<String, Double> totals =null;
    public SearchResponse(long total, List<D> data, int totalPages, int page,Map<String, Double> totals) {
        this.total = total;
        this.data = data;
        this.totalPages = totalPages;
        this.page = page;
        this.totals = totals;

    }

    public Map<String, Double> getTotals() {
        return totals;
    }

    public void setTotals(Map<String, Double> totals) {
        this.totals = totals;
    }

    // Getters and setters
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<D> getData() {
        return data;
    }

    public void setData(List<D> data) {
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