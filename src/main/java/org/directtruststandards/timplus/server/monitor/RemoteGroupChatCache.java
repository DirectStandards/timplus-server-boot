package org.directtruststandards.timplus.server.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.jivesoftware.openfire.domain.DomainManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.smackx.muc.packet.MUCInitialPresence;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;
import org.xmpp.packet.Presence;

/**
 * Simple implementation to cache nick names of users from group chats hosted in remote servers.  Because
 * the mapping of a nickname to a real JID is handled in the group chat presence message, it is more efficient
 * to pick off presence messages then query a server for room information.  This can also lead to cache coherence issues,
 * so care must be taken in the cache.
 * @author Greg Meyer
 * @since 1.0
 */
public class RemoteGroupChatCache implements PacketInterceptor
{
	static RemoteGroupChatCache INSTANCE = null;
	
	static
	{
		INSTANCE = new RemoteGroupChatCache();
	}
	
	// Simple hash map for now, possibly look at backing with Redis 
	// or something more cohesive in the future
	protected Map<JID, JID> groupNickJIDMap;
	
	protected Map<JID, Map<JID, JID>> roomOccupantsMap;
	
	public static RemoteGroupChatCache getInstance()
	{
		return INSTANCE;
	}
	
	private RemoteGroupChatCache()
	{
		super();
		
		groupNickJIDMap = new HashMap<>();
		roomOccupantsMap = new HashMap<>();
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException
	{
		// We are looking for presence packets.
		if (packet instanceof Presence)
		{
			final Presence pres = Presence.class.cast(packet);
			
			final Presence.Type presType = pres.getType();
			
			// Intercept incoming presence messages from other servers but that have not yet
			// been delivered to the end points.  Regardless if the packet makes it to the end point,
			// an occupant has either entered of left the room.
			if (incoming && !processed)
			{
				if (presType == null || presType == Presence.Type.unavailable)
				{
					// check if the domain is remote... we only need to cache remote users
					if (!(DomainManager.getInstance().isRegisteredDomain(pres.getFrom().getDomain()) 
							|| DomainManager.getInstance().isRegisteredComponentDomain(pres.getFrom().getDomain())))
					{
						// check if this is an MUC event
						final PacketExtension mucPres = pres.getExtension(MUCInitialPresence.ELEMENT, MUCInitialPresence.NAMESPACE + "#user");
						if (presType == null && mucPres != null)
						{
							final Element itemEl = mucPres.getElement().element("item");
							if (itemEl != null)
							{
								// this is a presence entrance
								// add/update the cache
								synchronized(groupNickJIDMap)
								{
									groupNickJIDMap.put(packet.getFrom(), new JID(itemEl.attributeValue("jid")));
									
									// Bare JID of the from address will be the room
									Map<JID, JID> roomOccupants = roomOccupantsMap.get(packet.getFrom().asBareJID());
									if (roomOccupants == null)
										roomOccupants = new HashMap<>();
										
									roomOccupants.put(packet.getFrom(), new JID(itemEl.attributeValue("jid")));
									
									roomOccupantsMap.put(packet.getFrom().asBareJID(), roomOccupants);
								}
							}
						}
						// Handle all unavailable messages as we don't know if 
						// a group chat will have "x" extension or not.
						else if (presType == Presence.Type.unavailable)
						{
							synchronized(groupNickJIDMap)
							{
								groupNickJIDMap.remove(packet.getFrom());
								
								final Map<JID, JID> roomOccupants = roomOccupantsMap.get(packet.getFrom().asBareJID());
								if (roomOccupants != null)
								{
									// if the room is empty, then remove the room from our map
									roomOccupants.remove(packet.getFrom());
									if (roomOccupants.isEmpty())
										roomOccupantsMap.remove(packet.getFrom().asBareJID());
								}
								
							}
						}
					}
				}
			}
		}
	}
	
	public JID getRemoteNickNameJID(JID nickNameJID)
	{
		JID retVal = null;
		synchronized(groupNickJIDMap)
		{
			retVal =  groupNickJIDMap.get(nickNameJID);
		}
		
		return retVal;
	}
	
	public Map<JID,JID> getRemoteRoomOccupants(JID roomJID)
	{
		Map<JID,JID> retVal = null;
		synchronized(groupNickJIDMap)
		{
			retVal =  roomOccupantsMap.get(roomJID);
		}
		
		return (retVal != null) ? retVal : Collections.emptyMap();
	}
}
