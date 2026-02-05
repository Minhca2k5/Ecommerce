package com.minzetsu.ecommerce.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "checkout.pricing")
public class CheckoutPricingProperties {
    private String defaultCurrency = "VND";
    private Map<String, BigDecimal> exchangeRates = new LinkedHashMap<>();
    private Map<String, BigDecimal> taxRates = new LinkedHashMap<>();
    private Map<String, BigDecimal> shippingFlatFees = new LinkedHashMap<>();

    public CheckoutPricingProperties() {
        exchangeRates.put("VND", BigDecimal.ONE);
        exchangeRates.put("USD", new BigDecimal("0.000039"));
        exchangeRates.put("JPY", new BigDecimal("0.0058"));

        taxRates.put("VND", new BigDecimal("0.08"));
        taxRates.put("USD", new BigDecimal("0.08"));
        taxRates.put("JPY", new BigDecimal("0.10"));

        shippingFlatFees.put("VND", new BigDecimal("30000"));
        shippingFlatFees.put("USD", new BigDecimal("1.50"));
        shippingFlatFees.put("JPY", new BigDecimal("220"));
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public Map<String, BigDecimal> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(Map<String, BigDecimal> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public Map<String, BigDecimal> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(Map<String, BigDecimal> taxRates) {
        this.taxRates = taxRates;
    }

    public Map<String, BigDecimal> getShippingFlatFees() {
        return shippingFlatFees;
    }

    public void setShippingFlatFees(Map<String, BigDecimal> shippingFlatFees) {
        this.shippingFlatFees = shippingFlatFees;
    }

    public String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return defaultCurrency;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
