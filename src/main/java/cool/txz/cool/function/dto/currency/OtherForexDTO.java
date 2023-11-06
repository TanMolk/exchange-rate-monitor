package cool.txz.cool.function.dto.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Molk
 * create at: 2022/3/31 14:51
 * for:
 */

@Data
public class OtherForexDTO {

    /**
     * 银行名称
     */
    private String bank;

    /**
     * 货币名称
     */
    private String currency;

    /**
     * 货币代码
     */
    private String code;

    /**
     * 现汇买入
     */
    private BigDecimal buyPrice1;

    /**
     * 现钞买入
     */
    private BigDecimal buyPrice2;

    /**
     * 现汇卖出
     */
    private BigDecimal sellPrice1;

    /**
     * 现钞卖出
     */
    private BigDecimal sellPrice2;

    /**
     * 发布日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime releasedate;
}

