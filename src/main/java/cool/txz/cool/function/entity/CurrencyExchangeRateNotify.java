package cool.txz.cool.function.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.enums.NotifyMethodEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrencyExchangeRateNotify implements Serializable {
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
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 通知方式类型
     */
    private NotifyMethodEnum notifyMethodType;

    /**
     * 通知方式
     */
    private String notifyMethod;

    /**
     * 是否激活
     */
    private Boolean isActivated;

    /**
     * 激活时间
     */
    private LocalDateTime activatedAt;

    /**
     * 最晚激活时间-清除数据用途
     */
    private LocalDateTime activatedExpiredAt;

    /**
     * 货币代码
     */
    private CurrencyEnum notifyCurrencyCode;

    /**
     * 通知范围下限
     */
    private BigDecimal rateMin;

    /**
     * 通知范围上限
     */
    private BigDecimal rateMax;

    /**
     * 上次通知时间
     */
    private LocalDateTime lastNotifyAt;

    /**
     * 特别关注银行
     */
    private String specialBank;

    private static final long serialVersionUID = 1L;
}