package com.newrelic.aws.proxy;

import com.newrelic.aws.proxy.handler.RestTemplateResponseErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	public RestTemplate createRestTemplate() {
		return new RestTemplateBuilder()
				.errorHandler(new RestTemplateResponseErrorHandler())
				.build();
	}
}
