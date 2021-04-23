package org.directtruststandards.timplus.server.boot;

import org.directtruststandards.timplus.common.crypto.CryptoUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

@SpringBootApplication
@ComponentScan({"org.directtruststandards.timplus.server.boot", "org.directtruststandards.timplus.server.springconfig",
		"org.directtruststandards.timplus.cluster"})
public class TIMPlusServerApplication 
{	
	@Autowired
	protected ApplicationContext ctx;
	
	static 
	{
	    CryptoUtils.registerJCEProviders();
	}
	
	public static void main(String[] args) 
	{
        SpringApplication application = new SpringApplication(TIMPlusServerApplication.class);
        
        /*
         * Placing a shutdown of the server here so the we can shutdown the server
         * before things like database connections and cache connections are destroyed.
         */
        application.addListeners((ApplicationListener<ContextClosedEvent>) event -> 
        {
        	try
        	{
	        	final XMPPServer server = event.getApplicationContext().getBean(XMPPServer.class);
	        	if (!server.isShuttingDown())
	        	{
	        		server.stop();
	        	}
        	}
        	catch (Exception e)
        	{
        		
        	}
        	
        });
        application.run(args);		
	}	
}
