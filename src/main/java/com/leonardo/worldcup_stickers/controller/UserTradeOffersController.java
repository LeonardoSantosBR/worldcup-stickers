package com.leonardo.worldcup_stickers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.config.JwtAuthFilter;
import com.leonardo.worldcup_stickers.dto.MakeOfferDto;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.dto.RespondOfferDto;
import com.leonardo.worldcup_stickers.dto.TradeOfferDetailDto;
import com.leonardo.worldcup_stickers.dto.TradeOfferDto;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;
import com.leonardo.worldcup_stickers.services.UserTradeOffersService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/trade-offers")
public class UserTradeOffersController {
    private final UserTradeOffersService userTradeOffersService;

    public UserTradeOffersController(UserTradeOffersService userTradeOffersService) {
        this.userTradeOffersService = userTradeOffersService;
    }

    @GetMapping("/inbox")
    public PageResponseDto<TradeOfferDetailDto> inbox(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) TradeStatusEnum status) {
        return userTradeOffersService.findReceivedOffers(userId, page, limit, status);
    }

    @PostMapping("/make-offer")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeOfferDto makeOffer(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @Valid @RequestBody MakeOfferDto body) {
        return userTradeOffersService.makeOffer(userId, body);
    }

    @PostMapping("/{offerId}/accept")
    public TradeOfferDto acceptOffer(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long offerId,
            @Valid @RequestBody(required = false) RespondOfferDto body) {
        return userTradeOffersService.acceptOffer(userId, offerId, noteOf(body));
    }

    @PostMapping("/{offerId}/reject")
    public TradeOfferDto rejectOffer(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long offerId,
            @Valid @RequestBody(required = false) RespondOfferDto body) {
        return userTradeOffersService.rejectOffer(userId, offerId, noteOf(body));
    }

    private static String noteOf(RespondOfferDto body) {
        return body == null ? null : body.note();
    }
}
