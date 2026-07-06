package com.fooddelivery.payment.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.payment.domain.valueobject.CreditHistoryId;
import com.fooddelivery.payment.domain.valueobject.TransactionType;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class CreditHistory extends BaseEntity<CreditHistoryId> {

    private final CustomerId customerId;
    private final Money price;
    private final TransactionType transactionType;
    private final ZonedDateTime createdAt;

    private CreditHistory(Builder builder) {
        super.setId(builder.creditHistoryId);
        this.customerId = builder.customerId;
        this.price = builder.price;
        this.transactionType = builder.transactionType;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private CreditHistoryId creditHistoryId;
        private CustomerId customerId;
        private Money price;
        private TransactionType transactionType;
        private ZonedDateTime createdAt;

        public Builder creditHistoryId(CreditHistoryId val) { creditHistoryId = val; return this; }
        public Builder customerId(CustomerId val) { customerId = val; return this; }
        public Builder price(Money val) { price = val; return this; }
        public Builder transactionType(TransactionType val) { transactionType = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public CreditHistory build() { return new CreditHistory(this); }
    }
}
