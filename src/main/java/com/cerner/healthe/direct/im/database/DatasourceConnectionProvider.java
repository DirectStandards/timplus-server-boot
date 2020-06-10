package com.cerner.healthe.direct.im.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jivesoftware.database.ConnectionProvider;
import org.springframework.context.ApplicationContext;

import com.cerner.healthe.direct.im.springconfig.XMPPServerConfig;

public class DatasourceConnectionProvider implements ConnectionProvider
{
	protected DataSource dataSource;
	
	public DatasourceConnectionProvider()
	{
		final ApplicationContext ctx = XMPPServerConfig.getAppContext();
		
		if (ctx == null)
			throw new IllegalStateException("Application context cannot be null");
		
		dataSource = ctx.getBean(DataSource.class);
		
		if (dataSource == null)
			throw new IllegalStateException("Datasource does not exist in application context");
	}
	
	@Override
	public boolean isPooled()
	{
		return true;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	@Override
	public void start()
	{
		
	}

	@Override
	public void restart()
	{
		
	}

	@Override
	public void destroy()
	{
		
	}

}
