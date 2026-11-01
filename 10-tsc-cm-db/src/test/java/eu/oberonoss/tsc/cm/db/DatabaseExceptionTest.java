package eu.oberonoss.tsc.cm.db;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseExceptionTest {

    @Test
    void testConstructors() {
        DatabaseException e1 = new DatabaseException("test message");
        assertThat(e1.getMessage()).isEqualTo("test message");
        assertThat(e1.getCause()).isNull();

        Throwable cause = new RuntimeException("root cause");
        DatabaseException e2 = new DatabaseException("failed", cause);
        assertThat(e2.getMessage()).isEqualTo("failed");
        assertThat(e2.getCause()).isEqualTo(cause);

        DatabaseException e3 = new DatabaseException(cause);
        assertThat(e3.getCause()).isEqualTo(cause);
    }
}
