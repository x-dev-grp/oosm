package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;


public class SearchData {
    private Integer page;
    private Integer size;
    private String sort;
    private String order;
    private SearchModel searchData;

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