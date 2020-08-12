package org.directtruststandards.timplus.server.springconfig;

import org.directtruststandards.timplus.common.crypto.KeyStoreProtectionManager;
import org.directtruststandards.timplus.common.crypto.exceptions.CryptoException;
import org.directtruststandards.timplus.common.crypto.impl.BootstrappedKeyStoreProtectionManager;
import org.directtruststandards.timplus.common.crypto.impl.BootstrappedPKCS11Credential;
import org.directtruststandards.timplus.common.crypto.impl.StaticCachedPKCS11TokenKeyStoreProtectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyStoreProtectionMgrConfig
{
	  private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreProtectionMgrConfig.class);	
	
	  @Value("${timplus.keystore.keyStorePin:som3randomp!n}")	
	  private String keyStorePin;
	  
	  @Value("${timplus.keystore.keyStoreType:Luna}")	
	  private String keyStoreType;
	  
	  @Value("${timplus.keystore.keyStoreSourceAsString:slot:0}")	
	  private String keyStoreSourceAsString;
	  
	  @Value("${timplus.keystore.keyStoreProviderName:com.safenetinc.luna.provider.LunaProvider}")	
	  private String keyStoreProviderName;
	  
	  @Value("${timplus.keystore.keyStorePassPhraseAlias:keyStorePassPhrase}")	
	  private String keyStorePassPhraseAlias;
	  
	  @Value("${timplus.keystore.privateKeyPassPhraseAlias:privateKeyPassPhrase}")	
	  private String privateKeyPassPhraseAlias;

	  @Value("${timplus.keystore.initOnStart:true}")	
	  private String initOnStart;	  
	  
	  @Value("${timplus.keystore.keyStorePassPhrase:T1mPl^sS+}")	
	  private String keyStorePassPhrase;	  
	  
	  @Value("${timplus.keystore.privateKeyPassPhrase:T1mPl^sS+}")	
	  private String privateKeyPassPhrase;	
	  
	  @Bean	  
	  @ConditionalOnMissingBean
	  @ConditionalOnProperty(name="timplus.keystore.hsmpresent", havingValue="true")
	  public KeyStoreProtectionManager hsmKeyStoreProtectionManager()
	  {
		  LOGGER.info("HSM configured.  Attempting to connect to device.");
		  
		  try
		  {
			  final BootstrappedPKCS11Credential cred = new BootstrappedPKCS11Credential(keyStorePin);
			  final StaticCachedPKCS11TokenKeyStoreProtectionManager mgr = new StaticCachedPKCS11TokenKeyStoreProtectionManager();
			  mgr.setCredential(cred);
			  mgr.setKeyStoreType(keyStoreType);
			  mgr.setKeyStoreSourceAsString(keyStoreSourceAsString);
			  mgr.setKeyStoreProviderName(keyStoreProviderName);
			  mgr.setKeyStorePassPhraseAlias(keyStorePassPhraseAlias);
			  mgr.setPrivateKeyPassPhraseAlias(privateKeyPassPhraseAlias);
			  
			  if (Boolean.parseBoolean(initOnStart))
				  mgr.initTokenStore();
			  
			  return mgr;
		  }
		  catch (Exception e)
		  {
			   throw new RuntimeException(e);
		  }
	  }
	  
	  @Bean	  
	  @ConditionalOnMissingBean
	  @ConditionalOnProperty(name="timplus.keystore.hsmpresent", havingValue="false", matchIfMissing=true)
	  public KeyStoreProtectionManager nonHSMKeyStoreProtectionManager() throws CryptoException 
	  {
		  LOGGER.info("No HSM configured.");
		  
		  final BootstrappedKeyStoreProtectionManager mgr = new BootstrappedKeyStoreProtectionManager(keyStorePassPhrase, privateKeyPassPhrase);
		  
		  return mgr;
	  }
	  
}
