package com.guilherme.githubbrowsingtool.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.guilherme.githubbrowsingtool.dto.RepoDTO;
import com.guilherme.githubbrowsingtool.service.GithubApiService;

@Controller
public class BrowsingController {
	
	@Value("${github.client_id}")
	private String clientId;
	
	@Value("${github.redirect_uri}")
	private String redirectUri;
	
	@Value("${github.scopes}")
	private String scopes;
	
	@Autowired
	private GithubApiService githubApiService;
	
	/**
	 * Gives to the view the URI to authorize using Github Oauth.
	 * Uses the client_id, redirect_uri and scopes provided in application.properties
	 * @param model
	 * @return login view
	 */
	@GetMapping("/")
	public String login(Model model) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/authorize");
		uriBuilder.queryParam("client_id", clientId);
		uriBuilder.queryParam("redirect_uri", redirectUri);
		uriBuilder.queryParam("scope", scopes.split(","));
		model.addAttribute("urlToGithubOauth", uriBuilder.toUriString());
		return "login";
	}
	
	/**
	 * When the redirect uri from Github Oatuh is called, receives the code to request the access_token
	 * and get user info and repositories list. 
	 * @param model
	 * @param code
	 * @return index view
	 * @throws Exception
	 */
	@GetMapping("/index")
	public String index(Model model, @RequestParam("code") String code) throws Exception {
		
		String accessToken = githubApiService.getAccessToken(code);
		Map<String, String> userInfo = githubApiService.getUserInfo(accessToken);
		
		int page = 1;
		List<Map<String, String>> userRepos = new ArrayList<>();
		boolean noMoreRepos = false;
		do {
			List<Map<String, String>> aux = githubApiService.getUserRepos(accessToken, page++);
			if(aux.isEmpty()) noMoreRepos = true;
			userRepos.addAll(aux);
		} while (!noMoreRepos);
		
		List<RepoDTO> repos = userRepos.stream()
				.map(item -> new RepoDTO(item.get("name"), item.get("html_url")))
				.collect(Collectors.toList());
		
		model.addAttribute("username", userInfo.get("login"));
		model.addAttribute("avatar_url", userInfo.get("avatar_url"));
		model.addAttribute("repos", repos);
		
		return "index";
	}
	

}
