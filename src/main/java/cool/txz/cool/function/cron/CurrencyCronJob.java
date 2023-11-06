package cool.txz.cool.function.cron;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cool.txz.cool.function.dto.CurrencyExchangeRateDTO;
import cool.txz.cool.function.entity.CurrencyCurrentNotifyRate;
import cool.txz.cool.function.entity.CurrencyExchangeRate;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.service.CurrencyCurrentNotifyRateService;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.service.CurrencyExchangeRateService;
import cool.txz.cool.function.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CurrencyCronJob {

    @Resource
    private ExchangeRateService exchangeRateService;
    @Resource
    private CurrencyExchangeRateNotifyService notifyService;
    @Resource
    private CurrencyExchangeRateService currencyExchangeRateService;
    @Resource
    private CurrencyCurrentNotifyRateService currencyCurrentNotifyRateService;

    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void currencyCrawler() {
        ThreadUtil.sleep(RandomUtil.randomLong(0, 2000));
        long start = System.currentTimeMillis();
        List<CurrencyExchangeRateDTO> dtoList = exchangeRateService.getTotalRate();

        List<CurrencyExchangeRate> currencyList = new ArrayList<>();
        for (CurrencyExchangeRateDTO dto : dtoList) {

            //必须在枚举里存在的货币种类
            CurrencyEnum currencyEnum = CurrencyEnum.getByCode(dto.getCurrencyCode());
            if (ObjectUtil.isNotNull(currencyEnum)) {
                if (currencyExchangeRateService.count(Wrappers.<CurrencyExchangeRate>lambdaQuery()
                        .eq(CurrencyExchangeRate::getBankName, dto.getBank())
                        .eq(CurrencyExchangeRate::getDataTime, dto.getPublishTime())) == 0) {
                    currencyList.add(
                            CurrencyExchangeRate.builder()
                                    .currencyCode(currencyEnum)
                                    .bankName(dto.getBank())
                                    .dataTime(dto.getPublishTime())
                                    .oofBuy(dto.getCashBuy())
                                    .oofSell(dto.getCashSell())
                                    .spotBuy(dto.getForexBuy())
                                    .spotSell(dto.getForexSell())
                                    .build());

                    //更新提醒用的表
                    currencyCurrentNotifyRateService.update(
                            Wrappers.<CurrencyCurrentNotifyRate>lambdaUpdate()
                                    .eq(CurrencyCurrentNotifyRate::getBankName, dto.getBank())
                                    .eq(CurrencyCurrentNotifyRate::getCurrencyCode, dto.getCurrencyCode())
                                    .set(CurrencyCurrentNotifyRate::getSpotBuy, dto.getForexBuy())
                                    .set(CurrencyCurrentNotifyRate::getSpotSell, dto.getForexSell())
                                    .set(CurrencyCurrentNotifyRate::getOofBuy, dto.getCashBuy())
                                    .set(CurrencyCurrentNotifyRate::getOofSell, dto.getCashSell())
                                    .set(CurrencyCurrentNotifyRate::getDataTime, dto.getPublishTime()));
                }
            }
        }
        currencyExchangeRateService.saveBatch(currencyList);
        long end = System.currentTimeMillis();
        log.info("[汇率抓取] 结束。获取了新数据:{}条,耗时:{}ms", currencyList.size(), end - start);

        if (duringTheNotifyTime()) {
            Integer integer = notifyService.currencyExchangeNotify();
            log.info("[汇率通知] {}名用户", integer);
        }
    }

    @Async
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void cleanExpiredAndUnactivatedNotify() {
        log.info("[清除过期未激活数据] {}条", notifyService.clearExpiredAndUnactivatedNotify());
    }

    private Boolean duringTheNotifyTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN.withHour(9));
        LocalDateTime end = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN.withHour(23));
        return now.isBefore(end) && now.isAfter(start);
    }
}
