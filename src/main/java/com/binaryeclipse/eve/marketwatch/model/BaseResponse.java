package com.binaryeclipse.eve.marketwatch.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("totalCount_str")
    public String totalCountString;
    public Integer pageCount;
    public String pageCount_str;

}
