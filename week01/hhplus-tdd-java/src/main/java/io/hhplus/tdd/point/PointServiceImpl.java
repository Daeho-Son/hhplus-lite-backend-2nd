package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public UserPoint point(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint charge(long userId, long amount) {
        if (userId <= 0) {
            throw new InvalidUserIdException("잘못된 userId입니다.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("잘못된 amount입니다.");
        }
        UserPoint userPoint = userPointTable.selectById(userId);
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, userPoint.point() + amount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis());
        return updatedUserPoint;
    }
}
