package cool.txz.cool.function.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum CurrencyEnum {

    GBP("GBP", "英镑", "826", true),
    JPY("JPY", "日元", "392", true),
    HKD("HKD", "港币", "344", true),
    EUR("EUR", "欧元", "978", true),
    USD("USD", "美元", "840", true),
    CAD("CAD", "加拿大元", "124", true),
    NZD("NZD", "新西兰元", "554", true),
    SGD("SGD", "新加坡元", "702", true),
    AUD("AUD", "澳大利亚元", "036", true),


    CHF("CHF", "瑞士法郎", "756", false),
    KZT("KZT", "哈萨克坚戈", "398", false),
    SAR("SAR", "沙特里亚尔", null, false),
    ZAR("ZAR", "南非兰特", "710", false),
    VND("VND", "越南盾", null, false),
    THB("THB", "泰国铢", "764", false),
    KRW("KRW", "韩元", "410", false),
    MNT("MNT", "蒙古图格里克", null, false),
    IDR("IDR", "印尼盾", null, false),
    PHP("PHP", "菲律宾比索", null, false),
    RUB("RUB", "俄罗斯卢布", "643", false),
    AED("AED", "阿联酋迪拉姆", null, false),
    TWD("TWD", "新台币", null, false),
    DKK("DKK", "丹麦克朗", "208", false),
    MYR("MYR", "林吉特", null, false),
    NOK("NOK", "挪威克朗", "578", false),
    MOP("MOP", "澳门元", "446", false),
    PKR("PKR", "巴基斯坦卢比", null, false),
    LAK("LAK", "老挝基普", null, false),
    SEK("SEK", "瑞典克朗", "752", false),
    ;

    private final String code;
    private final String currencyName;

    /**
     * null直接过滤
     */
    private final String CCBCode;

    private final Boolean ifSupport;


    public static CurrencyEnum getByCode(String code) {
        for (CurrencyEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static CurrencyEnum getByName(String name) {
        for (CurrencyEnum value : values()) {
            if (value.currencyName.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static CurrencyEnum getByCCBCode(String CCBCode) {
        for (CurrencyEnum value : values()) {
            if (value.getCCBCode() != null
                    && value.CCBCode.equals(CCBCode)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getSupportCurrencyNameList() {
        List<String> bankNames = new ArrayList<>();
        for (CurrencyEnum value : values()) {
            if (value.getIfSupport()) {
                bankNames.add(value.getCurrencyName());
            }
        }
        return bankNames;
    }
}
