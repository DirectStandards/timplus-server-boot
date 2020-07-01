package org.directtruststandards.timplus.server.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.directtruststandards.timplus.server.boot", "org.directtruststandards.timplus.server.springconfig"})
public class DirectImServerApplication 
{	
	static {

	    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
	}
	
	public static void main(String[] args) 
	{
		SpringApplication.run(DirectImServerApplication.class, args);
	}

}
