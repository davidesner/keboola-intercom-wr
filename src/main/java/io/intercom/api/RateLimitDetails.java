/*
 */
package io.intercom.api;

import java.time.Instant;

/**
 * Class maintaining the APIs rate limit details;
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class RateLimitDetails {

    //init default values
    private static int limit = 500;
    private static int remaining = 500;
    private static long reset_at = 0;

    protected void updateLimit(int limit, int remaining, long reset_at) {
        RateLimitDetails.remaining = remaining;
        RateLimitDetails.reset_at = reset_at;
    }

    /**
     * Returns remaining time in seconds
     *
     * @return
     */
    public Long getRemainingSeconds() {
        long remains = 0;
        if (reset_at != 0) {
            remains = Instant.ofEpochSecond(reset_at).getEpochSecond() - Instant.now().getEpochSecond();
        }
        return remains;
    }

    /**
     * Returns remaining time in miliseconds
     *
     * @return
     */
    public long getRemainingMilis() {
        long remains = 11;
        if (reset_at != 0) {
            remains = 1000 * (Instant.ofEpochSecond(reset_at).getEpochSecond() - Instant.now().getEpochSecond());
        }
        return remains;
    }

    /**
     * Indicates if the limit is not exceeded yet.
     *
     * @return
     */
    public boolean canSubmit() {
        refreshIfNeeded();
        long remains = this.getRemainingMilis();
        if (RateLimitDetails.remaining > 2) {
            return true;
        } else {
            return remains <= 0;
        }
    }

    /**
     * Reset rate limit if needed
     */
    private void refreshIfNeeded() {
        if (getRemainingSeconds() <= 0) {
            RateLimitDetails.remaining = limit;
        }
    }

    public long getReset_at() {
        return reset_at;
    }

    public int getLimit() {
        refreshIfNeeded();
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    @Override
    public String toString() {
        refreshIfNeeded();
        return "\nlimit: " + limit + "\nremaining: " + remaining + "\nremaining milis: " + getRemainingMilis() + "\nreset at: " + reset_at;
    }

}
