# DNS Configuration

Out of the box, the TIM+ reference implementation server can deliver messages between users within domains managed by a single server without any additional DNS settings.  However, the core value proposition of TIM+ is to facilitate messaging between disparate organizations which will generally run on different TIM+ server in different TIM+ messaging domains.  For TIM+ server to discover each other, they must publish information regarding their location in DNS.

## DNS Domain Registration

The first thing you must do is register your TIM+ domain with DNS registrar.  TIM+ domains can be any valid domain, but will generally be something that associates the domain with a health care organization.  For example, if health care organization's name is 'HealthCareOrg', it might register a domain name 'HealthCareOrg.com.'  There are several commercially available DNS registrars like GoDaddy or Amazon Route 53 that can be used to register a domain.

## Server A Record Creation

After registering your domain, you will need to create DNS A record that maps to the IP address of the server running your TIM+ implementation (or possibly to list of servers, a VIP, or a load balancer).  The A record will be the target of the SRV records described in the next section.  For our example of HeathCareOrg.com, we will create an A record with the name of xmppserver.healthcareorg.com with an IP address of 39.67.29.4.  Each DNS registrar should have appropriate tools for creating an A record.

## Server SRV Record Creation

TIM+ uses DNS SRV records to discover servers associated to TIM+ domains as described in section 3 of [RFC 6120](https://tools.ietf.org/html/rfc6120).  There are two qualifications of SRV records for TIM+ domains: client and server SRV records.  TIM+ only requires server SRV records to be created for the dynamic discovery of servers associated to external domains.  Client SRV records are provided for the convenience of TIM+ client applications to dynamically discover the server associated with a TIM+ domain.  It is very possible that a TIM+ client may use alternate methods to configure the name of the TIM+ server or may use a different edge protocol other than the IP based XMPP protocol binding.

### SRV Server Records

Each TIM+ domain requires three SRV records: one for the domain itself one for the groupchat subdomain, and one for the file transfer subdomain.  Using our example of HeathCareOrg.com, we could generate the following two SRV records:

##### Main TIM+ domain
```
Name: _xmpp-server._tcp.heathcareorg.com
Priority: 1
Weight: 10
Port: 5269
Server Host: xmppserver.healthcareorg.com
```

##### GroupChat TIM+ sub domain
```
Name: _xmpp-server._tcp.groupchat.heathcareorg.com
Priority: 1
Weight: 10
Port: 5269
Server Host: xmppserver.healthcareorg.com
```

##### File Transfer TIM+ sub domain
```
Name: _xmpp-server._tcp.ftproxystream.heathcareorg.com
Priority: 1
Weight: 10
Port: 5269
Server Host: xmppserver.healthcareorg.com
```

### SRV Client Records

If you would like your TIM+ server to be dynamically discoverable through DNS, you will need a client SRV record.  Using our example of HeathCareOrg.com, we could generate the following SRV records:

```
Name: _xmpp-client._tcp.heathcareorg.com
Priority: 1
Weight: 10
Port: 5222
Server Host: xmppserver.healthcareorg.com
```