package NoDam.Demo.ai;

public enum Prompt {

    /**
     * args[0] : AiRecommendPlaceRequestDto (JSON)
     * - scheduleType : LOOSE | NORMAL | TIGHT
     * - themeType    : FOOD | HEALING | LANDMARK | ACTIVITY
     * - previousPlace: PlaceInfo (nullable) — 직전 일정 장소
     * - nextPlace    : PlaceInfo (nullable) — 직후 일정 장소
     * - candidates   : List<PlaceCandidate>
     *     - place               : PlaceInfo (id, name, placeType, lat, lon, priceType, ...)
     *     - travelTimeSeconds   : 사용자 현재 위치 → 해당 장소 이동 시간(초)
     *     - travelDistanceMeters: 사용자 현재 위치 → 해당 장소 이동 거리(m)
     * args[1] : responseFormat (AiService가 자동 생성)
     */
    RECOMMEND_PLACE("""
            당신은 여행 일정 전문가입니다.
            아래 JSON 데이터를 분석하여 후보 장소 중 가장 적합한 5개를 선택하고 각 장소의 방문 시간을 제안하세요.

            [선택 기준]
            - scheduleType이 TIGHT이면 이동 시간이 짧은 장소를 우선합니다.
            - scheduleType이 LOOSE이면 여유로운 동선을 고려합니다.
            - themeType에 맞는 placeType의 장소를 우선합니다.
            - previousPlace와 nextPlace가 있을 경우 동선의 자연스러운 흐름을 고려합니다.
            - travelTimeSeconds가 null이면 이동 시간 정보가 없는 장소입니다.

            [입력 데이터]
            %s

            [응답 형식]
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            %s
            """),

    /**
     * args[0] : AiBuildDayScheduleDto (JSON)
     * - scheduleType       : LOOSE | NORMAL | TIGHT
     * - themeType          : FOOD | HEALING | LANDMARK | ACTIVITY
     * - necessaryPlaces    : List<PlaceItem> (id, placeType, name, lon, lat) — 반드시 포함해야 할 장소
     * - fixedPlans         : List<FixedPlanItem> (startTime, endTime, place) — 고정된 시간대 (공항, 호텔)
     * - previousDaysPlaces : List<PlaceItem> — 이전 날짜에 이미 선정된 장소 목록
     * - candidates         : Map<PlaceType, List<PlaceItem>> (RESTAURANT | CAFE | SIGHT | SHOP)
     * args[1] : responseFormat (AiService가 자동 생성)
     */
    BUILD_DAY_SCHEDULE("""
            당신은 여행 일정 전문가입니다.
            아래 JSON 데이터를 분석하여 하루 여행 일정을 구성하세요.

            [구성 기준]
            - 첫 날은 공항에서 시작하며, 마지막 날은 공항에서 일정이 끝납니다.
            - 첫 날과 마지막 날을 제외하고, 오전 9시에 일정을 시작하며, 오후 9시에 호텔에서 일정을 끝냅니다.
            - scheduleType별로 sight 장소의 개수를 조절합니다.
            - - loose : 2~3, normal : 3~4, tight : 4~5
            - themeType에 맞는 장소를 우선 배치합니다.
            - - food : RESTAURANT 우선, HEALING : CAFE, HOTEL 우선, LANDMARK : sight 우선
            - RESTAURANT은 식사 시간을 고려하여 선정합니다.
            - previousDaysPlaces에 포함된 장소는 선정하지 않습니다. (호텔, 공항 제외)
            - fixedPlans의 시간대와 겹치지 않도록 일정을 배치합니다. (호텔, 공항 제외)
            - 장소 간 이동 시간을 고려하여 시간이 겹치지 않도록 합니다.
            - 이미 선정된 장소을 제외하고는 place id에 null값을 허용하지 않습니다.

            [입력 데이터]
            %s

            [응답 형식]
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            %s
            """),

    /**
     * args[0] : AiRegionAssignRequestDto (JSON)
     * - dates         : List<String> (yyyy-MM-dd)
     * - regions       : List<RegionInfo> (regionId, name, lat, lon, placeCount)
     * - necessaryPlaces : List<PlaceCoordinate>
     * - airport       : PlaceCoordinate (nullable)
     * - hotel         : PlaceCoordinate (nullable)
     * args[1] : responseFormat (AiService가 자동 생성)
     */
    ASSIGN_REGION("""
            당신은 여행 일정 전문가입니다.
            아래 JSON 데이터를 분석하여 각 날짜에 방문할 region을 배정하세요.

            [배정 기준]
            - 같은 region은 연속된 날짜에 배치하여 불필요한 이동을 줄입니다.
            - placeCount가 많은 region에 더 많은 날짜를 배정합니다.
            - airport가 있으면 첫날과 마지막날은 airport와 가까운 region을 배정합니다.
            - hotel이 있으면 hotel 위치를 동선 최적화에 활용합니다.
            - 모든 날짜에 반드시 하나의 regionId를 배정해야 합니다.

            [입력 데이터]
            %s

            [응답 형식]
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            %s
            """);

    private final String preparedPrompt;

    Prompt(String preparedPrompt) {
        this.preparedPrompt = preparedPrompt;
    }

    public String toPrompt(String... args) {
        return String.format(preparedPrompt, (Object[]) args);
    }

}
