package org.example;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

final class MockJdbcSupport {

    static final String DEFAULT_DB_URL =
            "jdbc:mysql://localhost:3306/shopping_cart_localization"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                    + "&characterEncoding=UTF-8";

    private static final MockDriver DRIVER = new MockDriver();
    private static boolean registered;

    private MockJdbcSupport() {
    }

    static synchronized void registerDriver() throws SQLException {
        if (!registered) {
            DriverManager.registerDriver(DRIVER);
            registered = true;
        }
    }

    static void setScenario(Scenario scenario) {
        DRIVER.setScenario(scenario);
    }

    static final class Scenario {
        final Map<String, StatementBehavior> statementBehaviors = new HashMap<>();
        final List<String> executedSql = new ArrayList<>();
        final List<String> events = new ArrayList<>();
        String capturedUrl;
        String capturedUser;
        String capturedPassword;

        void whenSql(String sql, StatementBehavior behavior) {
            statementBehaviors.put(sql, behavior);
        }
    }

    static final class StatementBehavior {
        int executeUpdateResult = 1;
        SQLException executeUpdateException;
        SQLException executeQueryException;
        Deque<Map<Integer, Object>> generatedRows = new ArrayDeque<>();
        Deque<Map<Integer, Object>> queryRows = new ArrayDeque<>();

        static StatementBehavior forUpdateResult(int result) {
            StatementBehavior b = new StatementBehavior();
            b.executeUpdateResult = result;
            return b;
        }

        static StatementBehavior throwing(SQLException ex) {
            StatementBehavior b = new StatementBehavior();
            b.executeUpdateException = ex;
            return b;
        }

        static StatementBehavior throwingOnQuery(SQLException ex) {
            StatementBehavior b = new StatementBehavior();
            b.executeQueryException = ex;
            return b;
        }
    }

    private static final class MockDriver implements Driver {
        private Scenario scenario = new Scenario();

        void setScenario(Scenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public Connection connect(String url, Properties info) {
            if (!acceptsURL(url)) {
                return null;
            }
            scenario.capturedUrl = url;
            scenario.capturedUser = info == null ? null : info.getProperty("user");
            scenario.capturedPassword = info == null ? null : info.getProperty("password");
            return connectionProxy(scenario);
        }

        @Override
        public boolean acceptsURL(String url) {
            return url != null && url.startsWith("jdbc:mysql://localhost:3306/shopping_cart_localization");
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("No parent logger");
        }
    }

    private static Connection connectionProxy(Scenario scenario) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "setAutoCommit" -> {
                    scenario.events.add("setAutoCommit:" + args[0]);
                    return null;
                }
                case "prepareStatement" -> {
                    String sql = (String) args[0];
                    scenario.executedSql.add(sql);
                    StatementBehavior behavior =
                            scenario.statementBehaviors.getOrDefault(sql, StatementBehavior.forUpdateResult(1));
                    return preparedStatementProxy(behavior);
                }
                case "commit" -> {
                    scenario.events.add("commit");
                    return null;
                }
                case "rollback" -> {
                    scenario.events.add("rollback");
                    return null;
                }
                case "close" -> {
                    scenario.events.add("close");
                    return null;
                }
                case "isClosed" -> {
                    return false;
                }
                case "unwrap" -> {
                    return null;
                }
                case "isWrapperFor" -> {
                    return false;
                }
                default -> {
                    return defaultValue(method.getReturnType());
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                MockJdbcSupport.class.getClassLoader(),
                new Class[]{Connection.class},
                handler
        );
    }

    private static PreparedStatement preparedStatementProxy(StatementBehavior behavior) {
        Map<Integer, Object> params = new HashMap<>();
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "setInt", "setDouble", "setString" -> {
                    params.put((Integer) args[0], args[1]);
                    return null;
                }
                case "executeUpdate" -> {
                    if (behavior.executeUpdateException != null) {
                        throw behavior.executeUpdateException;
                    }
                    return behavior.executeUpdateResult;
                }
                case "getGeneratedKeys" -> {
                    return resultSetProxy(new ArrayDeque<>(behavior.generatedRows));
                }
                case "executeQuery" -> {
                    if (behavior.executeQueryException != null) {
                        throw behavior.executeQueryException;
                    }
                    return resultSetProxy(new ArrayDeque<>(behavior.queryRows));
                }
                case "close" -> {
                    return null;
                }
                case "unwrap" -> {
                    return null;
                }
                case "isWrapperFor" -> {
                    return false;
                }
                default -> {
                    return defaultValue(method.getReturnType());
                }
            }
        };
        return (PreparedStatement) Proxy.newProxyInstance(
                MockJdbcSupport.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                handler
        );
    }

    private static ResultSet resultSetProxy(Deque<Map<Integer, Object>> rows) {
        AtomicReference<Map<Integer, Object>> current = new AtomicReference<>();
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "next" -> {
                    if (rows.isEmpty()) {
                        current.set(null);
                        return false;
                    }
                    current.set(rows.removeFirst());
                    return true;
                }
                case "getInt" -> {
                    Number value = (Number) current.get().get((Integer) args[0]);
                    return value.intValue();
                }
                case "getString" -> {
                    Object value = current.get().get((Integer) args[0]);
                    return value == null ? null : value.toString();
                }
                case "close" -> {
                    return null;
                }
                case "unwrap" -> {
                    return null;
                }
                case "isWrapperFor" -> {
                    return false;
                }
                default -> {
                    return defaultValue(method.getReturnType());
                }
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                MockJdbcSupport.class.getClassLoader(),
                new Class[]{ResultSet.class},
                handler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == null || !returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
