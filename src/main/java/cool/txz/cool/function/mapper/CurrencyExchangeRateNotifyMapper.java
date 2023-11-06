package cool.txz.cool.function.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.NotifyMethodEnum;
import org.apache.ibatis.annotations.Param;

public interface CurrencyExchangeRateNotifyMapper extends BaseMapper<CurrencyExchangeRateNotify> {
    Integer clearExpiredAndUnactivatedNotify();

    Boolean updateToActivatedStatus(@Param("notifyMethodEnum") NotifyMethodEnum notifyMethodEnum, @Param("notifyMethod") String notifyMethod);
}




