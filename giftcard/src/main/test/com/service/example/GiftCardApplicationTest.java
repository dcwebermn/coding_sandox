package giftcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.beans.Transient;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GiftCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void returnAGiftCard() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards/25", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(25);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(49.99);
    }

    //this test should fail since the id is invalid
    @Test
    void returnAGiftCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    //need dirty data here to avoid conflict with other test assertions
    @Test
    @DirtiesContext
    void createAGiftCard() {
        GiftCard newGiftCard = new GiftCard(null, 250.00, "aaaa");
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .postForEntity("/giftcards", newGiftCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI endpointOfNewGiftCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity(endpointOfNewGiftCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }

    @Test
    void returnListOfAllGiftCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int giftCardCount = documentContext.read("$.length()");
        assertThat(giftCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(25, 26, 27);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(49.99, 100.00, 150.00);
    }

    @Test
    void returnAPageOfGiftCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void returnASortedPageOfGiftCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void returnASortedPageOfGiftCardsWithDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(49.99, 100.00, 150.00);
    }

    //test should fail due to wrong credentials - code 401
    @Test
    void returnGiftCardUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "zzzz")
                .getForEntity("/giftcards/25", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("aaaa", "BAD-PASSWORD")
                .getForEntity("/giftcards/25", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //test should return a 403
    @Test
    void rejectUsersNotInSystem() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user-has-no-cards", "zzzz")
                .getForEntity("/giftcards/25", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    //test should return a 404
    @Test
    void denyAccessToGiftCardsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("aaaa", "bbbb")
                .getForEntity("/giftcards/30", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateGiftCard() {
        GiftCard giftCardUpdate = new GiftCard(null, 19.99, null);
        HttpEntity<GiftCard> request = new HttpEntity<>(giftCardUpdate);
        ResponseEntity<Void> response = restTemplate.withBasicAuth("aaaa", "bbbb")
        .exchange("giftcards/25", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    //need to work with dirty data to avoid conflicts with other tests
    @Test
    @DirtiesContext
    void deleteGiftCard() {
        ResponseEntity<Void> response = restTemplate.withBasicAuth("aaaa", "bbbb")
        .exchange("/giftcards/25", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("aaaa", "bbbb")
        .getForEntity("/giftcards/99", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }
}
