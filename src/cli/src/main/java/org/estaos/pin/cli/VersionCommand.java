package org.estaos.pin.cli;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@CommandLine.Command(
        name = "version",
        description = "Print pinlang version"
)
public class VersionCommand implements Runnable, CommandLine.IVersionProvider {
    @Override
    public void run() {
        for(String version : getVersion()) {
            System.out.println(version);
        }
    }

    public String[] getVersion() {
        try (InputStream in = VersionCommand.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return new String[] {
                    props.getProperty("version")
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
