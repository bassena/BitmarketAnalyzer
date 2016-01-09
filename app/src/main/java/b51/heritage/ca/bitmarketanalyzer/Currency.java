package b51.heritage.ca.bitmarketanalyzer;

/**
 * Created by mbassett on 12/7/2015.
 */
public class Currency {
    private String code;
    private String symbol;

    public Currency(String c, String s){
        code = c;
        symbol = s;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }
}
