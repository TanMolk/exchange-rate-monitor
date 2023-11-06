package cool.txz.cool.function.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cool.txz.cool.function.entity.CurrencyCurrentNotifyRate;
import cool.txz.cool.function.entity.CurrencyExchangeRateNotify;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.enums.MailTemplateEnum;
import cool.txz.cool.function.enums.NotifyMethodEnum;
import cool.txz.cool.function.mapper.CurrencyExchangeRateNotifyMapper;
import cool.txz.cool.function.service.CurrencyCurrentNotifyRateService;
import cool.txz.cool.function.service.CurrencyExchangeRateNotifyService;
import cool.txz.cool.function.utils.TxzMailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyExchangeRateNotifyServiceImpl extends ServiceImpl<CurrencyExchangeRateNotifyMapper, CurrencyExchangeRateNotify>
        implements CurrencyExchangeRateNotifyService {

    @Resource
    private CurrencyCurrentNotifyRateService currencyCurrentNotifyRateService;

    @Override
    public Integer clearExpiredAndUnactivatedNotify() {
        return baseMapper.clearExpiredAndUnactivatedNotify();
    }

    @Override
    public Integer currencyExchangeNotify() {
        //查询需要通知的
        Map<CurrencyEnum, List<CurrencyExchangeRateNotify>> notifyMap = list(Wrappers.<CurrencyExchangeRateNotify>lambdaQuery()
                .eq(CurrencyExchangeRateNotify::getIsActivated, Boolean.TRUE)
                .eq(CurrencyExchangeRateNotify::getDeleted, Boolean.FALSE)
                .and(wrapper -> wrapper
                        .le(CurrencyExchangeRateNotify::getLastNotifyAt, LocalDateTime.now().minusMinutes(19))
                        .or(wrapper1 -> wrapper1.isNull(CurrencyExchangeRateNotify::getLastNotifyAt))))
                .stream()
                .collect(Collectors.groupingBy(CurrencyExchangeRateNotify::getNotifyCurrencyCode));

        if (notifyMap.isEmpty()) {
            return 0;
        }

        //查询出数据，并排序分组
        Map<CurrencyEnum, List<CurrencyCurrentNotifyRate>> currencyData = currencyCurrentNotifyRateService.list(
                        Wrappers.<CurrencyCurrentNotifyRate>lambdaQuery()
                                .in(CurrencyCurrentNotifyRate::getCurrencyCode, notifyMap.keySet()))
                .stream()
                .filter(d -> ObjectUtil.isNotNull(d.getSpotSell()))
                .sorted(Comparator.comparing(CurrencyCurrentNotifyRate::getSpotSell))
                .collect(Collectors.groupingBy(CurrencyCurrentNotifyRate::getCurrencyCode));
        //推送数据的缓存
        FIFOCache<String, List<CurrencyCurrentNotifyRate>> cache = CacheUtil.newFIFOCache(200, 60000);

        //推送消息
        List<CurrencyExchangeRateNotify> updateNotifyList = new ArrayList<>();
        for (Map.Entry<CurrencyEnum, List<CurrencyExchangeRateNotify>> entry : notifyMap.entrySet()) {
            CurrencyEnum currency = entry.getKey();
            List<CurrencyExchangeRateNotify> notifyList = entry.getValue();

            for (CurrencyExchangeRateNotify notify : notifyList) {
                String cacheKey = currency.getCode() + "-" + notify.getSpecialBank();
                List<CurrencyCurrentNotifyRate> notifyData = cache.get(cacheKey);
                if (CollectionUtil.isEmpty(notifyData)) {
                    List<CurrencyCurrentNotifyRate> originalData = currencyData.get(currency);
                    notifyData = ListUtil.toList(originalData.subList(0, 3));
                    //不包含特殊银行则查库再放入到缓存中
                    if (StrUtil.isNotBlank(notify.getSpecialBank())
                            && notifyData.stream()
                            .noneMatch(data -> data.getBankName().equals(notify.getSpecialBank()))) {
                        CurrencyCurrentNotifyRate specialRate = currencyCurrentNotifyRateService.getOne(Wrappers.<CurrencyCurrentNotifyRate>lambdaQuery()
                                .eq(CurrencyCurrentNotifyRate::getBankName, notify.getSpecialBank())
                                .eq(CurrencyCurrentNotifyRate::getCurrencyCode, notify.getNotifyCurrencyCode()));
                        if (ObjectUtil.isNotNull(specialRate)) {
                            notifyData.add(specialRate);
                            notifyData.sort(Comparator.comparing(CurrencyCurrentNotifyRate::getSpotSell));
                        }

                    }
                    cache.put(cacheKey, notifyData);
                }

                if (notifyData.isEmpty()) {
                    continue;
                }

                //推送
                BigDecimal maxSpotSellPrice = notifyData.get(notifyData.size() - 1).getSpotSell();
                //是否达到预期
                if (maxSpotSellPrice.compareTo(notify.getRateMax()) <= 0) {
                    TxzMailUtil.send(notify.getNotifyMethod(),
                            notify.getNotifyCurrencyCode().getCurrencyName() + "汇率低价通知",
                            MailTemplateEnum.NORMAL,
                            "https://cdn.txz.cool/image/mail/1.png",
                            "",
                            buildMailContent(notifyData, notify));

                    updateNotifyList.add(CurrencyExchangeRateNotify.builder()
                            .id(notify.getId())
                            .lastNotifyAt(LocalDateTime.now())
                            .build());
                }
            }
        }

        updateBatchById(updateNotifyList);
        return updateNotifyList.size();
    }

    @Override
    public Boolean activateAccount(NotifyMethodEnum notifyMethodEnum, String notifyMethod) {
        return baseMapper.updateToActivatedStatus(notifyMethodEnum, notifyMethod);
    }

    private String buildMailContent(List<CurrencyCurrentNotifyRate> data, CurrencyExchangeRateNotify notifyInfo) {
        String notifySpecialBank = notifyInfo.getSpecialBank();
        boolean hasSpecialBank = StrUtil.isNotEmpty(notifySpecialBank);

        StringBuilder content = new StringBuilder("低价提醒");
        content.append("<br/>");
        content.append("<br/>");
        content.append("<table border='2' style='color:black;font-size: 10px;margin: auto;width: 70%'>");
        content.append("<tr>");
        content.append("<td>银行</td>");
        content.append("<td>汇率</td>");
        content.append("<td>数据时间</td>");
        content.append("</tr>");

        if (hasSpecialBank) {
            //包含特别关注
            Optional<CurrencyCurrentNotifyRate> optional = data.stream()
                    .filter(d -> notifySpecialBank.equals(d.getBankName()))
                    .findAny();

            CurrencyCurrentNotifyRate specialNotify = null;
            if (optional.isPresent()) {
                specialNotify = optional.get();
            }

            int specialNotifyIndex = data.indexOf(specialNotify);
            for (int i = 0; i < data.size(); i++) {
                CurrencyCurrentNotifyRate currencyRate = data.get(i);
                if (i == specialNotifyIndex) {
                    BigDecimal spotSell = currencyRate.getSpotSell();
                    content.append("<tr bgcolor='#9acd32'>")
                            .append("<td>⭐").append(currencyRate.getBankName()).append("</td>")
                            .append("<td>").append(Objects.isNull(spotSell) ? "暂无数据" : spotSell).append("</td>")
                            .append("<td>").append(LocalDateTimeUtil.format(currencyRate.getDataTime(), "HH:mm:ss")).append("</td>")
                            .append("</tr>");
                } else if (i == 0) {
                    content.append("<tr>")
                            .append("<td>\uD83D\uDD25").append(currencyRate.getBankName()).append("</td>")
                            .append("<td>").append(currencyRate.getSpotSell()).append("</td>")
                            .append("<td>").append(LocalDateTimeUtil.format(currencyRate.getDataTime(), "HH:mm:ss")).append("</td>")
                            .append("</tr>");
                } else {
                    content.append("<tr>")
                            .append("<td>").append(currencyRate.getBankName()).append("</td>")
                            .append("<td>").append(currencyRate.getSpotSell()).append("</td>")
                            .append("<td>").append(LocalDateTimeUtil.format(currencyRate.getDataTime(), "HH:mm:ss")).append("</td>")
                            .append("</tr>");
                }
            }
        } else {
            //没有特别关注
            for (int i = 0; i < data.size(); i++) {
                CurrencyCurrentNotifyRate currencyRate = data.get(i);
                if (i == 0) {
                    content.append("<tr>")
                            .append("<td>\uD83D\uDD25️").append(currencyRate.getBankName()).append("</td>")
                            .append("<td>").append(currencyRate.getSpotSell()).append("</td>")
                            .append("<td>").append(LocalDateTimeUtil.format(currencyRate.getDataTime(), "HH:mm:ss")).append("</td>")
                            .append("</tr>");
                } else {
                    content.append("<tr>")
                            .append("<td>").append(currencyRate.getBankName()).append("</td>")
                            .append("<td>").append(currencyRate.getSpotSell()).append("</td>")
                            .append("<td>").append(LocalDateTimeUtil.format(currencyRate.getDataTime(), "HH:mm:ss")).append("</td>")
                            .append("</tr>");
                }
            }
        }
        content.append("</table>");
        content.append("<p style='color: darkgrey;font-size: 10px'>\uD83D\uDD25️代表最优汇率；⭐️ 或绿黄底代表特别关注；点击<span><a href='https://tool.txz.cool/currency-notify/'>这里</a></span>调整提醒</p>");
        return content.toString();
    }
}




