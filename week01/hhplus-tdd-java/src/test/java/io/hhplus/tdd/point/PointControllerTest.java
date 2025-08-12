package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    void 사용자_1을_조회하면_1번_사용자의_포인트를_조회할_수_있다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.point").exists())
            .andExpect(jsonPath("$.updateMillis").exists());
    }

    @Test
    void 서비스는_한번만_호출된다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk());

        verify(pointService, times(1)).point(userPoint.id());
        verifyNoMoreInteractions(pointService);
    }

    @Test
    void 경로변수_타입이_숫자가_아니면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void 사용자_id로_0이_들어오면_400_에러를_반환한다() throws Exception{
        mockMvc.perform(get("/point/0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void 사용자_id로_음수가_들어오면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());
    }
}