CREATE TABLE ofClusterMember (
  nodeId               VARCHAR(64)     NOT NULL,
  rawNodeId            BLOB            NOT NULL,
  nodeHost             VARCHAR(128)    NOT NULL,  
  nodeIP               VARCHAR(128)    NOT NULL,    
  nodeJoinedDtTm       BIGINT          NOT NULL,
  nodeLeftDtTm         BIGINT,
  lastNodeHBDtTm       BIGINT          NOT NULL,  
  nodeStatus           INTEGER         NOT NULL,  
  PRIMARY KEY (nodeId),
  INDEX ofClusterMember_nodeStatus_idx (nodeStatus)
);
