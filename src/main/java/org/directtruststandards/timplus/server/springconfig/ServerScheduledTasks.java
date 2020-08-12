package org.directtruststandards.timplus.server.springconfig;

import java.util.Collection;

import org.jivesoftware.openfire.filetransfer.proxy.credentials.ProxyServerCredentialManager;
import org.jivesoftware.openfire.trustbundle.TrustBundle;
import org.jivesoftware.openfire.trustbundle.TrustBundleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerScheduledTasks
{
	private static final Logger Log = LoggerFactory.getLogger(ServerScheduledTasks.class);	
	
	@Scheduled(fixedRateString = "${timplus.proxyCredentials.pruner.period:3600000}")
	public void pruneExpiredCredentialsTask()
	{
		ProxyServerCredentialManager.getInstance().pruneExpiredCredentials();
	}
	
	@Scheduled(fixedRateString = "${timplus.bundles.refresher.period:900000}")
	public void updateBundlesTask()
	{
		try
		{
			final Collection<TrustBundle> bundles = TrustBundleManager.getInstance().getTrustBundles(false);
			
			for (TrustBundle bundle : bundles)
			{
				try
				{
					TrustBundleManager.getInstance().refreshBundle(bundle);
				}
				catch (Exception e)
				{
					Log.warn("Failed to update trust bundle " + bundle.getBundleName(), e);
				}
			}
		}
		catch (Exception e)
		{
			Log.warn("Failed to get trust bundle list for refreshing", e);
		}
		
	}
}
