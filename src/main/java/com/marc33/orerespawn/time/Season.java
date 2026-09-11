package com.marc33.orerespawn.time;

import com.marc33.orerespawn.config.SeasonConfig;

import java.time.LocalDate;
import java.time.Month;

/**
 * A meteorological, real-calendar season, used to scale the ore respawn delay when
 * {@code enableSeasons} is on. Matches the same real-time basis {@code respawnDelaySeconds}
 * already uses (server clock, not in-game time).
 */
public enum Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    public static Season current() {
        Season season = fromMonth(LocalDate.now().getMonth());
        return SeasonConfig.SOUTHERN_HEMISPHERE.get() ? season.opposite() : season;
    }

    private static Season fromMonth(Month month) {
        return switch (month) {
            case MARCH, APRIL, MAY -> SPRING;
            case JUNE, JULY, AUGUST -> SUMMER;
            case SEPTEMBER, OCTOBER, NOVEMBER -> AUTUMN;
            case DECEMBER, JANUARY, FEBRUARY -> WINTER;
        };
    }

    private Season opposite() {
        return switch (this) {
            case SPRING -> AUTUMN;
            case SUMMER -> WINTER;
            case AUTUMN -> SPRING;
            case WINTER -> SUMMER;
        };
    }

    public double delayMultiplier() {
        return switch (this) {
            case SPRING -> SeasonConfig.SPRING_DELAY_MULTIPLIER.get();
            case SUMMER -> SeasonConfig.SUMMER_DELAY_MULTIPLIER.get();
            case AUTUMN -> SeasonConfig.AUTUMN_DELAY_MULTIPLIER.get();
            case WINTER -> SeasonConfig.WINTER_DELAY_MULTIPLIER.get();
        };
    }
}
