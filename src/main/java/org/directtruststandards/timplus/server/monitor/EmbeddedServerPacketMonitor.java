package org.directtruststandards.timplus.server.monitor;

import static org.springframework.integration.support.MessageBuilder.withPayload;

import java.util.Collection;

import org.directtruststandards.timplus.monitor.tx.TxParser;
import org.directtruststandards.timplus.monitor.tx.model.Tx;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class EmbeddedServerPacketMonitor extends AbstractPacketMonitor implements MessageHandler
{
	protected MessageChannel trackingChannel;
	
	public EmbeddedServerPacketMonitor(TxParser parser, MessageChannel trackingChannel)
	{
		super(parser);
		
		this.trackingChannel = trackingChannel;
	}

	@Override
	public void trackTx(Tx tx)
	{
		trackingChannel.send(withPayload(tx).build());
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException
	{
		if (message.getPayload() instanceof Collection)
		{
			@SuppressWarnings("unchecked")
			final Collection<String> errorMessages = (Collection<String>)message.getPayload();
			
			this.sendMonitorExperationErrorMessages(errorMessages);
		}
		
	}
	
	
}
