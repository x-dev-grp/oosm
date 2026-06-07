package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchModel {
    private Map<String, SearchDetails> search = new HashMap<>();
    private List<SearchModel> searchs = new ArrayList<>();
    private SearchOperation operation = SearchOperation.AND;
    private boolean reverse = false;

    public Map<String, SearchDetails> getSearch() {
        return search;
    }

    public void setSearch(Map<String, SearchDetails> search) {
        this.search = search;
    }

    public List<SearchModel> getSearchs() {
        return searchs;
    }

    public void setSearchs(List<SearchModel> searchs) {
        this.searchs = searchs;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
}