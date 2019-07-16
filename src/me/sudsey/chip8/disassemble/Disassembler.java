package me.sudsey.chip8.disassemble;

import me.sudsey.chip8.commons.Instruction;
import me.sudsey.chip8.commons.Opcode;

public class Disassembler {

    public static void disassemble(int[] rom) {
        int length = (rom.length / 2) * 2;

        for (int i = 0; i < length; i += 2) {
            int instructionBytes = (rom[i] << 8) | rom[1 + i];
            System.out.println(getDisassembledLine(i, instructionBytes));
        }
    }


    private static String getDisassembledLine(int location, int instructionBytes) {
        Instruction instruction = Instruction.parseInstructionBytes(instructionBytes);

        return String.format("0x%04x\t", location + 0x200) +
                String.format("%04X\t", instructionBytes) +
                (instruction != null ? disassembleInstruction(instruction) : "DATA");
    }

    private static String disassembleInstruction(Instruction instruction) {
        StringBuilder builder = new StringBuilder();
        Opcode opcode = instruction.getOpcode();

        builder.append(String.format("%-11s\t(%s)\t", opcode.name(), opcode.getOpcodeFormat()));

        if (opcode.usesNnn()) {
            builder.append(String.format("nnn: 0x%03x\t", instruction.getNnn()));
        } else {
            builder.append("\t\t\t");
        }
        if (opcode.usesN()) {
            builder.append(String.format("n: 0x%x\t", instruction.getN()));
        } else {
            builder.append("\t\t");
        }
        if (opcode.usesX()) {
            builder.append(String.format("x: 0x%x\t", instruction.getX()));
        } else {
            builder.append("\t\t");
        }
        if (opcode.usesY()) {
            builder.append(String.format("y: 0x%x\t", instruction.getY()));
        } else {
            builder.append("\t\t");
        }
        if (opcode.usesKk()) {
            builder.append(String.format("kk: 0x%02x", instruction.getKk()));
        } else {
            builder.append("\t\t");
        }

        return builder.toString();
    }

}
