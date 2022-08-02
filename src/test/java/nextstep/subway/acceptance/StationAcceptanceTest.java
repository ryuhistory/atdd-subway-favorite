package nextstep.subway.acceptance;

import static nextstep.subway.acceptance.MemberSteps.*;
import static nextstep.subway.acceptance.StationSteps.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.StationResponse;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

	private String adminAccessToken;
	private String memberAccessToken;

	@BeforeEach
	public void setUp() {
		super.setUp();
		adminAccessToken = 로그인_되어_있음(ADMIN_EMAIL, ADMIN_PASSWORD);
		memberAccessToken = 로그인_되어_있음(MEMBER_EMAIL, MEMBER_PASSWORD);
	}

	/**
	 * When admin이 지하철역을 생성하면
	 * Then 지하철역이 생성된다
	 * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
	 */
	@DisplayName("지하철역을 생성한다.")
	@Test
	void createStation() {
		// when
		ExtractableResponse<Response> response = 지하철역_생성_요청("강남역", adminAccessToken);

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

		// then
		List<String> stationNames = 지하철역_조회(adminAccessToken).jsonPath().getList("name", String.class);
		assertThat(stationNames).containsAnyOf("강남역");
	}

	/**
	 * Given admin이 2개의 지하철역을 생성하고
	 * When 지하철역 목록을 조회하면
	 * Then 2개의 지하철역을 응답 받는다
	 */
	@DisplayName("지하철역을 조회한다.")
	@Test
	void getStations() {
		// given
		지하철역_생성_요청("강남역", adminAccessToken);
		지하철역_생성_요청("역삼역", adminAccessToken);

		// when
		ExtractableResponse<Response> stationResponse = 지하철역_조회(adminAccessToken);

		// then
		List<StationResponse> stations = stationResponse.jsonPath().getList(".", StationResponse.class);
		assertThat(stations).hasSize(2);
	}

	/**
	 * Given admin이 지하철역을 생성하고
	 * When admin이 그 지하철역을 삭제하면
	 * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
	 */
	@DisplayName("지하철역을 제거한다.")
	@Test
	void deleteStation() {
		// given
		ExtractableResponse<Response> createResponse = 지하철역_생성_요청("강남역", adminAccessToken);

		// when
		지하철역_삭제(createResponse.header("location"), adminAccessToken);

		// then
		List<String> stationNames = 지하철역_조회(adminAccessToken).jsonPath().getList("name", String.class);
		assertThat(stationNames).doesNotContain("강남역");
	}

	/**
	 * When member가 지하철역을 생성하면
	 * Then 401 응답을 받는다
	 */
	@DisplayName("지하철역을 생성한다. By Member")
	@Test
	void createStationByMember() {
		// when
		ExtractableResponse<Response> response = 지하철역_생성_요청("강남역", memberAccessToken);

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	/**
	 * Given admin이 지하철역을 생성하고
	 * When member가 지하철역을 삭제하면
	 * Then 401 응답을 받는다
	 */
	@DisplayName("지하철역을 제거한다.By Member")
	@Test
	void deleteStationByMember() {
		// given
		ExtractableResponse<Response> createResponse = 지하철역_생성_요청("강남역", adminAccessToken);

		// when
		ExtractableResponse<Response> response = 지하철역_삭제(createResponse.header("location"), memberAccessToken);

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}
}