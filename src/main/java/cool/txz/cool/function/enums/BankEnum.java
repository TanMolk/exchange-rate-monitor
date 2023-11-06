package cool.txz.cool.function.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Molk
 * create at: 2022/3/28 14:45
 * for:
 */

@Getter
@AllArgsConstructor
public enum BankEnum {

    /**
     * JSON
     */
//    ICBC("工商银行", "http://papi.icbc.com.cn/icbc/iepa/oproxy/rest/nsexchanges/latest"),

    /**
     * HTML
     */
    BOC("中国银行", "https://www.boc.cn/sourcedb/whpj/"),

    /**
     * HTML
     */
    CMB("招商银行", "http://fx.cmbchina.com/hq/"),

    /**
     * Json格式
     */
    ABC("农业银行", "https://ewealth.abchina.com/app/data/api/DataService/ExchangeRateV2"),

    /**
     * HTML
     */
    SPDB("浦发银行", "https://www.spdb.com.cn/was5/web/search?channelid=256931"),

    /**
     * XML
     */
    CCB("建设银行", "http://forex2.ccb.com/cn/home/news/jshckpj_new.xml"),

    CITIC("中信银行", null),
    ;

    public static List<String> getBankNameList() {
        List<String> bankNames = new ArrayList<>();
        for (BankEnum value : values()) {
            bankNames.add(value.getName());
        }
        return bankNames;
    }

    public static List<String> getBankNameListWithOutApi() {
        List<String> bankNames = new ArrayList<>();
        for (BankEnum value : values()) {
            if (StrUtil.isBlank(value.getApi())) {
                bankNames.add(value.getName());
            }
        }
        return bankNames;
    }

    private final String name;
    private final String api;
}
