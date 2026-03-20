package com.example.nguyenthanhdat;

import com.example.nguyenthanhdat.service.OrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceRewardPointsTests {

    private final OrderService orderService = new OrderService();

    @Test
    void shouldGiveZeroPointsWhenSubtotalBelowDivisor() {
        assertEquals(0, orderService.calculateRewardPoints(7_499));
        assertEquals(0, orderService.calculateRewardPoints(0));
    }

    @Test
    void shouldGiveOnePointWhenSubtotalIsSevenThousandFiveHundred() {
        assertEquals(1, orderService.calculateRewardPoints(7_500));
    }

    @Test
    void shouldGiveTwoPointsWhenSubtotalIsFifteenThousand() {
        assertEquals(2, orderService.calculateRewardPoints(15_000));
    }

    @Test
    void shouldRoundToNearestIntegerForFractionalPoints() {
        assertEquals(133, orderService.calculateRewardPoints(1_000_000));
        assertEquals(134, orderService.calculateRewardPoints(1_005_000));
    }

    @Test
    void shouldFollowRequestedExampleOneMillionDividedBySevenThousandFiveHundred() {
        assertEquals(133, orderService.calculateRewardPoints(1_000_000));
    }

    @Test
    void shouldCalculateRewardValueFromComputedPoints() {
        assertEquals(997500.0, orderService.calculateRewardValue(1_000_000), 0.0001);
        assertEquals(15000.0, orderService.calculateRewardValue(15_000), 0.0001);
        assertEquals(0.0, orderService.calculateRewardValue(7_499), 0.0001);
    }
}
