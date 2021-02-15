package org.directtruststandards.timplus.server.boot;

import org.directtruststandards.timplus.common.crypto.CryptoUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.directtruststandards.timplus.server.boot", "org.directtruststandards.timplus.server.springconfig",
		"org.directtruststandards.timplus.cluster"})
public class TIMPlusServerApplication 
{	
	static 
	{
	    CryptoUtils.registerJCEProviders();
	}
	
	public static void main(String[] args) 
	{
		SpringApplication.run(TIMPlusServerApplication.class, args);
	}

}
