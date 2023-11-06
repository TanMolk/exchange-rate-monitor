package cool.txz.cool.function;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Molk
 * create at: 2022/3/18 22:39
 * for:
 */

@RestController
@EnableScheduling
@SpringBootApplication
@MapperScan("cool.txz.cool.function.mapper")
public class CurrencyExchangeRateFunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyExchangeRateFunctionApplication.class, args);
    }
}
