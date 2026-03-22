package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.PromoCodeRequest;
import com.danceclub.club_system.dto.PromoCodeValidateDTO;
import com.danceclub.club_system.model.PromoCode;
import com.danceclub.club_system.model.enums.DiscountType;
import com.danceclub.club_system.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    // =====================================================================
    //  Admin CRUD
    // =====================================================================

    @Transactional
    public PromoCode createPromoCode(PromoCodeRequest request, String createdBy) {
        String upperCode = request.getCode().toUpperCase().trim();
        if (promoCodeRepository.findByCode(upperCode).isPresent()) {
            throw new IllegalArgumentException("優惠碼已存在：" + upperCode);
        }
        PromoCode promo = new PromoCode();
        promo.setCode(upperCode);
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setDescription(request.getDescription());
        promo.setMaxUsage(request.getMaxUsage());
        promo.setExpiresAt(request.getExpiresAt());
        promo.setCreatedBy(createdBy);
        return promoCodeRepository.save(promo);
    }

    public List<PromoCode> listAll() {
        return promoCodeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deletePromoCode(Long id) {
        promoCodeRepository.deleteById(id);
    }

    @Transactional
    public PromoCode toggleActive(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到優惠碼：" + id));
        promo.setIsActive(!Boolean.TRUE.equals(promo.getIsActive()));
        return promoCodeRepository.save(promo);
    }

    // =====================================================================
    //  Member: validate (preview only, no usage increment)
    // =====================================================================

    public PromoCodeValidateDTO validate(String code, BigDecimal feeAmount) {
        PromoCodeValidateDTO dto = new PromoCodeValidateDTO();
        dto.setCode(code.toUpperCase().trim());
        dto.setOriginalAmount(feeAmount);

        Optional<PromoCode> opt = promoCodeRepository.findByCodeAndIsActiveTrue(code.toUpperCase().trim());
        if (opt.isEmpty()) {
            dto.setValid(false);
            dto.setErrorMessage("優惠碼無效或已停用");
            return dto;
        }

        PromoCode promo = opt.get();

        if (promo.getExpiresAt() != null && LocalDateTime.now().isAfter(promo.getExpiresAt())) {
            dto.setValid(false);
            dto.setErrorMessage("優惠碼已過期");
            return dto;
        }

        if (promo.getMaxUsage() != null && promo.getUsedCount() >= promo.getMaxUsage()) {
            dto.setValid(false);
            dto.setErrorMessage("優惠碼已達使用上限");
            return dto;
        }

        BigDecimal finalAmount = calcFinalAmount(promo, feeAmount);
        dto.setValid(true);
        dto.setDiscountType(promo.getDiscountType());
        dto.setDiscountValue(promo.getDiscountValue());
        dto.setDescription(promo.getDescription());
        dto.setFinalAmount(finalAmount);
        dto.setSavedAmount(feeAmount.subtract(finalAmount));
        return dto;
    }

    // =====================================================================
    //  Registration: apply (increments usage count)
    // =====================================================================

    @Transactional
    public DiscountService.DiscountResult apply(String code, BigDecimal feeAmount) {
        PromoCode promo = promoCodeRepository.findByCodeAndIsActiveTrue(code.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("無效的優惠碼"));

        if (promo.getExpiresAt() != null && LocalDateTime.now().isAfter(promo.getExpiresAt())) {
            throw new IllegalStateException("優惠碼已過期");
        }
        if (promo.getMaxUsage() != null && promo.getUsedCount() >= promo.getMaxUsage()) {
            throw new IllegalStateException("優惠碼已達使用上限");
        }

        BigDecimal finalAmount = calcFinalAmount(promo, feeAmount);

        // Increment usage
        promo.setUsedCount(promo.getUsedCount() + 1);
        promoCodeRepository.save(promo);

        return new DiscountService.DiscountResult(DiscountType.PROMO_CODE, feeAmount, finalAmount, null);
    }

    // =====================================================================
    //  Helper
    // =====================================================================

    private BigDecimal calcFinalAmount(PromoCode promo, BigDecimal original) {
        if ("FIXED_AMOUNT".equals(promo.getDiscountType())) {
            return original.subtract(promo.getDiscountValue()).max(BigDecimal.ZERO);
        } else { // PERCENTAGE
            return original.multiply(promo.getDiscountValue()).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
