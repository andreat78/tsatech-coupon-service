package com.newproject.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PriceQuoteRequest {
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal subtotal;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal shipping;

    private String couponCode;

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
