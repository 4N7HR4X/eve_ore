package com.binaryeclipse.eve.marketwatch;

import com.binaryeclipse.eve.marketwatch.model.*;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Main {

    //    https://public-crest.eveonline.com/market/groups/54/
    public static final int MARKET_GROUP_ORE = 54;
    //    https://public-crest.eveonline.com/market/groups/523/
    public static final int MARKET_GROUP_KERNITE = 523;
    //    https://public-crest.eveonline.com/market/groups/1031/
    public static final int MARKET_GROUP_RAW_MATERIALS = 1031;

    public static final String MARKET_TYPES_URL = "https://public-crest.eveonline.com/market/types/";
    public static final String MARKET_GROUPS_URL = "https://public-crest.eveonline.com/market/groups/";
    public static final String MARKET_TYPES_GROUPED_URL_PATTERN = "%s?group=%s%d/";

    //    https://public-crest.eveonline.com/market/types/?group=https://public-crest.eveonline.com/market/groups/523/
    public static final String MARKET_TYPES_OF_KERNITE_URL = String.format(MARKET_TYPES_GROUPED_URL_PATTERN, MARKET_TYPES_URL, MARKET_GROUPS_URL, MARKET_GROUP_KERNITE);
    //    https://public-crest.eveonline.com/market/types/?group=https://public-crest.eveonline.com/market/groups/54/
    public static final String MARKET_TYPES_OF_ORE_URL = String.format(MARKET_TYPES_GROUPED_URL_PATTERN, MARKET_TYPES_URL, MARKET_GROUPS_URL, MARKET_GROUP_ORE);
    //    https://public-crest.eveonline.com/types/20/
    public static final int TYPE_KERNITE = 20;
    private static List<OreGuy> oreguys = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .get()
                .url("https://public-crest.eveonline.com/market/prices/")
                .build();

        Response execute = client.newCall(request).execute();


        String s = IOUtils.toString(execute.body().byteStream());

        MarketPricesResponse marketPricesResponse = gson.fromJson(s, MarketPricesResponse.class);

        Request marketTypesOfOreRequest = new Request.Builder()
                .get()
                .url(MARKET_TYPES_OF_ORE_URL)
                .build();
        Response marketTypesOfOreResponse = client.newCall(marketTypesOfOreRequest).execute();

        String oreTypesJson = marketTypesOfOreResponse.body().string();
        MarketTypesResponse marketTypesResponse = gson.fromJson(oreTypesJson, MarketTypesResponse.class);

        List<MarketItem> oreItems = getOreMarketItems(marketPricesResponse.items, marketTypesResponse.items);

        for (MarketItem item : oreItems) {
            Request thingumRequest = new Request.Builder()
                    .get()
                    .url(item.type.href)
                    .build();

            Response response = client.newCall(thingumRequest).execute();

            String string = response.body().string();

//            System.out.println(string);
            ItemType itemType = gson.fromJson(string, ItemType.class);

            int beginIndex = string.indexOf("\'>") + 2;
            int endIndex = string.indexOf("</color>");
            String secLevel = string.substring(beginIndex, endIndex);

//            System.out.println(secLevel);

            Float securityLevel = Float.valueOf(secLevel);

            OreGuy guy = new OreGuy(item.type.name, item.type.id, item.averagePrice, itemType.volume, item.averagePrice.setScale(2, BigDecimal.ROUND_UP).divide(itemType.volume, BigDecimal.ROUND_UP), securityLevel);

//                    System.out.format("%s(%d): %.2fISK %.2fm3 ->%.2fISK/m3\n", item.type.name, item.type.id, item.averagePrice, itemType.volume, item.averagePrice.setScale(2, BigDecimal.ROUND_UP).divide(itemType.volume, BigDecimal.ROUND_UP));
            addItem(guy);
        }

        Collections.sort(oreguys, (o1, o2) -> -o1.getSafety().compareTo(o2.getSafety()));

        for (OreGuy oreGuy : oreguys) {
            System.out.format("%s(%.1f):\t->\t%.2f ISK/m3\tsf=%.2f\n", oreGuy.name, oreGuy.securityLevel, oreGuy.pricePerUnitVolume, oreGuy.getSafety());
        }

//        printPrices(oreItems);


    }

    private static synchronized void addItem(OreGuy guy) {
        oreguys.add(guy);
    }

    private static List<MarketItem> getOreMarketItems(List<MarketItem> items, List<MarketTypeItem> oreItems) {
        List<MarketItem> orePrices = new ArrayList<>();
        for (MarketTypeItem item : oreItems) {
            if (item.type.name.contains("Compressed")) {
                continue;
            }
            MarketItem marketItem = findMarketPrice(items, item);
            orePrices.add(marketItem);
        }
        return orePrices;
    }

    private static void printPrices(List<MarketItem> items) {
        for (MarketItem item : items) {
            System.out.format("%s(%d): %.2f\n", item.type.name, item.type.id, item.averagePrice);
        }
    }

    private static MarketItem findMarketPrice(List<MarketItem> items, MarketTypeItem item) {
        for (MarketItem marketItem : items) {
            if (Objects.equals(marketItem.type.id, item.type.id)) {
                return marketItem;
            }
        }
        return null;
    }
}
