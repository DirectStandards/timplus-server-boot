CREATE TABLE ofCertificate (
  id                   VARCHAR(64)     NOT NULL,
  distinguishedName    VARCHAR(255)    NOT NULL,
  serialNumber         VARCHAR(60)     NOT NULL,  
  thumbprint           VARCHAR(60)     NOT NULL,    
  thumbprintAllCaps    VARCHAR(60)     NOT NULL,      
  validStartDate       BIGINT          NOT NULL,
  validEndDate         BIGINT          NOT NULL,  
  certData             BLOB            NOT NULL,  
  domain               VARCHAR(255)    NOT NULL,
  domainAllCaps        VARCHAR(255)    NOT NULL,   
  certStatus           INTEGER         NOT NULL,      
  PRIMARY KEY (id),
  INDEX ofofCertificate_domainAllCaps_idx (domainAllCaps),
  INDEX ofofCertificate_thumbprintAllCaps_idx (thumbprintAllCaps)
);

CREATE TABLE ofTrustAnchor (
  id                   VARCHAR(64)     NOT NULL,
  distinguishedName    VARCHAR(255)    NOT NULL,
  serialNumber         VARCHAR(60)     NOT NULL,  
  thumbprint           VARCHAR(60)     NOT NULL,    
  validStartDate       BIGINT          NOT NULL,
  validEndDate         BIGINT          NOT NULL,  
  anchorData           BLOB            NOT NULL,  
  PRIMARY KEY (id)
);

CREATE TABLE ofTrustBundle (
  id                     VARCHAR(64)     NOT NULL,
  bundleName             VARCHAR(255)    NOT NULL,
  bundleURL              VARCHAR(255)    NOT NULL,  
  checkSum               VARCHAR(60),  
  lastRefreshAttempt     BIGINT,
  lastSuccessfulRefresh  BIGINT,  
  lastRefreshError       VARCHAR(30),
  refreshInterval        INTEGER         NOT NULL,    
  signingCertificateData BLOB,
  createTime             BIGINT          NOT NULL, 
  PRIMARY KEY (id)
);

CREATE TABLE ofTrustBundleAnchor (
  id                   VARCHAR(64)     NOT NULL,
  distinguishedName    VARCHAR(255)    NOT NULL,
  serialNumber         VARCHAR(60)     NOT NULL,  
  thumbprint           VARCHAR(60)     NOT NULL,    
  validStartDate       BIGINT          NOT NULL,
  validEndDate         BIGINT          NOT NULL,  
  anchorData           BLOB            NOT NULL,  
  trustBundleId        VARCHAR(64)     NOT NULL,  
  PRIMARY KEY (id),
  INDEX ofTrustBundleAnchor_trustBundleId_idx (trustBundleId)
);

CREATE TABLE ofTrustCircle (
  id                     VARCHAR(64)     NOT NULL,
  circleName             VARCHAR(255)    NOT NULL,
  createTime             BIGINT          NOT NULL, 
  PRIMARY KEY (id)
);

CREATE TABLE ofTrustCircleAnchorReltn (
  trustCircleId          VARCHAR(64)     NOT NULL,
  trustAnchorId          VARCHAR(64)     NOT NULL,
  PRIMARY KEY (trustCircleId, trustAnchorId)
);

CREATE TABLE ofTrustCircleBundleReltn (
  trustCircleId          VARCHAR(64)     NOT NULL,
  trustBundleId          VARCHAR(64)     NOT NULL,
  PRIMARY KEY (trustCircleId, trustBundleId)
);

CREATE TABLE ofTrustCircleDomainReltn (
  trustCircleId          VARCHAR(64)     NOT NULL,
  domainName             VARCHAR(64)     NOT NULL,
  PRIMARY KEY (trustCircleId, domainName)
);