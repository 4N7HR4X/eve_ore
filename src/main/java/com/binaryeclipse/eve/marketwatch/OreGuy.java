package com.binaryeclipse.eve.marketwatch;

import java.math.BigDecimal;

public class OreGuy {
    public String name;
    public Integer id;
    public BigDecimal averagePrice;
    public BigDecimal volume;
    public BigDecimal pricePerUnitVolume;
    public Float securityLevel;

    public OreGuy(String name, Integer id, BigDecimal averagePrice, BigDecimal volume, BigDecimal pricePerUnitVolume, Float securityLevel) {

        this.name = name;
        this.id = id;
        this.averagePrice = averagePrice;
        this.volume = volume;
        this.pricePerUnitVolume = pricePerUnitVolume;
        this.securityLevel = securityLevel;
    }

    public BigDecimal getSafety() {
        return pricePerUnitVolume.multiply(BigDecimal.valueOf(securityLevel.doubleValue()).add(BigDecimal.ONE));
    }
}
