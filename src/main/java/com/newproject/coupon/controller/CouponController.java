package com.newproject.coupon.controller;

import com.newproject.coupon.dto.CouponRequest;
import com.newproject.coupon.dto.CouponResponse;
import com.newproject.coupon.dto.PriceQuoteRequest;
import com.newproject.coupon.dto.PriceQuoteResponse;
import com.newproject.coupon.service.CouponService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/coupons")
    public List<CouponResponse> listCoupons() {
        return couponService.list();
    }

    @GetMapping("/coupons/{id}")
    public CouponResponse getCoupon(@PathVariable Long id) {
        return couponService.get(id);
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @PutMapping("/coupons/{id}")
    public CouponResponse updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return couponService.update(id, request);
    }

    @DeleteMapping("/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long id) {
        couponService.delete(id);
    }

    @PostMapping("/quote")
    public PriceQuoteResponse quote(@Valid @RequestBody PriceQuoteRequest request) {
        return couponService.quote(request);
    }
}
