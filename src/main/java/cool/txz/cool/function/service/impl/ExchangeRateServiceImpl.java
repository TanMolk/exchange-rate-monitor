package cool.txz.cool.function.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cool.txz.cool.function.dto.CurrencyExchangeRateDTO;
import cool.txz.cool.function.dto.currency.ABCForexDTO;
import cool.txz.cool.function.dto.currency.CCBForexDTO;
import cool.txz.cool.function.dto.currency.OtherForexDTO;
import cool.txz.cool.function.enums.BankEnum;
import cool.txz.cool.function.enums.CurrencyEnum;
import cool.txz.cool.function.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Molk
 * create at: 2022/3/28 14:53
 * for:
 */

@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ThreadPoolExecutor RATE_THREAD_POOL = new ThreadPoolExecutor(
            1, 7,
            300, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(35),
            ThreadUtil.newNamedThreadFactory("rate-data", false));

    @Override
    public List<CurrencyExchangeRateDTO> getTotalRate() {
        long start = System.currentTimeMillis();
        int taskAmount = 7;

        CountDownLatch countDownLatch = new CountDownLatch(taskAmount);
        List<CurrencyExchangeRateDTO> result = new CopyOnWriteArrayList<>();
        for (int i = 0; i < taskAmount; i++) {
            RATE_THREAD_POOL.execute(() -> {
                try {
                    switch ((int) countDownLatch.getCount() - 1) {
                        case 0:
//                            result.addAll(getICBCRate());
                            break;
                        case 1:
                            result.addAll(getABCRate());
                            break;
                        case 2:
                            result.addAll(getBOCRate());
                            break;
                        case 3:
                            result.addAll(getCMBRate());
                            break;
                        case 4:
                            result.addAll(getSPDBRate());
                            break;
                        case 5:
                            result.addAll(getCCBRate());
                            break;
                        case 6:
                            result.addAll(getOtherBankRate());
                            break;
                    }
                    countDownLatch.countDown();
                } catch (Exception e) {
                    log.error("[获取所有汇率数据] 失败", e);
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("[获取所有汇率数据] 失败", e);
        }
        long end = System.currentTimeMillis();
        log.info("[获取所有汇率数据] 共{}条 ,耗时{}ms", result.size(), end - start);
        return result;
    }

//    @Override
//    public List<CurrencyExchangeRateDTO> getICBCRate() {
//        String json = HttpUtil.get(BankEnum.ICBC.getApi());
//        List<ICBCForexDTO> data = JSONUtil.toList(JSONUtil.parseObj(json).getJSONArray("data"), ICBCForexDTO.class);
//
//        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
//        data.forEach(d -> {
//            if (CurrencyEnum.getByCode(d.getCurrencyENName()) != null) {
//                result.add(CurrencyExchangeRateDTO.builder()
//                        .bank(BankEnum.ICBC.getName())
//                        .currencyCode(d.getCurrencyENName())
//                        .currencyName(d.getCurrencyCHName())
//                        .cashBuy(d.getCashBuy())
//                        .cashSell(d.getCashSell())
//                        .forexBuy(d.getForeignBuy())
//                        .forexSell(d.getForeignSell())
//                        .publishTime(d.getPublishDate().atTime(d.getPublishTime()))
//                        .build());
//            }
//        });
//        log.info("[汇率接口] 获取{}数据 {}条", BankEnum.ICBC.getName(), result.size());
//        return result;
//    }

    @Override
    public List<CurrencyExchangeRateDTO> getBOCRate() {
        return getDataInTableFromHtml(
                BankEnum.BOC,
                1,
                0,
                1,
                2,
                3,
                4,
                6,
                d -> LocalDateTimeUtil.parse(d, "yyyy.MM.dd HH:mm:ss"));
    }

    @Override
    public List<CurrencyExchangeRateDTO> getCMBRate() {
        return getDataInTableFromHtml(
                BankEnum.CMB,
                1,
                0,
                5,
                6,
                3,
                4,
                7,
                d -> {
                    if (d.length() < 8) {
                        d = "0" + d;
                    }
                    return LocalDate.now().atTime(LocalTime.parse(d, DateTimeFormatter.ofPattern("HH:mm:ss")));
                });
    }

    @Override
    public List<CurrencyExchangeRateDTO> getABCRate() {
        String json = HttpUtil.get(BankEnum.ABC.getApi());
        JSONArray jsonArray = JSONUtil.parseObj(json)
                .getJSONObject("Data")
                .getJSONArray("Table");

        List<ABCForexDTO> dataItems = JSONUtil.toList(jsonArray, ABCForexDTO.class);

        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
        for (ABCForexDTO dto : dataItems) {
            CurrencyEnum currencyEnum = CurrencyEnum.getByCode(dto.getCurrencyCode());
            if (currencyEnum != null) {
                result.add(CurrencyExchangeRateDTO
                        .builder()
                        .bank(BankEnum.ABC.getName())
                        .currencyCode(currencyEnum.getCode())
                        .currencyName(currencyEnum.getCurrencyName())
                        .forexBuy(dto.getBuyingPrice())
                        .cashBuy(dto.getCashBuyingPrice())
                        .forexSell(dto.getSellPrice())
                        .cashSell(dto.getSellPrice())
                        .publishTime(dto.getPublishTime())
                        .build());
            }
        }
        log.info("[汇率接口] 获取{}数据 {}条", BankEnum.ABC.getName(), result.size());
        return result;
    }

    @Override
    public List<CurrencyExchangeRateDTO> getSPDBRate() {
        String html = HttpUtil.get(BankEnum.SPDB.getApi());
        Document document = Jsoup.parse(html);
        Elements trs = document
                .getElementsByTag("table")
                .get(0)
                .getElementsByTag("tbody");

        LocalDateTime dataTime = LocalDateTimeUtil.parse(document
                .getElementsByClass("fine_title")
                .get(0)
                .getElementsByTag("p")
                .html(), "yyyy.MM.dd HH:mm:ss");

        //去除表头、过滤货币枚举中存在的货币
        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
        trs.forEach(b -> {
            Elements tds = b.getElementsByTag("tr").get(0).getElementsByTag("td");
            String currencyCode = tds.get(0).html().split(" ")[1];
            CurrencyEnum currencyEnum = CurrencyEnum.getByCode(currencyCode);
            if (currencyEnum != null) {
                String forexBuy = tds.get(2).html();
                String cashBuy = tds.get(3).html();
                String forexSell = tds.get(4).html();
                String cashSell = tds.get(4).html();
                CurrencyExchangeRateDTO dto = CurrencyExchangeRateDTO
                        .builder()
                        .bank(BankEnum.SPDB.getName())
                        .currencyCode(currencyEnum.getCode())
                        .currencyName(currencyEnum.getCurrencyName())
                        .forexBuy(StrUtil.isBlank(forexBuy) ? null : new BigDecimal(forexBuy))
                        .cashBuy(StrUtil.isBlank(cashBuy) ? null : new BigDecimal(cashBuy))
                        .forexSell(StrUtil.isBlank(forexSell) ? null : new BigDecimal(forexSell))
                        .cashSell(StrUtil.isBlank(cashSell) ? null : new BigDecimal(cashSell))
                        .publishTime(dataTime)
                        .build();
                if (CurrencyEnum.JPY.equals(currencyEnum)) {
                    dto.setCashBuy(dto.getCashBuy().divide(new BigDecimal(10), 4, RoundingMode.UP));
                    dto.setForexBuy(dto.getForexBuy().divide(new BigDecimal(10), 4, RoundingMode.UP));
                    dto.setCashSell(dto.getCashSell().divide(new BigDecimal(10), 4, RoundingMode.UP));
                    dto.setForexSell(dto.getForexSell().divide(new BigDecimal(10), 4, RoundingMode.UP));
                }

                result.add(dto);
            }
        });

        log.info("[汇率接口] 获取{}数据 {}条", BankEnum.ABC.getName(), result.size());
        return result;
    }

    @Override
    public List<CurrencyExchangeRateDTO> getCCBRate() {
        String xml = HttpUtil.get(BankEnum.CCB.getApi());
        JSONArray jsonArray = JSONUtil.parseFromXml(xml)
                .getJSONObject("ReferencePriceSettlements")
                .getJSONArray("ReferencePriceSettlement");
        List<CCBForexDTO> dtoList = JSONUtil.toList(jsonArray, CCBForexDTO.class);

        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
        dtoList.forEach(dto -> {
            CurrencyEnum currencyEnum = CurrencyEnum.getByCCBCode(dto.getOfrd_Ccy_CcyCd());
            if (currencyEnum != null) {
                result.add(CurrencyExchangeRateDTO
                        .builder()
                        .bank(BankEnum.CCB.getName())
                        .currencyCode(currencyEnum.getCode())
                        .currencyName(currencyEnum.getCurrencyName())
                        .forexBuy(dto.getBidRateOfCcy())
                        .cashBuy(dto.getBidRateOfCash())
                        .forexSell(dto.getOfrRateOfCcy())
                        .cashSell(dto.getOfrRateOfCash())
                        .publishTime(dto.getPublishTime())
                        .build());
            }
        });
        log.info("[汇率接口] 获取{}数据 {}条", BankEnum.CCB.getName(), result.size());
        return result;
    }

    @Override
    public List<CurrencyExchangeRateDTO> getOtherBankRate() {
        String data = HttpUtil.get("http://data.bank.hexun.com/other/cms/foreignexchangejson.ashx?callback=ShowDatalist");
        //预处理数据，处理成json格式字符串
        data = data.replace("ShowDatalist(", "");
        data = data.substring(0, data.length() - 1);

        List<OtherForexDTO> dtoList = JSONUtil.toList(data, OtherForexDTO.class);
        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
        //过滤出没有api的的银行数据
        List<String> filterBankNames = BankEnum.getBankNameListWithOutApi();
        dtoList.stream()
                .filter(dto -> filterBankNames.contains(dto.getBank()))
                .forEach(dto -> result.add(CurrencyExchangeRateDTO
                        .builder()
                        .bank(dto.getBank())
                        .currencyCode(dto.getCode())
                        .currencyName(dto.getCurrency())
                        .forexBuy(dto.getBuyPrice1())
                        .cashBuy(dto.getBuyPrice2())
                        .forexSell(dto.getSellPrice1())
                        .cashSell(dto.getSellPrice2())
                        .publishTime(dto.getReleasedate())
                        .build()));
        log.info("[汇率接口] 获取{}数据 共{}条", filterBankNames, result.size());
        return result;
    }

    private List<CurrencyExchangeRateDTO> getDataInTableFromHtml(BankEnum bankEnum,
                                                                 Integer tablePosition,
                                                                 Integer currencyCodePosition,
                                                                 Integer forexBuyPosition,
                                                                 Integer cashBuyPosition,
                                                                 Integer forexSellPosition,
                                                                 Integer cashSellPosition,
                                                                 Integer dataTimePosition,
                                                                 TransStringToLocalDateTimeFunction transStringToLocalDateTimeFunction) {

        String html = HttpUtil.get(bankEnum.getApi());

        Elements trs = Jsoup.parse(html)
                .getElementsByTag("table")
                .get(tablePosition)
                .getElementsByTag("tbody")
                .get(0)
                .getElementsByTag("tr");

        //去除表头、过滤货币枚举中存在的货币
        List<CurrencyExchangeRateDTO> result = new ArrayList<>();
        trs.subList(1, trs.size() - 1).forEach(tr -> {
            CurrencyExchangeRateDTO data = buildCurrencyExchangeDTO(bankEnum,
                    currencyCodePosition,
                    forexBuyPosition,
                    cashBuyPosition,
                    forexSellPosition,
                    cashSellPosition,
                    dataTimePosition,
                    transStringToLocalDateTimeFunction,
                    tr);
            if (data != null) {
                result.add(data);
            }

        });
        log.info("[汇率接口] 获取{}数据 {}条", bankEnum.getName(), result.size());
        return result;
    }

    private CurrencyExchangeRateDTO buildCurrencyExchangeDTO(BankEnum bankEnum,
                                                             Integer currencyNamePosition,
                                                             Integer forexBuyPosition,
                                                             Integer cashBuyPosition,
                                                             Integer forexSellPosition,
                                                             Integer cashSellPosition,
                                                             Integer dataTimePosition,
                                                             TransStringToLocalDateTimeFunction transStringToLocalDateTimeFunction,
                                                             Element tr) {
        Elements tds = tr.getElementsByTag("td");
        CurrencyEnum currencyEnum = CurrencyEnum.getByName(tds.get(currencyNamePosition).html());
        if (currencyEnum != null) {
            String forexBuy = tds.get(forexBuyPosition).html();
            String cashBuy = tds.get(cashBuyPosition).html();
            String forexSell = tds.get(forexSellPosition).html();
            String cashSell = tds.get(cashSellPosition).html();
            return CurrencyExchangeRateDTO
                    .builder()
                    .bank(bankEnum.getName())
                    .currencyCode(currencyEnum.getCode())
                    .currencyName(currencyEnum.getCurrencyName())
                    .forexBuy(StrUtil.isBlank(forexBuy) ? null : new BigDecimal(forexBuy))
                    .cashBuy(StrUtil.isBlank(cashBuy) ? null : new BigDecimal(cashBuy))
                    .forexSell(StrUtil.isBlank(forexSell) ? null : new BigDecimal(forexSell))
                    .cashSell(StrUtil.isBlank(cashSell) ? null : new BigDecimal(cashSell))
                    .publishTime(transStringToLocalDateTimeFunction.trans(tds.get(dataTimePosition).html()))
                    .build();
        }
        return null;
    }

    private interface TransStringToLocalDateTimeFunction {

        LocalDateTime trans(String dateTimeString);

    }
}
