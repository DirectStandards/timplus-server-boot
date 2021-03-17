package org.directtruststandards.timplus.server.monitor;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.directtruststandards.timplus.monitor.tx.TxParser;
import org.directtruststandards.timplus.monitor.tx.model.Tx;
import org.directtruststandards.timplus.monitor.tx.model.TxDetail;
import org.directtruststandards.timplus.monitor.tx.model.TxDetailType;
import org.directtruststandards.timplus.monitor.tx.model.TxStanzaType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.domain.DomainManager;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.spi.RemoteMUCCache;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.smackx.amp.AMPDeliverCondition;
import org.jivesoftware.smackx.amp.packet.AMPExtension;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;

public abstract class AbstractPacketMonitor implements PacketMonitor
{
	private static final Logger Log = LoggerFactory.getLogger(AbstractPacketMonitor.class);
	
	protected TxParser parser; 
	
	public AbstractPacketMonitor(TxParser parser)
	{
		super();
		
		this.parser = parser;
		
	}
	
	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException
	{
		/*
		 * The boolean incoming is misleading.  It indicates if the message is incoming to the server from a connected client
		 * or external server (true) of if it is destined to an edge client (false)
		 */
		try
		{
			if (packet instanceof Message)
			{
				final Message messagePacket = Message.class.cast(packet);
				final Tx tx = parser.parseStanza(packet.toXML());
				if (tx != null)
				{
					// Make sure this is a chat or group chat message.  We also want to start tracking the
					// message before the rest of the server has a chance to process or drop the message
					if (incoming && !processed && tx.getStanzaType() == TxStanzaType.MESSAGE)
					{
						// ensure we have a from detail
						final TxDetail fromDetail = tx.getDetail(TxDetailType.FROM);
						if (fromDetail != null)
						{
							// make sure we own the source domain
							final Jid from = JidCreate.from(fromDetail.getDetailValue()).asBareJid();
							if (DomainManager.getInstance().isRegisteredDomain(from.getDomain().toString()))
							{
								if (messagePacket.getType() == Message.Type.groupchat)
								{
									// group chats are a special case because they can contain multiple recipients.
									// Need to get the full list of recipients (real JIDs) from the room
									
									// Bare JID is the room name
									final Map<JID, JID> roomRecipients = getRoomParticipants(messagePacket.getTo().asBareJID());
									final StringBuilder recipBuilder = new StringBuilder();
									
									final Iterator<JID> recipIter = roomRecipients.values().iterator();
									while (recipIter.hasNext())
									{
										recipBuilder.append(recipIter.next().toString());
										if (recipIter.hasNext())
											recipBuilder.append(",");
									}
									final TxDetail recipDetail = tx.getDetail(TxDetailType.RECIPIENTS);
									if (recipDetail != null)
										recipDetail.setDetailValue(recipBuilder.toString());
								}
								// track the message
								if (Log.isDebugEnabled())
									Log.debug("Tracking outgoing message from " + packet.getFrom() + " to " + packet.getTo());

								trackTx(tx);
							}
						}
					}
					
					else if (!incoming) 
					{
						// make sure we own the destination
						final TxDetail toDetail = tx.getDetail(TxDetailType.RECIPIENTS);
						
						if (toDetail != null)
						{		
							final JID to = new JID(toDetail.getDetailValue()).asBareJID();
							if (DomainManager.getInstance().isRegisteredDomain(to.getDomain().toString()))
							{
								// check for AMP or Error Messages and track them
								if (!processed && (tx.getStanzaType() == TxStanzaType.AMP || tx.getStanzaType() == TxStanzaType.MESSAGE_ERROR))
								{
									if (Log.isDebugEnabled())
									{
										if (tx.getStanzaType() == TxStanzaType.AMP)
											Log.debug("Tracking AMP from " + packet.getFrom() + " to " + packet.getTo());
										else
											Log.debug("Tracking error from " + packet.getFrom() + " to " + packet.getTo());
									}
									//trackingChannel.send(withPayload(tx).build());
									trackTx(tx);
								}
								// check for messages that have been delivered to the edge client
								else if (processed && tx.getStanzaType() == TxStanzaType.MESSAGE)
								{ 
									// send an AMP message the message was delivered
									if (Log.isDebugEnabled())
										Log.debug("Message was delivered to final destination: from " + packet.getFrom() + " to " + packet.getTo());

									final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
									final Packet msg = generateDeliveryAMPMessage(messagePacket, AMPDeliverCondition.Value.direct.name());
									
									final JID ampPacketTo = msg.getTo();
									
									if (ampPacketTo != null)
										routingTable.routePacket(ampPacketTo, msg, false);
								}
							}
						}
					}
				}
			}
			
		}
		catch (Exception e)
		{
			
		}
	}
	
	
	
	@Override
	public void messageBounced(Message message)
	{
		/* no-op */
	}

	@Override
	public void messageStored(Message message)
	{
		if (Log.isDebugEnabled())
			Log.debug("Message put in offline storage: from " + message.getFrom() + " to " + message.getTo());

		// only send an AMP message is we are storing a MESSAGE
		// sending an AMP for other types of messages can result in infinite message loops
		final Tx tx = parser.parseStanza(message.toXML());
		if (tx != null && tx.getStanzaType() == TxStanzaType.MESSAGE)
		{
			
			final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
			final Packet msg = generateDeliveryAMPMessage(message, AMPDeliverCondition.Value.stored.name());
			
			final JID ampPacketTo = message.getFrom().asBareJID();
			
			if (Log.isDebugEnabled())
				Log.debug("Generating and sending storage offline AMP packet message  from " + ampPacketTo.toString() + " to " + msg.getTo());
			
			routingTable.routePacket(ampPacketTo, msg, false);
		}
	}


	@Override
	public void sendMonitorExperationErrorMessages(Collection<String> errorMessages)
	{
		for (String msg : errorMessages)
		{
			try
			{
				final SAXReader saxBuilder = new SAXReader();
				final Document document = saxBuilder.read(new StringReader(msg));
				
				final Packet packet = new Message(document.getRootElement());
				
				final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
				
				routingTable.routePacket(packet.getTo(), packet, false);
			}
			catch (Exception e)
			{
				Log.warn("Failed to send message expiration error message.", e);
			}
		}
		
	}
	
	protected Packet generateDeliveryAMPMessage(Message originalPacket, String value)
	{
		final Message msg = new Message();
		
		msg.setFrom(originalPacket.getTo());
			
		//
		// For group chats, the message needs to be sent to the real JID and not the 
		// the nickname
		if (originalPacket.getType() == Message.Type.groupchat)
			msg.setTo(nickNameToUserJID(originalPacket.getFrom()));
		else
			msg.setTo(originalPacket.getFrom().asBareJID());
		msg.setID(originalPacket.getID());
		
		final PacketExtension amp = new PacketExtension(AMPExtension.ELEMENT, AMPExtension.NAMESPACE);
		final Element ampElement = amp.getElement();
		ampElement.addAttribute("status", AMPExtension.Status.notify.name());
		

		ampElement.addAttribute("to", originalPacket.getTo().asBareJID().toString());
		
		// Per the TIM+ spec, group chat AMP message will have the From attribute in the rule element
		// set to the original senders JID (not their nickname)
		if (originalPacket.getType() == Message.Type.groupchat)
		{
			final JID originalSendersJID = nickNameToUserJID(originalPacket.getFrom());
			if (originalSendersJID != null)
			ampElement.addAttribute("from", originalSendersJID.toString());
		}
		else
			ampElement.addAttribute("from", originalPacket.getFrom().toString());
		
		final Element ruleElement = ampElement.addElement(new QName(AMPExtension.Rule.ELEMENT));
		ruleElement.addAttribute(AMPExtension.Action.ATTRIBUTE_NAME, AMPExtension.Action.notify.name());
		ruleElement.addAttribute(AMPExtension.Condition.ATTRIBUTE_NAME, AMPDeliverCondition.NAME);
		ruleElement.addAttribute("value", value);
		
		msg.addExtension(amp);
		
		return msg;
	}
	
	protected Map<JID, JID> getRoomParticipants(JID roomJID)
	{		
		final Map<JID, JID> retVal = new HashMap<>();
		
        final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
        final boolean isLocal = routingTable.hasComponentRoute(roomJID);

        if (isLocal)
        {
    		// rooms owned by this server can be quickly looked up
    		// in the local MUC Room manager
            final MultiUserChatService mucService = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(new JID(roomJID.getDomain()));
            if (mucService != null)
            {
            	final MUCRoom room = mucService.getChatRoom(roomJID.getNode());
            	if (room != null)
            		for (MUCRole occupant : room.getOccupants())
            			retVal.put(new JID(roomJID.getNode(), roomJID.getDomain(), occupant.getNickname()), occupant.getUserAddress());
            }
        }
        else
        {
        	return RemoteMUCCache.getInstance().getRemoteRoomOccupants(roomJID);
        }
		
		return retVal;
	}
	
	protected JID nickNameToUserJID(JID nickNameJID)
	{
		// the bare JID of the nickname is the room JID
        final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
        final boolean isLocal = routingTable.hasComponentRoute(nickNameJID.asBareJID());
        
        if (isLocal)
        {
        	for (Map.Entry<JID, JID> roomParticipant : getRoomParticipants(nickNameJID.asBareJID()).entrySet())
        		if (roomParticipant.getKey().equals(nickNameJID))
        			return roomParticipant.getValue();
        }
        else
        {
        	return RemoteMUCCache.getInstance().getRemoteNickNameJID(nickNameJID);
        }
        			
		return null;
	}
}
