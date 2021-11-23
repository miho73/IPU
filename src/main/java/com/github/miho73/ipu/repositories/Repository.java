package com.github.miho73.ipu.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class Repository {
    protected DriverManagerDataSource dataSource;

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public Connection openConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public Connection openConnectionForEdit() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }
    public void commit(Connection connection) throws SQLException {
        connection.commit();
    }
    public void commitAndClose(Connection connection) throws SQLException {
        connection.commit();
        connection.close();
    }
    public void rollback(Connection connection) throws SQLException {
        connection.rollback();
    }
    public void rollbackAndClose(Connection connection) throws SQLException {
        connection.rollback();
        connection.close();
    }
    public void close(Connection connection) throws SQLException {
        connection.close();
    }

    @PostConstruct
    public abstract void initRepository() throws SQLException;
}
