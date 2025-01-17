import com.toty.user.domain.User;
import com.toty.user.domain.UserRepository;
import com.toty.user.presentation.dto.request.UserSignUpRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserApiE2ETest extends BaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("truncate table users");
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("회원 가입")
    void signUp() {
        UserSignUpRequest signUpRequest = new UserSignUpRequest("test@gmail.com", "test123");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when()
                .post("/api/users/signup")
                .then()
                .statusCode(200)
                .body("email", Matchers.equalTo("test@gmail.com"))
                .body("password", Matchers.equalTo("test123"));
    }

    @Test
    @DisplayName("내 정보 찾기")
    void getUserInfo() {
        // Given
        User existingUser = new User("test@gmail.com", "test123");
        Long userId = userRepository.save(existingUser).getId();

        // When & Then
        RestAssured.given()
                .pathParam("id", userId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(200)
                .body("email", Matchers.equalTo("test@gmail.com"));
    }
}
