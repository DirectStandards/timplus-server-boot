package org.directtruststandards.timplus.server.springconfig;

import org.directtruststandards.timplus.monitor.condition.TxReleaseStrategy;
import org.directtruststandards.timplus.monitor.condition.TxTimeoutCondition;
import org.directtruststandards.timplus.monitor.impl.DefaultTxParser;
import org.directtruststandards.timplus.monitor.spring.RouteComponents;
import org.directtruststandards.timplus.monitor.spring.ScheduledRouteReaper;
import org.directtruststandards.timplus.server.monitor.EmbeddedServerPacketMonitor;
import org.directtruststandards.timplus.server.monitor.PacketMonitor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Import({RouteComponents.class, ScheduledRouteReaper.class})
public class MessageMonitorConfig
{
	@Bean
	public IntegrationFlow monitorFlow(@Qualifier("monitorStart") MessageChannel inputChannel, @Qualifier("monitorStart") MessageChannel receive, CorrelationStrategy correlationStradegy, 
			TxReleaseStrategy releaseStrategy, TxTimeoutCondition timeoutCondition, MessageGroupStore messageGroupStore, 
			LockRegistry lockRegistry, Transformer transformer)
	{
		return IntegrationFlows.from(inputChannel)
		.aggregate(a -> a.correlationStrategy(correlationStradegy)
		       .releaseStrategy(releaseStrategy)
		       .groupTimeout(g -> timeoutCondition.getTimeout(g))
		       .sendPartialResultOnExpiry(true)
		       .messageStore(messageGroupStore))	
		.filter(releaseStrategy)
		.transform(transformer)
		.handle((MessageHandler)monitoringInterceptor())
		.get();
	}	

	
	@Bean
	public PacketMonitor monitoringInterceptor()
	{
		return new EmbeddedServerPacketMonitor(new DefaultTxParser(), monitorStart());
	}
	
	@Bean
	public MessageChannel monitorStart()
	{
	    return MessageChannels.direct("monitorStart")
                .get();
	}
	
	@Bean
	public QueueChannel monitorOut() 
	{
	    return MessageChannels.queue("monitorOut")
	                        .get();
	}
}
