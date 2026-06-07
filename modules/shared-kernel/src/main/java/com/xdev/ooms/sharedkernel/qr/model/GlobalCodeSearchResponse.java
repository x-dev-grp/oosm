package com.xdev.ooms.sharedkernel.qr.model;

import java.util.ArrayList;
import java.util.List;

public class GlobalCodeSearchResponse {
    private String code;
    private int matchCount;
    private QrResolveResponse result;
    private List<QrResolveResponse> results = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public QrResolveResponse getResult() {
        return result;
    }

    public void setResult(QrResolveResponse result) {
        this.result = result;
    }

    public List<QrResolveResponse> getResults() {
        return results;
    }

    public void setResults(List<QrResolveResponse> results) {
        this.results = results;
    }
}
