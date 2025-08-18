package io.hhplus.tdd.point;


import java.util.List;

public interface PointService {

    UserPoint point(long userId);

    List<PointHistory> history(long userId);

    UserPoint charge(long userId, long amount);

    UserPoint use(long userId, long amount);
}
