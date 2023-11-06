package cool.txz.cool.function.dto.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Molk
 * create at: 2022/3/31 15:53
 * for:
 */

@Data
public class CCBForexDTO {
    /**
     * 建行货币标识码
     */
    private String Ofrd_Ccy_CcyCd;

    /**
     * 现汇买入
     */
    private BigDecimal BidRateOfCcy;

    /**
     * 现钞买入
     */
    private BigDecimal BidRateOfCash;

    /**
     * 现汇卖出
     */
    private BigDecimal OfrRateOfCcy;

    /**
     * 现钞卖出
     */
    private BigDecimal OfrRateOfCash;

    /**
     * 数据时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HHmmss", timezone = "GMT+8")
    private LocalTime LstPr_Tm;

    /**
     * 数据日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd", timezone = "GMT+8")
    private LocalDate LstPr_Dt;

    public LocalDateTime getPublishTime() {
        return getLstPr_Dt().atTime(LstPr_Tm);
    }

    public BigDecimal getBidRateOfCcy() {
        return BidRateOfCcy.multiply(new BigDecimal(100));
    }

    public BigDecimal getBidRateOfCash() {
        return BidRateOfCash.multiply(new BigDecimal(100));
    }

    public BigDecimal getOfrRateOfCcy() {
        return OfrRateOfCcy.multiply(new BigDecimal(100));
    }

    public BigDecimal getOfrRateOfCash() {
        return OfrRateOfCash.multiply(new BigDecimal(100));
    }
}
