package cool.txz.cool.function.dto.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Molk
 * create at: 2022/3/30 14:15
 * for:
 */

@Data
public class ABCForexDTO {
    /**
     * 货币名称 货币中文名(货币代码)
     */
    private String CurrName;

    /**
     * 现汇买入
     */
    private BigDecimal BuyingPrice;

    /**
     * 现钞买入
     */
    private BigDecimal CashBuyingPrice;

    /**
     * 现汇、现钞卖出
     */
    private BigDecimal SellPrice;

    /**
     * 数据时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddTHH:mm:ssZ", timezone = "GMT+8")
    private LocalDateTime PublishTime;

    public String getCurrencyCode() {
        return CurrName.split("\\(")[1].replace(")", "");
    }
}
