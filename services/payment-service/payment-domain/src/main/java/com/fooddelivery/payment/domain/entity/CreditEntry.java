package com.fooddelivery.payment.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.payment.domain.valueobject.CreditEntryId;
import lombok.Getter;

@Getter
public class CreditEntry extends BaseEntity<CreditEntryId> {

    private final CustomerId customerId;
    private Money totalCreditAmount;

    private CreditEntry(Builder builder) {
        super.setId(builder.creditEntryId);
        this.customerId = builder.customerId;
        this.totalCreditAmount = builder.totalCreditAmount;
    }

    public void addCreditAmount(Money amount) {
        this.totalCreditAmount = this.totalCreditAmount.add(amount);
    }

    public void subtractCreditAmount(Money amount) {
        this.totalCreditAmount = this.totalCreditAmount.subtract(amount);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private CreditEntryId creditEntryId;
        private CustomerId customerId;
        private Money totalCreditAmount;

        public Builder creditEntryId(CreditEntryId val) { creditEntryId = val; return this; }
        public Builder customerId(CustomerId val) { customerId = val; return this; }
        public Builder totalCreditAmount(Money val) { totalCreditAmount = val; return this; }
        public CreditEntry build() { return new CreditEntry(this); }
    }
}
