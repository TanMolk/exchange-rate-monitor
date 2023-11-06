package cool.txz.cool.function.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cool.txz.cool.function.dto.SubscribeCurrencyNotifyDTO;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.BankEnum;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.enums.MailTemplateEnum;
import cool.txz.cool.function.enums.NotifyMethodEnum;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.utils.TxzMailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * @author Molk
 * create at: 2022/3/21 11:39
 * for:
 */

@Slf4j
@Controller
@RequestMapping("/")
public class PageController {

    @Resource
    private CurrencyExchangeRateNotifyService notifyService;

    @GetMapping("/")
    public String index(Model model) {
        SubscribeCurrencyNotifyDTO data = new SubscribeCurrencyNotifyDTO();

        model.addAttribute("data", data);
        model.addAttribute("banks", BankEnum.getBankNameList());
        model.addAttribute("currencies", CurrencyEnum.getSupportCurrencyNameList());
        return "index";
    }

    @PostMapping("/register")
    public String success(@ModelAttribute SubscribeCurrencyNotifyDTO data, Model model) {
        //设置baseURL，避免资源找不到；存疑
        model.addAttribute("baseURL", "/currency-notify");

        try {
            String mail = data.getMail().toLowerCase(Locale.ROOT);
            if (CurrencyEnum.getByName(data.getCurrencyName()) == null) {
                throw new Exception();
            }

            CurrencyExchangeRateNotify existNotify = notifyService.getOne(Wrappers.<CurrencyExchangeRateNotify>lambdaQuery()
                    .eq(CurrencyExchangeRateNotify::getNotifyMethod, mail));

            if (ObjectUtil.isNotNull(existNotify)) {
                updateNotify(data, existNotify);

                model.addAttribute("title", "更新成功");
            } else {
                saveNotify(data, mail);
                model.addAttribute("title", "订阅成功");
            }

            model.addAttribute("subTitle", "实时汇率监控");
            return "success";
        } catch (Exception e) {
            log.warn("", e);
            return "error";
        }
    }

    @GetMapping("/unsubscribe")
    public String unsubscribe(@ModelAttribute SubscribeCurrencyNotifyDTO data, Model model) {
        model.addAttribute("baseURL", "/currency-notify");


        String mail = data.getMail();
        CurrencyExchangeRateNotify existNotify = notifyService.getOne(Wrappers.<CurrencyExchangeRateNotify>lambdaQuery()
                .eq(CurrencyExchangeRateNotify::getNotifyMethod, mail)
                .eq(CurrencyExchangeRateNotify::getIsActivated, Boolean.TRUE));

        if (ObjectUtil.isNull(existNotify)) {
            return "error";
        }
        notifyService.updateById(CurrencyExchangeRateNotify.builder()
                .id(existNotify.getId())
                .isActivated(Boolean.FALSE)
                .deletedAt(LocalDateTime.now())
                .build());

        TxzMailUtil.send(mail, "您已经退订服务", MailTemplateEnum.NORMAL,
                "https://cdn.txz.cool/image/mail/1.png",
                "退订已完成",
                "下次再见");

        model.addAttribute("title", "退订成功");
        model.addAttribute("subTitle", "实时汇率监控");
        return "success";
    }

    @GetMapping("/activate")
    public String active(@RequestParam("id") Long id, Model model) {
        model.addAttribute("baseURL", "/currency-notify/");


        boolean result = notifyService.updateById(CurrencyExchangeRateNotify.builder()
                .id(id)
                .deleted(Boolean.FALSE)
                .deletedAt(null)
                .isActivated(Boolean.TRUE)
                .activatedAt(LocalDateTime.now())
                .build());
        if (!result) {
            return "error";
        }

        model.addAttribute("title", "激活成功");
        model.addAttribute("subTitle", "实时汇率监控");
        return "success";
    }

    private void saveNotify(SubscribeCurrencyNotifyDTO data, String mail) {
        String currencyStr = data.getCurrencyBound();
        BigDecimal currency = BigDecimal.valueOf(Double.parseDouble(currencyStr));
        CurrencyExchangeRateNotify notify = CurrencyExchangeRateNotify.builder()
                .activatedExpiredAt(LocalDateTime.now().plusMinutes(30))
                .notifyCurrencyCode(CurrencyEnum.getByName(data.getCurrencyName()))
                .notifyMethodType(NotifyMethodEnum.MAIL)
                .notifyMethod(mail)
                .rateMin(currency)
                .rateMax(currency)
                .specialBank(data.getSpecialBank())
                .build();

        notifyService.save(notify);
        TxzMailUtil.send(mail, MailTemplateEnum.SUBSCRIBE,
                "https://cdn.txz.cool/image/mail/1.png",
                mail.split("@")[0],
                "英镑汇率",
                "https://tool.txz.cool/currency-notify/activate?id=" + notify.getId());
    }

    private void updateNotify(SubscribeCurrencyNotifyDTO data, CurrencyExchangeRateNotify existNotify) {
        String currencyStr = data.getCurrencyBound();

        CurrencyExchangeRateNotify updateNotify = CurrencyExchangeRateNotify.builder()
                .id(existNotify.getId())
                .notifyCurrencyCode(CurrencyEnum.getByName(data.getCurrencyName()))
                .build();
        if (StrUtil.isNotBlank(data.getSpecialBank())) {
            updateNotify.setSpecialBank(data.getSpecialBank());
        }
        if (StrUtil.isNotBlank(currencyStr)) {
            BigDecimal currency = BigDecimal.valueOf(Double.parseDouble(currencyStr));
            updateNotify.setRateMin(currency);
            updateNotify.setRateMax(currency);
        }

        //如果退订后重新订阅
        if (existNotify.getDeleted()) {
            String mail = existNotify.getNotifyMethod();
            TxzMailUtil.send(mail, MailTemplateEnum.SUBSCRIBE,
                    "https://cdn.txz.cool/image/mail/1.png",
                    mail.split("@")[0],
                    "英镑汇率",
                    "https://tool.txz.cool/currency-notify/activate?id=" + existNotify.getId());

            //更新状态
            notifyService.activateAccount(NotifyMethodEnum.MAIL, existNotify.getNotifyMethod());
        }

        notifyService.updateById(updateNotify);
    }
}
