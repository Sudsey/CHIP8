package me.sudsey.chip8.commons;

public enum Opcode {

    CLS         (0x00E0, false, false, false, false, false),    // 00E0
    RET         (0x00EE, false, false, false, false, false),    // 00EE
    JP          (0x1000, true,  false, false, false, false),    // 1nnn
    CALL        (0x2000, true,  false, false, false, false),    // 2nnn
    SE_Vx_byte  (0x3000, false, false, true,  false, true ),    // 3xkk
    SNE_Vx_byte (0x4000, false, false, true,  false, true ),    // 4xkk
    SE_Vx_Vy    (0x5000, false, false, true,  true,  false),    // 5xy0
    LD_Vx_byte  (0x6000, false, false, true,  false, true ),    // 6xkk
    ADD_Vx_byte (0x7000, false, false, true,  false, true ),    // 7xkk
    LD_Vx_Vy    (0x8000, false, false, true,  true,  false),    // 8xy0
    OR          (0x8001, false, false, true,  true,  false),    // 8xy1
    AND         (0x8002, false, false, true,  true,  false),    // 8xy2
    XOR         (0x8003, false, false, true,  true,  false),    // 8xy3
    ADD_Vx_Vy   (0x8004, false, false, true,  true,  false),    // 8xy4
    SUB         (0x8005, false, false, true,  true,  false),    // 8xy5
    SHR         (0x8006, false, false, true,  true,  false),    // 8xy6
    SUBN        (0x8007, false, false, true,  true,  false),    // 8xy7
    SHL         (0x800E, false, false, true,  true,  false),    // 8xyE
    SNE_Vx_Vy   (0x9000, false, false, true,  true,  false),    // 9xy0
    LD_I_addr   (0xA000, true,  false, false, false, false),    // Annn
    JP_V0_addr  (0xB000, true,  false, false, false, false),    // Bnnn
    RND         (0xC000, false, false, true,  false, true ),    // Cxkk
    DRW         (0xD000, false, true,  true,  true,  false),    // Dxyn
    SKP         (0xE09E, false, false, true,  false, false),    // Ex9E
    SKNP        (0xE0A1, false, false, true,  false, false),    // ExA1
    LD_Vx_DT    (0xF007, false, false, true,  false, false),    // Fx07
    LD_Vx_K     (0xF00A, false, false, true,  false, false),    // Fx0A
    LD_DT_Vx    (0xF015, false, false, true,  false, false),    // Fx15
    LD_ST_Vx    (0xF018, false, false, true,  false, false),    // Fx18
    ADD_I_Vx    (0xF01E, false, false, true,  false, false),    // Fx1E
    LD_F_Vx     (0xF029, false, false, true,  false, false),    // Fx29
    LD_B_Vx     (0xF033, false, false, true,  false, false),    // Fx33
    LD_I_Vx     (0xF055, false, false, true,  false, false),    // Fx55
    LD_Vx_I     (0xF065, false, false, true,  false, false);    // Fx65


    private int operationBits;

    private boolean usesNnn;
    private boolean usesN;
    private boolean usesX;
    private boolean usesY;
    private boolean usesKk;

    Opcode(int operationBits, boolean usesNnn, boolean usesN, boolean usesX, boolean usesY, boolean usesKk) {
        this.operationBits = operationBits;
        this.usesNnn = usesNnn;
        this.usesN = usesN;
        this.usesX = usesX;
        this.usesY = usesY;
        this.usesKk = usesKk;
    }


    public int getOperationBits() {
        return operationBits;
    }

    public int getOperationMask() {
        return 0xFFFF
                & (usesNnn ? 0xF000 : 0xFFFF)
                & (usesN   ? 0xFFF0 : 0xFFFF)
                & (usesX   ? 0xF0FF : 0xFFFF)
                & (usesY   ? 0xFF0F : 0xFFFF)
                & (usesKk  ? 0xFF00 : 0xFFFF);
    }


    public boolean usesNnn() {
        return usesNnn;
    }

    public boolean usesN() {
        return usesN;
    }

    public boolean usesX() {
        return usesX;
    }

    public boolean usesY() {
        return usesY;
    }

    public boolean usesKk() {
        return usesKk;
    }


    public String getOpcodeFormat() {
        StringBuilder builder = new StringBuilder(String.format("%04X", getOperationBits()));

        if (usesNnn) {
            builder.replace(1, 4, "nnn");
        }
        if (usesN) {
            builder.replace(3, 4, "n");
        }
        if (usesX) {
            builder.replace(1, 2, "x");
        }
        if (usesY) {
            builder.replace(2, 3, "y");
        }
        if (usesKk) {
            builder.replace(2, 4, "kk");
        }

        return builder.toString();
    }

}
