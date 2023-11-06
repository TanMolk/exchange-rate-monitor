package cool.txz.cool.function.dto.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Molk
 * create at: 2022/3/30 14:15
 * for:
 */

@Data
public class ICBCForexDTO {
    /**
     * 银行名称
     */
    private String bank;

    /**
     * 货币名称
     */
    private String currencyCHName;

    /**
     * 货币代码
     */
    private String currencyENName;

    /**
     * 现汇买入
     */
    private BigDecimal foreignBuy;

    /**
     * 现钞买入
     */
    private BigDecimal cashBuy;

    /**
     * 现汇卖出
     */
    private BigDecimal foreignSell;

    /**
     * 现钞卖出
     */
    private BigDecimal cashSell;

    /**
     * 发布时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "GMT+8")
    private LocalTime publishTime;

    /**
     * 发布日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate publishDate;
}
