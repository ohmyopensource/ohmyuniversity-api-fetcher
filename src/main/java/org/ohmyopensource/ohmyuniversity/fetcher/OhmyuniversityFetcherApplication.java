package org.ohmyopensource.ohmyuniversity.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OhmyuniversityFetcherApplication {

	public static void main(String[] args) {
		System.out.println("=== ENV CHECK ===");
		System.out.println("POSTGRES_URL: " + System.getenv("POSTGRES_URL"));
		System.out.println("POSTGRES_USERNAME: " + System.getenv("POSTGRES_USERNAME"));
		System.out.println("POSTGRES_PASSWORD: " + System.getenv("POSTGRES_PASSWORD"));
		System.out.println("=================");
		System.out.println("Working dir: " + System.getProperty("user.dir"));
		SpringApplication.run(OhmyuniversityFetcherApplication.class, args);
	}

}
