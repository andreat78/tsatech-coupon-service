package com.newproject.coupon.service;

import com.newproject.coupon.domain.Coupon;
import com.newproject.coupon.dto.CouponRequest;
import com.newproject.coupon.dto.CouponResponse;
import com.newproject.coupon.dto.PriceQuoteRequest;
import com.newproject.coupon.dto.PriceQuoteResponse;
import com.newproject.coupon.events.EventPublisher;
import com.newproject.coupon.exception.BadRequestException;
import com.newproject.coupon.exception.NotFoundException;
import com.newproject.coupon.repository.CouponRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final EventPublisher eventPublisher;

    public CouponService(CouponRepository couponRepository, EventPublisher eventPublisher) {
        this.couponRepository = couponRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> list() {
        return couponRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponResponse get(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Coupon not found"));
        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        couponRepository.findByCodeIgnoreCase(request.getCode())
            .ifPresent(existing -> {
                throw new BadRequestException("Coupon code already exists");
            });

        Coupon coupon = new Coupon();
        applyRequest(coupon, request);
        coupon.setUsedCount(0);
        coupon.setUpdatedAt(OffsetDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        eventPublisher.publish("COUPON_CREATED", "coupon", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Coupon not found"));

        couponRepository.findByCodeIgnoreCase(request.getCode())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new BadRequestException("Coupon code already exists");
            });

        applyRequest(coupon, request);
        coupon.setUpdatedAt(OffsetDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        eventPublisher.publish("COUPON_UPDATED", "coupon", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Coupon not found"));

        couponRepository.delete(coupon);
        eventPublisher.publish("COUPON_DELETED", "coupon", id.toString(), null);
    }

    @Transactional(readOnly = true)
    public PriceQuoteResponse quote(PriceQuoteRequest request) {
        BigDecimal subtotal = notNull(request.getSubtotal());
        BigDecimal shipping = notNull(request.getShipping());

        PriceQuoteResponse response = new PriceQuoteResponse();
        response.setSubtotal(subtotal);
        response.setShipping(shipping);

        BigDecimal discount = BigDecimal.ZERO;
        String couponCode = request.getCouponCode();
        String message = null;

        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim())
                .orElse(null);

            if (coupon == null) {
                message = "Coupon non valido";
            } else if (!isCouponUsable(coupon, subtotal)) {
                message = "Coupon non applicabile";
            } else {
                discount = calculateDiscount(coupon, subtotal);
                response.setAppliedCoupon(coupon.getCode());
                message = "Coupon applicato";
            }
        }

        BigDecimal total = subtotal.add(shipping).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        response.setDiscount(scale(discount));
        response.setTotal(scale(total));
        response.setMessage(message);
        return response;
    }

    private boolean isCouponUsable(Coupon coupon, BigDecimal subtotal) {
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (coupon.getDateStart() != null && coupon.getDateStart().isAfter(now)) {
            return false;
        }
        if (coupon.getDateEnd() != null && coupon.getDateEnd().isBefore(now)) {
            return false;
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return false;
        }

        return subtotal.compareTo(notNull(coupon.getMinTotal())) >= 0;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        String type = coupon.getDiscountType() != null ? coupon.getDiscountType().toUpperCase(Locale.ROOT) : "FIXED";
        BigDecimal discount;

        if ("PERCENT".equals(type)) {
            discount = subtotal.multiply(coupon.getValue()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getValue();
        }

        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return scale(discount);
    }

    private void applyRequest(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.getCode() != null ? request.getCode().trim().toUpperCase(Locale.ROOT) : null);
        coupon.setName(request.getName());
        coupon.setDiscountType(request.getDiscountType() != null ? request.getDiscountType().trim().toUpperCase(Locale.ROOT) : null);
        coupon.setValue(scale(request.getValue()));
        coupon.setMinTotal(scale(request.getMinTotal()));
        coupon.setMaxDiscount(request.getMaxDiscount() != null ? scale(request.getMaxDiscount()) : null);
        coupon.setCurrency(request.getCurrency() != null ? request.getCurrency().trim().toUpperCase(Locale.ROOT) : null);
        coupon.setActive(request.getActive());
        coupon.setDateStart(request.getDateStart());
        coupon.setDateEnd(request.getDateEnd());
        coupon.setUsageLimit(request.getUsageLimit());
        if (coupon.getUsedCount() == null) {
            coupon.setUsedCount(0);
        }
    }

    private CouponResponse toResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setName(coupon.getName());
        response.setDiscountType(coupon.getDiscountType());
        response.setValue(coupon.getValue());
        response.setMinTotal(coupon.getMinTotal());
        response.setMaxDiscount(coupon.getMaxDiscount());
        response.setCurrency(coupon.getCurrency());
        response.setActive(coupon.getActive());
        response.setDateStart(coupon.getDateStart());
        response.setDateEnd(coupon.getDateEnd());
        response.setUsageLimit(coupon.getUsageLimit());
        response.setUsedCount(coupon.getUsedCount());
        response.setUpdatedAt(coupon.getUpdatedAt());
        return response;
    }

    private BigDecimal notNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
