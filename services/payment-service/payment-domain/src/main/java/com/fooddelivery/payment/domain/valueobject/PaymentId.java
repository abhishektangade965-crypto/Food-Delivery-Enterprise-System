package com.fooddelivery.payment.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public class PaymentId extends BaseId<UUID> {
    public PaymentId(UUID value) {
        super(value);
    }
}
