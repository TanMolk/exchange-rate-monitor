package cool.txz.cool.function.dto;

import lombok.Data;

/**
 * @author Molk
 * create at: 2022/3/21 15:11
 * for:
 */

@Data
public class SubscribeCurrencyNotifyDTO {
    private String mail;
    private String currencyName;
    private String currencyBound;
    private String specialBank;
}
