package com.gv.csc.config;

import com.gv.csc.helper.CLOSETHelper;
import com.gv.csc.helper.PLMHelper;
import com.gv.csc.helper.RestService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class ModelConfig {
    @Bean
    public CLOSETHelper closetHelper() {
        return new CLOSETHelper();
    }

    @Bean
    public PLMHelper plmHelper() {
        return new PLMHelper();
    }

    @Bean
    public RestService restService() {
        return new RestService();
    }
}
