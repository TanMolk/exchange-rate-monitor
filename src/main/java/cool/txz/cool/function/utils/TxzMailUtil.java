package cool.txz.cool.function.utils;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailException;
import cn.hutool.extra.mail.MailUtil;
import cool.txz.cool.function.enums.MailTemplateEnum;

public class TxzMailUtil {
    private static final MailAccount MAIL_ACCOUNT;

    static {
        MAIL_ACCOUNT = new MailAccount();
        MAIL_ACCOUNT.setHost("");
        MAIL_ACCOUNT.setPort(465);
        MAIL_ACCOUNT.setSslEnable(true);
        MAIL_ACCOUNT.setAuth(true);

        MAIL_ACCOUNT.setUser("");
        MAIL_ACCOUNT.setPass("");
        MAIL_ACCOUNT.setFrom("");
    }

    public static boolean send(String to, String subject, MailTemplateEnum mailTemplate, Object... params) {
        if (params.length != mailTemplate.getParamAmount()) {
            throw new MailException("params length should be " + mailTemplate.getParamAmount() + ",but only find " + params.length);
        }
        String content = String.format(mailTemplate.getTemplate(), params);
        MailUtil.send(MAIL_ACCOUNT, to, subject, content, mailTemplate.getIsHtml());
        return true;
    }

    public static boolean send(String to, MailTemplateEnum mailTemplate, Object... params) {
        return send(to, mailTemplate.getSubject(), mailTemplate, params);
    }
}
