package NoDam.Demo.memo.dto;

import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import NoDam.Demo.memo.dto.response.MemoInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoInfoTest {

    @Test
    @DisplayName("Memo의 값으로 MemoInfo가 만들어진다")
    void from() {
        Memo memo = Memo.builder()
                .tripId(1L)
                .userId(10L)
                .icon(MemoIconType.IDEA)
                .title("제목")
                .content("내용")
                .build();

        MemoInfo info = MemoInfo.from(memo);

        assertEquals(MemoIconType.IDEA, info.getIcon());
        assertEquals("제목", info.getTitle());
        assertEquals("내용", info.getContent());
    }

}
