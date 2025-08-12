package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;

    public PointServiceImpl(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public UserPoint point(long userId) {
        return userPointTable.selectById(userId);
    }
}
