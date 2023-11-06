package cool.txz.cool.function.test;

import cn.hutool.core.date.LocalDateTimeUtil;
import cool.txz.cool.function.CurrencyExchangeRateFunctionApplication;
import cool.txz.cool.function.cron.CurrencyCronJob;
import cool.txz.cool.function.entity.CurrencyExchangeRate;
import cool.txz.cool.function.enums.MailTemplateEnum;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.utils.TxzMailUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Molk
 * create at: 2022/3/22 13:23
 * for:
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CurrencyExchangeRateFunctionApplication.class)
public class CronJobTest {

    @Resource
    private CurrencyCronJob cronJob;

    @Resource
    private CurrencyExchangeRateNotifyService notifyService;

    @Test
    public void clearExpiredUnactivatedNotify() {
        System.out.println(notifyService.clearExpiredAndUnactivatedNotify());
    }

    @Test
    public void testMail() {
        List<CurrencyExchangeRate> GBPDataList = new ArrayList<>();
        GBPDataList.add(CurrencyExchangeRate.builder()
                .bankName("1")
                .spotSell(new BigDecimal("1.11"))
                .dataTime(LocalDateTime.now().minusHours(5))
                .build());
        GBPDataList.add(CurrencyExchangeRate.builder()
                .bankName("2")
                .spotSell(new BigDecimal("1.11"))
                .dataTime(LocalDateTime.now().plusHours(20))
                .build());
        GBPDataList.add(CurrencyExchangeRate.builder()
                .bankName("3")
                .spotSell(new BigDecimal("1.11"))
                .dataTime(LocalDateTime.now())
                .build());

        StringBuilder content = new StringBuilder("低价提醒");
        content.append("<br/>");
        content.append("<br/>");
        content.append("<table border='2' style='color:black;font-size: 10px;margin: auto;width: 40%'>");
        content.append("<tr>");
        content.append("<td>银行</td>");
        content.append("<td>汇率</td>");
        content.append("<td>数据时间</td>");
        content.append("</tr>");

//        style='color: black'>

        GBPDataList.forEach(d -> content
                .append("<tr>")
                .append("<td>").append(d.getBankName()).append("</td>")
                .append("<td>").append(d.getSpotSell()).append("</td>")
                .append("<td>").append(LocalDateTimeUtil.format(d.getDataTime(), "HH:mm:ss")).append("</td>")
                .append("</tr>"));
        content.append("</table>");


        TxzMailUtil.send("",
                "英镑汇率低价通知",
                MailTemplateEnum.NORMAL,
                "",
                "",
                content.toString());
    }

    @Test
    public void testCronJob(){
        cronJob.currencyCrawler();
    }
}
