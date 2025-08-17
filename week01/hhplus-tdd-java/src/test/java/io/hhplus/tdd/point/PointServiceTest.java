package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    /**
     * 사용자는 양수의 id를 가지고 있다.
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void point_userId는_양수가_아니면_예외를_반환한다(long userId) {
        // when & then
        assertThrows(InvalidUserIdException.class,
            () -> pointService.point(userId));

        verifyNoInteractions(userPointTable, pointHistoryTable);
    }

    /**
     * 포인트 조회 시 포인트 테이블의 메서드를 호출한다.
     */
    @Test
    void point_포인트_조회를_포인트_테이블에_위임한다() {
        // given
        long userId = 1L;
        long amount = 100L;
        UserPoint expected = new UserPoint(userId, amount, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result.id()).isEqualTo(expected.id());
        assertThat(result.point()).isEqualTo(expected.point());
        assertThat(result.updateMillis()).isPositive();

        verify(userPointTable).selectById(userId);
        verifyNoMoreInteractions(userPointTable);
    }

    /**
     * 존재하지 않는 유저가 포인트를 조회하면 UserPoint.empty 값이 반환된다.
     */
    @Test
    void point_존재하지_않는_사용자는_empty_반환() {
        // given
        long userId = Long.MAX_VALUE;
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
        assertThat(result.updateMillis()).isPositive();
    }

    /**
     * userId로 0 이하의 숫자가 요청되면 예외를 반환한다.
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void charge_userId는_양수가_아니면_예외를_반환한다(long userId) {
        // when & then
        assertThrows(InvalidUserIdException.class,
            () -> pointService.charge(userId, 1000L));

        verifyNoInteractions(userPointTable, pointHistoryTable);
    }

    /**
     * 충전 금액으로 0 이하의 숫자가 요청되면 예외를 반환한다.
     */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void charge_amount는_양수가_아니면_예외를_반환한다(long amount) {
        // when & then
        assertThrows(InvalidAmountException.class,
            () -> pointService.charge(1L, amount));

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
        verify(userPointTable).insertOrUpdate(eq(userId), anyLong());
        verify(pointHistoryTable).insert(userId, amount, TransactionType.CHARGE, currentTimestamp);
    }

    /**
     * 단위테스트
     * userId가 양수가 아니면 예외를 반환한다.
     */
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100"})
    void history_userId가_양수가_아니면_예외를_반환한다(long userId) {
        assertThrows(InvalidUserIdException.class,
            () -> pointService.history(userId));

        verifyNoInteractions(pointHistoryTable);
    }

    /**
     * 단위테스트
     * 정상적인 요청인 경우, PointHistoryTable로 위임한다.
     */
    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, Long.MAX_VALUE})
    void history_정상적인_요청은_PointHistoryTable로_위임한다(long userId) {
        when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(List.of());

        pointService.history(userId);
        verify(pointHistoryTable).selectAllByUserId(userId);
        verifyNoMoreInteractions(pointHistoryTable);
    }

    /**
     * 통합테스트
     * 결과가 없으면 빈 리스트를 반환한다.
     */
    @Test
    void history_결과가_없으면_빈_리스트를_반환한다() {
        // given
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        PointService pointService = new PointServiceImpl(userPointTable, pointHistoryTable);

        long userId = 1L;

        // when
        List<PointHistory> histories = pointService.history(userId);

        // then
        assertThat(histories).isEmpty();
    }

    /**
     * 통합테스트
     * 조회 결과가 여러 건이 있으면 여러 건이 담긴 배열을 반환한다.
     */
    @Test
    void history_여러_건이_있으면_여러_건이_담긴_배열을_반환한다() {
        // given
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        PointService pointService = new PointServiceImpl(userPointTable, pointHistoryTable);

        long userId = 1L;
        pointService.charge(userId, 100L);
        pointService.charge(userId, 200L);
        pointService.charge(userId, 200L);

        // when
        List<PointHistory> result = pointService.history(userId);
        
        // then
        assertThat(result).hasSize(3);
        for (PointHistory history : result) {
            assertThat(history.userId()).isEqualTo(userId);
        }
        assertThat(result.get(0).amount()).isEqualTo(100L);
        assertThat(result.get(1).amount()).isEqualTo(200L);
        assertThat(result.get(2).amount()).isEqualTo(200L);
    }
}