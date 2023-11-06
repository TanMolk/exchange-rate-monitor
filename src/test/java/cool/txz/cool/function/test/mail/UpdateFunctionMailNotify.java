package cool.txz.cool.function.test.mail;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cool.txz.cool.function.CurrencyExchangeRateFunctionApplication;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.MailTemplateEnum;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.utils.TxzMailUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CurrencyExchangeRateFunctionApplication.class)
public class UpdateFunctionMailNotify {

    @Resource
    private CurrencyExchangeRateNotifyService notifyService;

    @Test
    public void sendUpdateMsg() {
        List<String> mailList = notifyService.list(Wrappers.<CurrencyExchangeRateNotify>lambdaQuery()
                        .eq(CurrencyExchangeRateNotify::getIsActivated, Boolean.TRUE)
                        .eq(CurrencyExchangeRateNotify::getDeleted, Boolean.FALSE))
                .stream()
                .map(CurrencyExchangeRateNotify::getNotifyMethod).collect(Collectors.toList());
        System.out.println(mailList);


        String[] updateContent = {
                "因为工商银行关闭了个人外汇业务，现移除了对工商银行的数据支持",
        };

        StringBuilder content = new StringBuilder("更新汇总");
        content.append("<div style='color: darkgrey; font-size: 10px;text-align:left'>");
        for (int i = 0; i < updateContent.length; i++) {
            content.append("<li>");
            content.append(i + 1).append(".").append(updateContent[i]);
            content.append("</li>");

        }
//        content.append("<p style='text-align:center'>");
//        content.append("感谢 @小鹿爱吃多纳滋 等小伙伴 提供的优化建议");
//        content.append("</p>");
//        content.append("<p style='text-align:center'>");
//        content.append("下一次的版本更新后，本项目会停止功能更新。目前本项目处于最后需求收集阶段，有需求的小伙伴可以直接在小红书私信我，或者邮件至zai@txz.cool提出呀！");
//        content.append("</p>");
//        content.append("</div>");

        for (String mail : mailList) {
            TxzMailUtil.send(mail,
                    "【汇率通知】紧急调整",
                    MailTemplateEnum.NORMAL,
                    "https://cdn.txz.cool/image/mail/1.png",
                    "",
                    content.toString());
        }
    }
}
