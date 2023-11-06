package cool.txz.cool.function.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrencyExchangeRateDTO {

    /**
     * 银行名称
     */
    private String bank;

    /**
     * 货币名称
     */
    private String currencyName;

    /**
     * 货币代码
     */
    private String currencyCode;

    /**
     * 现汇买入
     */
    private BigDecimal forexBuy;

    /**
     * 现钞买入
     */
    private BigDecimal cashBuy;

    /**
     * 现汇卖出
     */
    private BigDecimal forexSell;

    /**
     * 现钞卖出
     */
    private BigDecimal cashSell;

    /**
     * 发布日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    public BigDecimal getForexBuy() {
        return forexBuy == null ? null : forexBuy.divide(new BigDecimal(100), 4, RoundingMode.UP);
    }

    public BigDecimal getCashBuy() {
        return cashBuy == null ? null : cashBuy.divide(new BigDecimal(100), 4, RoundingMode.UP);
    }

    public BigDecimal getForexSell() {
        return forexSell == null ? null : forexSell.divide(new BigDecimal(100), 4, RoundingMode.UP);
    }

    public BigDecimal getCashSell() {
        return cashSell == null ? null : cashSell.divide(new BigDecimal(100), 4, RoundingMode.UP);
    }
}
