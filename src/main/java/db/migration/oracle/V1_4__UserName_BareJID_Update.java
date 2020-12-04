package db.migration.oracle;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class V1_4__UserName_BareJID_Update extends BaseJavaMigration
{
    private static Logger log = LoggerFactory.getLogger(V1_4__UserName_BareJID_Update.class);
	
    @Override
    public void migrate(Context context) throws Exception 
    {
    	log.info("Beginning update of usernames to bare JIDs");

    	
        final JdbcTemplate jdbcTemplate = 
        	      new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));

        
        final List<Pair<String,String>> userBairJIDPairs = jdbcTemplate.query("select username, domain from ofUser", (rs, rowNum) ->
          {
        	  final String userName = rs.getString("username");
        	  final String domain = rs.getString("domain");
        	  
        	  return (userName.indexOf("@") > -1) ? Pair.of(userName, userName) : Pair.of(userName, userName + "@" + domain);
      	  
          });
        
        log.info("Found {} user(s) that may need to be updated to bare JID format", userBairJIDPairs.size());
        
        userBairJIDPairs.forEach(pair -> 
        {
        	jdbcTemplate.update("update ofUser set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofUserProp set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofUserFlag set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofOffline set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofPresence set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofRoster set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofVCard set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofGroupUser set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofPrivacyList set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofSASLAuthorized set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofSecurityAuditLog set username = ? where username = ?", pair.getRight(), pair.getLeft());
        	jdbcTemplate.update("update ofSecurityAuditLog set username = ? where username = ?", pair.getRight(), pair.getLeft());
        });
        
        log.info("Completed updating username to bare JIDs");
    }
    
    
}
