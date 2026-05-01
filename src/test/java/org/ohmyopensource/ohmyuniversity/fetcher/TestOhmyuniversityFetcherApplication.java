package org.ohmyopensource.ohmyuniversity.fetcher;

import org.springframework.boot.SpringApplication;

public class TestOhmyuniversityFetcherApplication {

	public static void main(String[] args) {
		SpringApplication.from(OhmyuniversityFetcherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
