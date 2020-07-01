package org.directtruststandards.timplus.server.monitor;

import java.util.Collection;

import org.directtruststandards.timplus.monitor.tx.model.Tx;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;

public interface PacketMonitor extends PacketInterceptor, OfflineMessageListener 
{
	public void trackTx(Tx tx);
	
	public void sendMonitorExperationErrorMessages(Collection<String> errorMessage);
}
