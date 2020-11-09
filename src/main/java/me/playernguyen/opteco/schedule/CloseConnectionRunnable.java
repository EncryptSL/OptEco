package me.playernguyen.opteco.schedule;

import java.sql.Connection;
import java.sql.SQLException;

public class CloseConnectionRunnable extends OptEcoRunnable {

    private final Connection connection;

    public CloseConnectionRunnable(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * This to close the connection after timeout
     */
    @Override
    public void run() {
        try {
            if (getConnection() != null && !getConnection().isClosed()) {
                this.getInstance().getDebugger().notice(String.format("Closing connection with task #%s...",
                        getTaskId()));
                getConnection().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cancel();
    }
}
