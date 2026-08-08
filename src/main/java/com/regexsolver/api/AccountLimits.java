package com.regexsolver.api;

import com.regexsolver.api.generated.model.AccountLimitsDto;
import java.util.Objects;

/**
 * The plan limits currently applying to the account.
 */
public final class AccountLimits {

    private final long maxRequestsCount;
    private final long maxRequestsRate;
    private final long maxTermsCount;
    private final long maxTimeout;
    private final long maxStatesCount;

    AccountLimits(
        long maxRequestsCount,
        long maxRequestsRate,
        long maxTermsCount,
        long maxTimeout,
        long maxStatesCount
    ) {
        this.maxRequestsCount = maxRequestsCount;
        this.maxRequestsRate = maxRequestsRate;
        this.maxTermsCount = maxTermsCount;
        this.maxTimeout = maxTimeout;
        this.maxStatesCount = maxStatesCount;
    }

    static AccountLimits fromDto(AccountLimitsDto dto) {
        return new AccountLimits(
            dto.getMaxRequestsCount(),
            dto.getMaxRequestsRate(),
            dto.getMaxTermsCount(),
            dto.getMaxTimeout(),
            dto.getMaxStatesCount()
        );
    }

    /** @return Maximum number of requests allowed per billing period. */
    public long getMaxRequestsCount() {
        return maxRequestsCount;
    }

    /** @return Maximum number of requests allowed per second. 0 means no rate limit is enforced. */
    public long getMaxRequestsRate() {
        return maxRequestsRate;
    }

    /** @return Maximum number of terms accepted in a single request. */
    public long getMaxTermsCount() {
        return maxTermsCount;
    }

    /** @return Maximum execution timeout per request, in milliseconds. */
    public long getMaxTimeout() {
        return maxTimeout;
    }

    /** @return Maximum number of automaton states an operation may build. */
    public long getMaxStatesCount() {
        return maxStatesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountLimits)) return false;
        AccountLimits that = (AccountLimits) o;
        return (
            maxRequestsCount == that.maxRequestsCount &&
            maxRequestsRate == that.maxRequestsRate &&
            maxTermsCount == that.maxTermsCount &&
            maxTimeout == that.maxTimeout &&
            maxStatesCount == that.maxStatesCount
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            maxRequestsCount,
            maxRequestsRate,
            maxTermsCount,
            maxTimeout,
            maxStatesCount
        );
    }

    @Override
    public String toString() {
        return (
            "<AccountLimits: maxRequestsCount=" +
            maxRequestsCount +
            ", maxRequestsRate=" +
            maxRequestsRate +
            ", maxTermsCount=" +
            maxTermsCount +
            ", maxTimeout=" +
            maxTimeout +
            ", maxStatesCount=" +
            maxStatesCount +
            ">"
        );
    }
}
