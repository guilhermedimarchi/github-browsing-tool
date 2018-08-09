package com.guilherme.githubbrowsingtool.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GithubApiService {
	
    @Value("${github.client_id}")
    private String clientId;

    @Value("${github.client_secret}")
    private String clientSecret;
	
	private static ParameterizedTypeReference<Map<String, String>> MAPDTO = new ParameterizedTypeReference<Map<String, String>>(){};

	public String getAccessToken(String code) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", code);
		HttpEntity<String> entity = new HttpEntity<String>(new HttpHeaders());
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, MAPDTO);
		
		if(response.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Error during request of access_token.");
		}
		
		return response.getBody().get("access_token");
	}
	
	public Map<String, String> getUserInfo(String accessToken) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.github.com/user")
				.queryParam("access_token", accessToken);
		
		HttpEntity<String> entity = new HttpEntity<String>(new HttpHeaders());
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, MAPDTO);
		
		if(response.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Error during request of user info.");
		}
		
		return response.getBody();
	}
	
	public List<Map<String, String>>  getUserRepos(String accessToken) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.github.com/user/repos")
				.queryParam("access_token", accessToken);
		
		HttpEntity<String> entity = new HttpEntity<String>(new HttpHeaders());
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
		
		if(response.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Error during request of user repos.");
		}
		return new ObjectMapper().readValue(response.getBody(), List.class);
	}

}
