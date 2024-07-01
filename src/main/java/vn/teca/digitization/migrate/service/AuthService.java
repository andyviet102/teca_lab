package vn.teca.digitization.migrate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${rest.services.idp}")
    private String idpEndpoint;

    public List<GrantedAuthority> findAuthorities(Jwt jwt) {
        try {
            String accessToken = jwt.getTokenValue();
            String userId = jwt.getClaimAsString("sub");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<?> entity = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(idpEndpoint)
                    .path("/authorities");

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.build(false).toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());
            ArrayNode json = (ArrayNode) jsonNode.get("data");

            return StreamSupport.stream(json.spliterator(), true)
                    .map(JsonNode::asText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}
