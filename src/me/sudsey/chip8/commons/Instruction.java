package me.sudsey.chip8.commons;

public class Instruction {

    private Opcode opcode;
    private int nnn;
    private int n;
    private int x;
    private int y;
    private int kk;

    public Instruction(Opcode opcode, int nnn, int n, int x, int y, int kk) {
        this.opcode = opcode;
        this.nnn = nnn;
        this.n = n;
        this.x = x;
        this.y = y;
        this.kk = kk;
    }


    public static Instruction parseInstructionBytes(int instruction) {
        Opcode opcode = null;
        for (Opcode _opcode : Opcode.values()) {
            if ((instruction & _opcode.getOperationMask()) == _opcode.getOperationBits()) {
                opcode = _opcode;
                break;
            }
        }

        if (opcode == null) {
            return null;
        }

        int nnn = instruction & 0x0FFF;
        int n = instruction & 0x000F;
        int x = (instruction >>> 8) & 0x000F;
        int y = (instruction >>> 4) & 0x000F;
        int kk = instruction & 0x00FF;

        return new Instruction(opcode, nnn, n, x, y, kk);
    }


    public Opcode getOpcode() {
        return opcode;
    }

    public int getNnn() {
        return nnn;
    }

    public int getN() {
        return n;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getKk() {
        return kk;
    }

}
