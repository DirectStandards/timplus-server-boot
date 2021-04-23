CREATE TABLE ofClusterMember (
  nodeId               VARCHAR(64)     NOT NULL,
  rawNodeId            BLOB            NOT NULL,
  nodeHost             VARCHAR(128)    NOT NULL,  
  nodeIP               VARCHAR(128)    NOT NULL,    
  nodeJoinedDtTm       BIGINT          NOT NULL,
  nodeLeftDtTm         BIGINT,
  lastNodeHBDtTm       BIGINT          NOT NULL,  
  nodeStatus           INTEGER         NOT NULL,  
  CONSTRAINT ofClusterMember PRIMARY KEY (nodeId)
);

CREATE INDEX ofClusterMember_nodeStatus_idx ON ofClusterMember (nodeStatus);