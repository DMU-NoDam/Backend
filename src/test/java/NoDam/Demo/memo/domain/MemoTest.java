package NoDam.Demo.memo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    Memo domain 단위 test (DB 없음)
 */
class MemoTest {

    private Memo createMemo() {
        return Memo.builder()
                .tripId(1L)
                .userId(10L)
                .icon(MemoIconType.FOOD)
                .title("라멘 맛집")
                .content("이치란 본점")
                .build();
    }

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("icon이 null이면 ETC로 생성된다")
        void defaultIcon() {
            Memo memo = Memo.builder()
                    .tripId(1L)
                    .userId(10L)
                    .title("제목")
                    .build();

            assertEquals(MemoIconType.ETC, memo.getIcon());
        }

        @Test
        @DisplayName("icon을 지정하면 그대로 생성된다")
        void givenIcon() {
            Memo memo = createMemo();

            assertEquals(MemoIconType.FOOD, memo.getIcon());
        }
    }

    @Nested
    @DisplayName("수정")
    class Update {

        @Test
        @DisplayName("모든 값이 null이면 변경되지 않는다")
        void allNull() {
            Memo memo = createMemo();

            memo.update(null, null, null);

            assertEquals(MemoIconType.FOOD, memo.getIcon());
            assertEquals("라멘 맛집", memo.getTitle());
            assertEquals("이치란 본점", memo.getContent());
        }

        @Test
        @DisplayName("title이 공백이면 변경되지 않는다")
        void blankTitle() {
            Memo memo = createMemo();

            memo.update(null, "   ", null);

            assertEquals("라멘 맛집", memo.getTitle());
        }

        @Test
        @DisplayName("content를 빈 문자열로 보내면 내용이 비워진다")
        void emptyContent() {
            Memo memo = createMemo();

            memo.update(null, null, "");

            assertEquals("", memo.getContent());
        }

        @Test
        @DisplayName("값을 보내면 해당 값으로 변경된다")
        void updateAll() {
            Memo memo = createMemo();

            memo.update(MemoIconType.HOTEL, "숙소", "체크인 15시");

            assertEquals(MemoIconType.HOTEL, memo.getIcon());
            assertEquals("숙소", memo.getTitle());
            assertEquals("체크인 15시", memo.getContent());
        }
    }

    @Nested
    @DisplayName("소유자 확인")
    class Owner {

        @Test
        @DisplayName("작성자 본인이면 true")
        void owner() {
            assertTrue(createMemo().isOwnedBy(10L));
        }

        @Test
        @DisplayName("다른 사용자면 false")
        void notOwner() {
            assertFalse(createMemo().isOwnedBy(99L));
        }
    }

}
