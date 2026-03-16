package com.project.concert.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayClientConfig {

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                AlipayConfig.GATEWAY_URL,
                AlipayConfig.APP_ID,
                AlipayConfig.MERCHANT_PRIVATE_KEY,
                AlipayConfig.FORMAT,
                AlipayConfig.CHARSET,
                AlipayConfig.ALIPAY_PUBLIC_KEY,
                AlipayConfig.SIGN_TYPE
        );
    }
}