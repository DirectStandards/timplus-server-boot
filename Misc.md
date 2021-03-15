# Miscellaneous Setting

This section outlines some miscellaneous setting in the TIM+ server application.

## Message Monitoring Timeout

A key value proposition in the TIM+ specification is knowing the delivery status of a given message.  The TIM+ server application tracks each message (including group chat messages) for delivery status and automatically generates an error message addressed to the message's original sender if a delivery, stored offline, or other error message is not received from the receiving.  The server set's a default timeout of 1 hour before generating an error message, but can be overridden using the following example setting in the server application's application.yml or bootstrap.yml file:

```
timplus.monitor.condition.generalConditionTimeout: 3600000
```

The setting sets the timeout in ms, so the example above would timeout messages after 1 hour.

## VCards

The default configuration of the TIM+ server does not allow TIM+ client applications to update VCard information using the mechansims described in [XEP-0054](https://xmpp.org/extensions/xep-0054.html).  This is because production policies will mostly likely require VCard information to be set and updated by an authoritative source using tools such as the TIM+ server admin console.  To allow TIM+ clients to update their own VCard information, set the following configuration in the server application's application.yml or bootstrap.yml file:

```
timplus.vcard.allowClientSet: true
```

## CRLs

The TIM+ specification requires that all certificates be checked for revocation before they are considered "trusted."  The TIM+ server does this by reading CRL attribute of a peer's certificate and downloads (and caches) the CRL file.  It is very possibly in the case of development and testing systems where certificates do not implement a full PKI infrastructure that CRLs may not be available or even implemented.  In these cases, the CRL checking feature can be disabled by setting the following configuration in the server application's application.yml or bootstrap.yml file:

**NOTE:**  CRL checking should NOT be disabled in production systems, and doing so it technically a violation of the TIM+ specification.

```
timplus.secandtrust.crl.ignoreCLRChecking: true
```

For more efficient CRL processing, the server application stores and cache retrieved CRL files in the server's default directory, but can be overridden using the following example setting in the server application's application.yml or bootstrap.yml file:

```
timplus.secandtrust.crl.fileCahceLoc: <location for CRL files>
```

## File Transfer Proxy

The default configuration for the file transfer proxy *stream hosts* uses all IP addresses bound to all network interfaces on the server.  This is typically not useful as the default IP addresses are not generally accessible from the public internet.  To configure the IP(s) or hostname(s) of the file transfer proxy server(s), set the following configuration in the server application's application.yml or bootstrap.yml file.  If you have mulitple IPs or hostnames, separate each with a comma (","). 

```
timplus.filetransfer.proxy.host: <server of ip>
```

## PKCS 11 (Hardware Security Module)

The TIM+ application server support connecting to a PKCS 11 token for the purpose of utilizing asymmetric private keys.  Because the underlying configuration utilizes Java JCE implementations, there are several different combinations of configuration.  One of the most common configuration utilizes the Gemalto Luna SA networked hardware security module.  The following is an example configuration to configure a Luna SA module.  The configuration can be done in the application.yml or bootstrap.yml file.

```
timplus:
  keystore:
# Enables the use of a PKCS 11 token (i.e. hardward security module)
    hsmpresent: true
# The pin to login to the hardware security module  
    keyStonePin: somepassword
# The Luna Slot to use
    keyStoreSourceAsString: slot0
# The symmetric key alias on the hardware security module to encrypt a PKCS 12 file
    keyStorePassPhraseAlias: keyStorePassPhrase
# The symmetric key alias on the hardware security module to wrap private keys    
    privateKeyPassPhraseAlias: privateKeyPassPhrase
```

## Proxy Credential Pruner

The file transfer proxy uses one time credentials to secure connections to the proxy services.  These credentials are automatically removed after a successful login, but need to be removed if credentials are never successfully used.  By default, a pruner task run every hour to clean up expired/unused credentials, but can be overridden using the following example setting in the server application's application.yml or bootstrap.yml file:

```
timplus.proxyCredentials.pruner.period: 3600000
```

The setting sets the timeout in ms, so the example above would prune credentials ever 1 hour.

## Trust Bundle Refreshing

Trust bundles are set to be refreshed based on a configuration in the admin console, however a background task is used to check if a given bundle is due to be refreshed.  By default, this taks runs every 15 minutes, but can be overridden using the following example setting in the server application's application.yml or bootstrap.yml file:

```
timplus.bundles.refresher.period: 3600000
```

The setting sets the timeout in ms, so the example above would check for bundle refreshes every 15 minutes.