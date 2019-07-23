package me.sudsey.chip8;

import me.sudsey.chip8.commons.Options;
import me.sudsey.chip8.disassemble.Disassembler;
import me.sudsey.chip8.interpret.Interpreter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar " + getExecutableName() + " (run|disassemble) <rom-path> [options]");
            return;
        }

        int[] rom;
        try {
            rom = getROM(Paths.get(args[1]));
        } catch (IOException e) {
            System.err.println("Could not read ROM file. Error: " + e.toString());
            return;
        }

        Options options = Options.parseOptions(args);
        switch (args[0]) {
            case "run":         Interpreter.run(options, rom);  break;
            case "disassemble": Disassembler.disassemble(rom);
        }
    }

    private static String getExecutableName() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
        } catch (URISyntaxException e) {
            return "CHIP8.jar";
        }
    }

    private static int[] getROM(Path path) throws IOException {
        byte[] romBytes = Files.readAllBytes(path);

        int[] rom = new int[romBytes.length];
        for (int i = 0; i < romBytes.length; i++) {
            rom[i] = romBytes[i] & 0xFF;
        }

        return rom;
    }

}
