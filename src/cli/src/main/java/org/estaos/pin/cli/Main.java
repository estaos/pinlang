package org.estaos.pin.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "pin",
        description = "pinlang cli",
        mixinStandardHelpOptions = true,
        versionProvider = VersionCommand.class,
        subcommands = {
                EmitCommand.class,
                VersionCommand.class
        }
)
public class Main implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine cmd = new CommandLine(spec);
        cmd.usage(System.out);
    }
}
