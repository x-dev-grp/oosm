package com.xdev.ooms.sharedkernel.models;

import java.util.Set;

public class SearchData {
    private Integer page;
    private Integer size;
    private String sort;
    private String order;
    private SearchModel searchData;
    private boolean filterTenant =true;
    private Set<String> toCalculateTotal=null;

    public Set<String> getToCalculateTotal() {
        return toCalculateTotal;
    }

    public void setToCalculateTotal(Set<String> toCalculateTotal) {
        this.toCalculateTotal = toCalculateTotal;
    }

    public boolean isFilterTenant() {
        return filterTenant;
    }

    public void setFilterTenant(boolean filterTenant) {
        this.filterTenant = filterTenant;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public SearchModel getSearchData() {
        return searchData;
    }

    public void setSearchData(SearchModel searchData) {
        this.searchData = searchData;
    }
}