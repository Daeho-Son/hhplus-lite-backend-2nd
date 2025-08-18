package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InsufficientPointException;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (userId <= 0) {
            throw new InvalidUserIdException("잘못된 사용자 ID입니다.");
        }
        return userPointTable.selectById(userId);
    }

    @Override
    public List<PointHistory> history(long userId) {
        if (userId <= 0) {
            throw new InvalidUserIdException("잘못된 사용자 ID입니다.");
        }
        return pointHistoryTable.selectAllByUserId(userId);
    }

    @Override
    public UserPoint charge(long userId, long amount) {
        if (userId <= 0) {
            throw new InvalidUserIdException("잘못된 사용자 ID입니다.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("잘못된 충전 금액입니다.");
        }
        UserPoint userPoint = userPointTable.selectById(userId);
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, userPoint.point() + amount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis());
        return updatedUserPoint;
    }

    // 남은 포인트 반환
    @Override
    public UserPoint use(long userId, long amount) {
        if (userId <= 0) {
            throw new InvalidUserIdException("잘못된 사용자 ID입니다.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("잘못된 사용 금액입니다.");
        }
        UserPoint userPoint = userPointTable.selectById(userId);
        if (userPoint.point() < amount) {
            throw new InsufficientPointException("포인트가 부족합니다.");
        }
        pointHistoryTable.insert(userId, amount, TransactionType.USE, userPoint.updateMillis());
        return userPointTable.insertOrUpdate(userId, userPoint.point() - amount);
    }
}
