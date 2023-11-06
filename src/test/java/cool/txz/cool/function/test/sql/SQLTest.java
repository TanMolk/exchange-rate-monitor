package cool.txz.cool.function.test.sql;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cool.txz.cool.function.CurrencyExchangeRateFunctionApplication;
import cool.txz.cool.function.entity.CurrencyExchangeRate;
import cool.txz.cool.function.enums.BankEnum;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.service.CurrencyExchangeRateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Molk
 * create at: 2022/4/19 13:49
 * for:
 */


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CurrencyExchangeRateFunctionApplication.class)
public class SQLTest {

    @Resource
    private CurrencyExchangeRateService currencyExchangeRateService;

    @Test
    public void testSql() {
        List<CurrencyExchangeRate> list = currencyExchangeRateService.list(Wrappers.<CurrencyExchangeRate>lambdaQuery()
                .eq(CurrencyExchangeRate::getCurrencyCode, CurrencyEnum.GBP)
                .eq(CurrencyExchangeRate::getBankName, BankEnum.CMB.getName())
                .orderByAsc(CurrencyExchangeRate::getDataTime));

        StringBuilder builder = new StringBuilder();
        list.stream()
                .map(d -> LocalDateTimeUtil.formatNormal(d.getDataTime()))
                .forEach(d->builder.append("\"")
                        .append(d)
                        .append("\"")
                        .append(","));
        System.out.println(builder);

        System.out.println(list.stream()
                .map(d -> d.getSpotSell().toString())
                .collect(Collectors.joining(",")));
    }
}
