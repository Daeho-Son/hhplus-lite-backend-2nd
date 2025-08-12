package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    void 포인트_조회_시_포인트_테이블을_한_번_호출() {
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
    void 존재하지_않는_사용자는_empty_반환() {
        // given
        long userId = 999L;
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }
}