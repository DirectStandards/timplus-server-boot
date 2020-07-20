package org.directtruststandards.timplus.server.springconfig;

import org.jivesoftware.openfire.filetransfer.proxy.credentials.ProxyServerCredentialManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OnceTimeProxyCredentialCleaner
{
	@Scheduled(fixedRateString = "${timplus.proxyCredentials.pruner.period:3600000}")
	public void pruneExpiredCredentials()
	{
		ProxyServerCredentialManager.getInstance().pruneExpiredCredentials();
	}
}
