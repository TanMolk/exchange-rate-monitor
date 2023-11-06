package cool.txz.cool.function.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.NotifyMethodEnum;

public interface CurrencyExchangeRateNotifyService extends IService<CurrencyExchangeRateNotify> {

    Integer clearExpiredAndUnactivatedNotify();

    Integer currencyExchangeNotify();

    Boolean activateAccount(NotifyMethodEnum notifyMethodEnum,String notifyMethod);
}
