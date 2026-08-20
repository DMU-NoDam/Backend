package NoDam.Demo.common.type;

public enum PlaceStatus {
    CREATED, // 앱 lazy 생성(Google 최소 정보만 있는 상태)
    CRAWLED  // 크롤 파이프라인 export로 보강 완료(en/jp·score·테마 등 채워짐)
}
