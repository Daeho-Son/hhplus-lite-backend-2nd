package io.hhplus.tdd.point;

public interface PointService {

    UserPoint point(long userId);

    UserPoint charge(long userId, long amount);
}
