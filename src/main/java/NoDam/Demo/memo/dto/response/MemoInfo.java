package NoDam.Demo.memo.dto.response;

import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoInfo {
    private Long id;
    private MemoIconType icon;
    private String title;
    private String content;

    public static MemoInfo from(Memo memo) {
        return MemoInfo.builder()
                .id(memo.getId())
                .icon(memo.getIcon())
                .title(memo.getTitle())
                .content(memo.getContent())
                .build();
    }
}
