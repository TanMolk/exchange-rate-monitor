package cool.txz.cool.function.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import cool.txz.cool.function.enums.CurrencyEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class CurrencyExchangeRate {

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 该条数据创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 该条数据上次更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 货币代码
     */
    private CurrencyEnum currencyCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 现汇买入
     */
    private BigDecimal spotBuy;

    /**
     * 现钞买入
     */
    private BigDecimal oofBuy;

    /**
     * 现汇卖出
     */
    private BigDecimal spotSell;

    /**
     * 现钞卖出
     */
    private BigDecimal oofSell;

    /**
     * 数据日期
     */
    private LocalDateTime dataTime;

}
