package com.maschior.fms.domain.model;

import com.maschior.fms.domain.enums.AccountGroup;
import com.maschior.fms.domain.enums.LiquidityClass;
import com.maschior.fms.domain.enums.NormalBalanceSide;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_code", columnNames = "code")
    },
    indexes = {
        @Index(name = "ix_accounts_group", columnList = "group_type"),
        @Index(name = "ix_accounts_liquidity", columnList = "liquidity_class")
    }
)

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "1.1.1" (Caixa), "2.1.1" (Fornecedores)
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    // Ex: "Caixa", "Banco Itaú", "Fornecedores"
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 16)
    private AccountGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "liquidity_class", length = 16)
    private LiquidityClass liquidityClass;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Account() { } // JPA

    public Account(
            String code,
            String name,
            AccountGroup group,
            LiquidityClass liquidityClass,
            String description
    ) {
        this.code = code;
        this.name = name;
        this.group = group;
        this.liquidityClass = liquidityClass;
        this.description = description;
        validate();
    }

    void prePersist() {
        validate();
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    void  preUpdate() {
        validate();
        this.updatedAt = Instant.now();
    }

    private void validate() {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code é obrigatório");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório");
        if (group == null) throw new IllegalArgumentException("group é obrigatório");

        boolean allowsLiquidity = (group == AccountGroup.ASSET || group == AccountGroup.LIABILITY);
        if (!allowsLiquidity && liquidityClass != null) {
            throw new IllegalArgumentException("liquidityClass só é permitido para ASSET/LIABILITY");
        }
    }

    /**
     * Lado natural do saldo (essencial pra entender débito/crédito).
     * - ASSET/EXPENSE: saldo natural DEBIT
     * - LIABILITY/EQUITY/REVENUE: saldo natural CREDIT
     */
    @Transient
    public NormalBalanceSide getNormalBalanceSide() {
        return switch (group) {
            case ASSET, EXPENSE -> NormalBalanceSide.DEBIT;
            case LIABILITY, EQUITY, REVENUE -> NormalBalanceSide.CREDIT;
        };
    }


}
