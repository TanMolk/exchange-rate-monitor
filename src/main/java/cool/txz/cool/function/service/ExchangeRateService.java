package cool.txz.cool.function.service;

import cool.txz.cool.function.dto.CurrencyExchangeRateDTO;

import java.util.List;

/**
 * @author Molk
 * create at: 2022/3/28 14:51
 * for:
 */
public interface ExchangeRateService {

    /**
     * 抓取所有银行的数据
     */
    List<CurrencyExchangeRateDTO> getTotalRate();

    /**
     * 抓取工商银行的汇率
     */
//    List<CurrencyExchangeRateDTO> getICBCRate();

    /**
     * 抓取中国银行的汇率
     */
    List<CurrencyExchangeRateDTO> getBOCRate();

    /**
     * 抓取招商银行的汇率
     */
    List<CurrencyExchangeRateDTO> getCMBRate();

    /**
     * 抓取农业银行的汇率
     */
    List<CurrencyExchangeRateDTO> getABCRate();

    /**
     * 抓取浦发银行的汇率
     */
    List<CurrencyExchangeRateDTO> getSPDBRate();

    /**
     * 抓取建设银行的汇率
     */
    List<CurrencyExchangeRateDTO> getCCBRate();

    /**
     * 抓取其他没有通用接口银行的汇率
     */
    List<CurrencyExchangeRateDTO> getOtherBankRate();


}
