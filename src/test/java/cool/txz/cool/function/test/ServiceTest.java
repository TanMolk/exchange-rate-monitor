package cool.txz.cool.function.test;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cool.txz.cool.function.CurrencyExchangeRateFunctionApplication;
import cool.txz.cool.function.dto.CurrencyExchangeRateDTO;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.enums.NotifyMethodEnum;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.service.ExchangeRateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Molk
 * create at: 2022/3/28 16:26
 * for:
 */


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CurrencyExchangeRateFunctionApplication.class)
public class ServiceTest {

    @Resource
    private ExchangeRateService exchangeRateService;

    @Resource
    private CurrencyExchangeRateNotifyService notifyService;

    @Resource
    private CurrencyExchangeRateNotifyService currencyExchangeRateNotifyService;

    @Test
    public void testExchangeRateService() {
        List<CurrencyExchangeRateDTO> totalRate = exchangeRateService.getCMBRate();
        for (CurrencyExchangeRateDTO currencyExchangeRateDTO : totalRate) {
            System.out.println(currencyExchangeRateDTO);
        }

        System.out.println(totalRate.size());

    }

    @Test
    public void testNotifyService() {
        System.out.println(currencyExchangeRateNotifyService.list().size());
    }

    @Test
    public void testList() {
        //查询需要通知的
        Map<CurrencyEnum, List<CurrencyExchangeRateNotify>> notifyMap = currencyExchangeRateNotifyService.list(Wrappers.<CurrencyExchangeRateNotify>lambdaQuery()
                        .eq(CurrencyExchangeRateNotify::getIsActivated, Boolean.TRUE)
                        .and(wrapper -> wrapper
                                .le(CurrencyExchangeRateNotify::getLastNotifyAt, LocalDateTime.now().minusMinutes(19))
                                .or(wrapper1 -> wrapper1.isNull(CurrencyExchangeRateNotify::getLastNotifyAt))))
                .stream()
                .collect(Collectors.groupingBy(CurrencyExchangeRateNotify::getNotifyCurrencyCode));
    }

    @Test
    public void testDelete() {
        Assert.assertTrue(
                notifyService.activateAccount(NotifyMethodEnum.MAIL, "123@1asdasc2.com"));

//        CurrencyExchangeRateNotify.builder()
//                .deleted(Boolean.FALSE)
//                .deletedAt(null)
//                .isActivated(Boolean.TRUE)
//                .activatedAt(LocalDateTime.now())
//                .build()
    }
}
