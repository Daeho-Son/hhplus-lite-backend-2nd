package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    void point_포인트_조회_시_포인트_테이블을_한_번_호출() {
        // given
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 100L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result).isSameAs(expected);
        verify(userPointTable, times(1)).selectById(userId);
        verifyNoMoreInteractions(userPointTable);
    }

    @Test
    void point_존재하지_않는_사용자는_empty_반환() {
        // given
        long userId = 999L;
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    /**
     * userId는 1부터 long 최대값까지 사용할 수 있다.
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void charge_userId는_양수가_아니면_예외를_반환한다(long userId) {
        // given
        long amount = 1000L;

        // when & then
        assertThrows(InvalidUserIdException.class,
            () -> pointService.charge(userId, amount));

        verifyNoInteractions(userPointTable, pointHistoryTable);
    }

    /**
     * 양수의 값만 충전할 수 있다.
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void charge_amount는_양수가_아니면_예외를_반환한다(long amount) {
        // given
        long userId = 1L;

        // when & then
        assertThrows(InvalidAmountException.class,
            () -> pointService.charge(userId, amount));

        verifyNoInteractions(userPointTable, pointHistoryTable);
    }

    /**
     * 충전에 성공하면 포인트가 증가해야한다.
     */
    @Test
    void charge_충전에_성공하면_포인트가_증가한다() {
        // given
        long userId = 1L;
        long amount = 1000L;
        long currentTimestamp = 123456789L;
        UserPoint initial = new UserPoint(userId, 500L, currentTimestamp);
        when(userPointTable.selectById(userId))
            .thenReturn(initial);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong()))
            .thenAnswer(inv -> new UserPoint(
                inv.getArgument(0, Long.class), inv.getArgument(1, Long.class), currentTimestamp)
            );

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(initial.point() + amount);
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, initial.point() + amount);
    }

    /**
     * 충전에 성공하면 이력이 남는다.
     */
    @Test
    void charge_충전에_성공하면_이력이_남는다() {
        // given
        long userId = 1L;
        long amount = 1000L;
        long currentTimestamp = 123456789L;
        UserPoint initial = new UserPoint(userId, 500L, currentTimestamp);
        when(userPointTable.selectById(userId)).thenReturn(initial);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong()))
            .thenAnswer(inv -> new UserPoint(
                inv.getArgument(0, Long.class),
                inv.getArgument(1, Long.class),
                currentTimestamp
            ));

        // when
        pointService.charge(userId, amount);

        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, anyLong());
        verify(pointHistoryTable).insert(userId, amount, TransactionType.CHARGE, currentTimestamp);
    }
}