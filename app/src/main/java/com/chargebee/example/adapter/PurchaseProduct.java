package com.chargebee.example.adapter;

import com.chargebee.android.models.CBProduct;
import com.chargebee.android.models.PricingPhase;
import com.chargebee.android.models.SubscriptionOffer;

public class PurchaseProduct {
    private final String productId;
    private final CBProduct cbProduct;
    private final String basePlanId;
    private final String offerId;
    private final String offerToken;
    private final String price;

    public PurchaseProduct(CBProduct cbProduct, SubscriptionOffer subscriptionOffer) {
        this(cbProduct.getId(), cbProduct, subscriptionOffer.getBasePlanId(), subscriptionOffer.getOfferId(),
                subscriptionOffer.getOfferToken(), subscriptionOffer.getPricingPhases().get(0).getFormattedPrice());
    }

    public PurchaseProduct(CBProduct cbProduct, PricingPhase oneTimePurchaseOffer) {
        this(cbProduct.getId(), cbProduct,
                null, null,
                null, oneTimePurchaseOffer.getFormattedPrice());
    }

    public PurchaseProduct(String id, CBProduct cbProduct, String basePlanId, String offerId, String offerToken, String formattedPrice) {
        this.productId = id;
        this.cbProduct = cbProduct;
        this.basePlanId = basePlanId;
        this.offerId = offerId;
        this.offerToken = offerToken;
        this.price = formattedPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getBasePlanId() {
        return basePlanId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getOfferToken() {
        return offerToken;
    }

    public String getPrice() {
        return price;
    }

    public CBProduct getCbProduct() {
        return cbProduct;
    }
}