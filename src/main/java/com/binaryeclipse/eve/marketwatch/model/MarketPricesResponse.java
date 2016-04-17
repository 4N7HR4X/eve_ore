package com.binaryeclipse.eve.marketwatch.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MarketPricesResponse extends BaseResponse {

    @SerializedName("items")
    public List<MarketItem> items;
}
