package eu.oberon.oss.tsc.cm.cli.tools;

import eu.oberonoss.tsc.cm.gui.TSCApplication;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TSCApplicationCLITest {

    @Test
    void testLoggerTopic() throws NoSuchFieldException, IllegalAccessException {
        Field loggerField = TSCApplicationCLI.class.getDeclaredField("LOGGER");
        loggerField.setAccessible(true);
        org.slf4j.Logger logger = (org.slf4j.Logger) loggerField.get(null);
        assertEquals("tsc-application", logger.getName());
    }

    @Test
    void testCommandAnnotation() {
        CommandLine.Command annotation = TSCApplicationCLI.class.getAnnotation(CommandLine.Command.class);
        assertNotNull(annotation);
        assertEquals("tsc-application", annotation.name());
        assertArrayEquals(new String[]{}, annotation.aliases());
        assertTrue(annotation.mixinStandardHelpOptions());
    }

    @Test
    void testHelpOption() {
        TSCApplicationCLI cli = new TSCApplicationCLI();
        CommandLine cmd = new CommandLine(cli);
        int exitCode = cmd.execute("--help");
        assertEquals(0, exitCode);
    }

    @Test
    void testBuildCommandWithoutOptions() {
        TSCApplicationCLI cli = new TSCApplicationCLI();
        List<String> command = cli.buildCommand();

        assertNotNull(command);
        assertFalse(command.isEmpty());
        assertEquals(cli.getJavaExecutable(), command.getFirst());
        assertTrue(command.contains("-cp"));
        assertEquals(TSCApplication.class.getName(), command.getLast());
    }

    @Test
    void testBuildCommandWithAppHomeAndJvmArgs() {
        String originalAppHome = System.getProperty("app.home");
        try {
            System.setProperty("app.home", "/custom/app/home");
            TSCApplicationCLI cli = new TSCApplicationCLI();
            cli.jvmArgs = List.of("-Xmx512m", "-Dcustom.prop=test");

            List<String> command = cli.buildCommand();
            assertTrue(command.contains("-Dapp.home=/custom/app/home"));
            assertTrue(command.contains("-Xmx512m"));
            assertTrue(command.contains("-Dcustom.prop=test"));
            assertTrue(command.contains("-cp"));
            assertEquals(TSCApplication.class.getName(), command.getLast());
        } finally {
            if (originalAppHome != null) {
                System.setProperty("app.home", originalAppHome);
            } else {
                System.clearProperty("app.home");
            }
        }
    }

    @Test
    void testCallSuccessWithoutWait() {
        Process dummyProcess = new DummyProcess(0);
        TSCApplicationCLI cli = new TSCApplicationCLI() {
            @Override
            Process launchApplication() {
                return dummyProcess;
            }
        };

        CommandLine cmd = new CommandLine(cli);
        int exitCode = cmd.execute();
        assertEquals(0, exitCode);
    }

    @Test
    void testCallSuccessWithWait() {
        Process dummyProcess = new DummyProcess(0);
        TSCApplicationCLI cli = new TSCApplicationCLI() {
            @Override
            Process launchApplication() {
                return dummyProcess;
            }
        };

        CommandLine cmd = new CommandLine(cli);
        int exitCode = cmd.execute("--wait");
        assertEquals(0, exitCode);
    }

    @Test
    void testCallFailureOnLaunchException() {
        TSCApplicationCLI cli = new TSCApplicationCLI() {
            @Override
            Process launchApplication() throws IOException {
                throw new IOException("Cannot spawn process");
            }
        };

        CommandLine cmd = new CommandLine(cli);
        int exitCode = cmd.execute();
        assertEquals(1, exitCode);
    }

    @Test
    void testCallInterrupted() {
        Process dummyProcess = new Process() {
            @Override
            public java.io.OutputStream getOutputStream() {
                return null;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return null;
            }

            @Override
            public java.io.InputStream getErrorStream() {
                return null;
            }

            @Override
            public int waitFor() throws InterruptedException {
                throw new InterruptedException("Interrupted");
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {
                // Left intentionally blank
            }

            @Override
            public long pid() {
                return 12345L;
            }
        };

        TSCApplicationCLI cli = new TSCApplicationCLI() {
            @Override
            Process launchApplication() {
                return dummyProcess;
            }
        };

        CommandLine cmd = new CommandLine(cli);
        int exitCode = cmd.execute("--wait");
        assertEquals(1, exitCode);
    }

    @Test
    void testCreateProcessBuilder() {
        TSCApplicationCLI cli = new TSCApplicationCLI();
        ProcessBuilder processBuilder = cli.createProcessBuilder();
        assertNotNull(processBuilder);
        assertEquals(cli.buildCommand(), processBuilder.command());
    }

    private static class DummyProcess extends Process {
        private final int exitCode;

        DummyProcess(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public java.io.OutputStream getOutputStream() {
            return null;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return null;
        }

        @Override
        public java.io.InputStream getErrorStream() {
            return null;
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public int exitValue() {
            return exitCode;
        }

        @Override
        public void destroy() {
            // Left intentionally blank
        }

        @Override
        public long pid() {
            return 9999L;
        }
    }
}
