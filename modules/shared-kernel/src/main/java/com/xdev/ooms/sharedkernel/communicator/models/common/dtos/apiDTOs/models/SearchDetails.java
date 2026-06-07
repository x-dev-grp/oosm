package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;

import java.util.ArrayList;
import java.util.List;

public class SearchDetails {
    private Object minValue;
    private Object minValueOrEqual;
    private Object equalValue;
    private Object maxValue;
    private Object maxValueOrEqual;
    private Object likeValue;
    private String lessThanOrEqualTo;
    private String lessThan;
    private String equalTo;
    private String moreThan;
    private String moreThanOrEqualTo;
    private String likeThe;
    private Object containsValue;
    private String contains;
    private List<String> in = new ArrayList<>();
    private List<Object> inValues = new ArrayList<>();
    private Boolean isNull;
    private boolean ignoreIfNull = false;

    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    public Object getMinValueOrEqual() {
        return minValueOrEqual;
    }

    public void setMinValueOrEqual(Object minValueOrEqual) {
        this.minValueOrEqual = minValueOrEqual;
    }

    public Object getEqualValue() {
        return equalValue;
    }

    public void setEqualValue(Object equalValue) {
        this.equalValue = equalValue;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public Object getMaxValueOrEqual() {
        return maxValueOrEqual;
    }

    public void setMaxValueOrEqual(Object maxValueOrEqual) {
        this.maxValueOrEqual = maxValueOrEqual;
    }

    public Object getLikeValue() {
        return likeValue;
    }

    public void setLikeValue(Object likeValue) {
        this.likeValue = likeValue;
    }

    public String getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }

    public void setLessThanOrEqualTo(String lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public String getLessThan() {
        return lessThan;
    }

    public void setLessThan(String lessThan) {
        this.lessThan = lessThan;
    }

    public String getEqualTo() {
        return equalTo;
    }

    public void setEqualTo(String equalTo) {
        this.equalTo = equalTo;
    }

    public String getMoreThan() {
        return moreThan;
    }

    public void setMoreThan(String moreThan) {
        this.moreThan = moreThan;
    }

    public String getMoreThanOrEqualTo() {
        return moreThanOrEqualTo;
    }

    public void setMoreThanOrEqualTo(String moreThanOrEqualTo) {
        this.moreThanOrEqualTo = moreThanOrEqualTo;
    }

    public String getLikeThe() {
        return likeThe;
    }

    public void setLikeThe(String likeThe) {
        this.likeThe = likeThe;
    }

    public Object getContainsValue() {
        return containsValue;
    }

    public void setContainsValue(Object containsValue) {
        this.containsValue = containsValue;
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = contains;
    }

    public List<String> getIn() {
        return in;
    }

    public void setIn(List<String> in) {
        this.in = in;
    }

    public List<Object> getInValues() {
        return inValues;
    }

    public void setInValues(List<Object> inValues) {
        this.inValues = inValues;
    }

    public Boolean getNull() {
        return isNull;
    }

    public void setNull(Boolean aNull) {
        isNull = aNull;
    }

    public boolean isIgnoreIfNull() {
        return ignoreIfNull;
    }

    public void setIgnoreIfNull(boolean ignoreIfNull) {
        this.ignoreIfNull = ignoreIfNull;
    }
}