package com.maschior.fms.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;


/**
 *
 * @param minor pode ser negativo (saldo, estorno, débito, etc).
 * @param currency não pode ser null
 */
public record Money(Long minor, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(currency, "currency can't be null");
    }

    /**
     * Cria um Money a partir de um valor decimal, aplicando escala e rounding definidos.
     *
     * @param major
     * @param currency
     * @param rounding
     * @return
     */
    public static Money of(BigDecimal major, Currency currency, RoundingMode rounding) {
        Objects.requireNonNull(major, "major can't be null");
        Objects.requireNonNull(currency, "currency can't be null");
        Objects.requireNonNull(rounding, "rounding can't be null");

        int scale = fractionDigits(currency);

        BigDecimal scaled = major.setScale(scale, rounding);
        BigDecimal factor = BigDecimal.TEN.pow(scale);

        // minor = major * 10^scale (ex: 12.34 BRL -> 1234)
        BigDecimal minorBd = scaled.multiply(factor);

        long minor;
        try {
            minor = minorBd.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount out of range for long minor units: " + major, e);
        }

        return new Money(minor, currency);
    }

    /** Cria exatamente com a escala correta (falha se precisar arredondar). */
    public static Money ofExact(BigDecimal major, Currency currency) {
        return of(major, currency, RoundingMode.UNNECESSARY);
    }

    public static Money ofMinor(long minor, Currency currency) {
        return new Money(minor, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    /** Retorna valor "major" (ex: 1234 -> 12.34) como BigDecimal, derivado. */
    public BigDecimal toBigDecimal() {
        int scale = fractionDigits(currency);
        return BigDecimal.valueOf(minor, scale);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.minor, other.minor), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(this.minor, other.minor), currency);
    }

    /** Multiplica por um fator decimal com rounding explícito. */
    public Money multiply(BigDecimal factor, RoundingMode rounding) {
        Objects.requireNonNull(factor, "factor can't be null");
        Objects.requireNonNull(rounding, "rounding can't be null");

        int scale = fractionDigits(currency);

        // minor * factor -> ainda em minor (com arredondamento no minor)
        BigDecimal resultMinor = BigDecimal.valueOf(minor).multiply(factor);
        long newMinor;
        try {
            newMinor = resultMinor.setScale(0, rounding).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount out of range for floating minor units: " + factor, e);
        }
        return new Money(newMinor, currency);
    }

    /** Divide em N partes (alocação) mantendo soma exata; sobra vai pros primeiros. */
    public Money[] allocate(int parts) {
        if (parts <= 0) throw new IllegalArgumentException("parts <= 0");
        Money[] out = new Money[parts];

        long base = minor / parts;
        long remainder = Math.floorMod(minor, parts);

        for (int i = 0; i < parts; i++) {
            long extra = (i < remainder) ? 1L : 0L;
            out[i] = new Money(base + extra, currency);
        }
        return out;
    }

    public boolean isZero() {
        return minor == 0L;
    }

    public boolean isPositive() {
        return minor > 0L;
    }

    public boolean isNegative() {
        return minor < 0L;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return Long.compare(minor, other.minor);
    }

    /**
     *
     * @param currency to get fraction digits
     * @return int - fraction digits
     */
    private static int fractionDigits(Currency currency) {
        int fd = currency.getDefaultFractionDigits();
        if (fd < 0) {
            throw new  IllegalArgumentException("Unsupported currency fractions digits for " + currency);
        }
        return fd;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other can't be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Different currencies: " + this.currency + "vs" + other.currency);
        }
    }

    private void checkCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Different currencies");
        }
    }
}
