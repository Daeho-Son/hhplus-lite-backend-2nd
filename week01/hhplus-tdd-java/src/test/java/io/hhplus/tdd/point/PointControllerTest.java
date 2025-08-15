package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    void point_사용자_1을_조회하면_1번_사용자의_포인트를_조회할_수_있다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.point").exists())
            .andExpect(jsonPath("$.updateMillis").exists());
    }

    @Test
    void point_서비스는_한번만_호출된다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk());

        verify(pointService, times(1)).point(userPoint.id());
        verifyNoMoreInteractions(pointService);
    }

    @Test
    void point_사용자_id가_숫자가_아니면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void point_사용자_id로_0이_들어오면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void point_사용자_id로_음수가_들어오면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    /**
     * - 컨트롤러의 비즈니스 로직은 service에서 수행되며, 응답 시 `json`을 반환한다.
     * - 요청과 응답의 Content-Type은 `application/json`이다.
     */
    @Test
    void charge_요청을_service로_위임하고_json으로_응답한다() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint userPoint = new UserPoint(userId, amount, 123456789L);
        when(pointService.charge(userPoint.id(), amount)).thenReturn(userPoint);

        // when & then
        mockMvc.perform(
                patch("/point/{id}/charge", userId)
                    .contentType("application/json")
                    .content(String.valueOf(amount))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(userPoint.id()))
            .andExpect(jsonPath("$.point").value(userPoint.point()))
            .andExpect(jsonPath("$.updateMillis").exists());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        verify(pointService).charge(userIdCaptor.capture(), amountCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(amountCaptor.getValue()).isEqualTo(amount);

        verifyNoMoreInteractions(pointService);
    }

    /**
     * 사용자 아이디는 숫자만 입력받을 수 있다.
     */
    @Test
    void charge_사용자_id가_숫자가_아니면_400_에러가_발생한다() throws Exception {
        mockMvc.perform(
                patch("/point/abc/charge")
                    .contentType("application/json")
                    .content(String.valueOf(1000L))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(pointService);
    }
}