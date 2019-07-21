package me.sudsey.chip8.interpret;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

// Most of the graphics code in this class is pretty poor. I'm still trying to work my head around how OpenGL works.
// Using synchronised() { } is also really slow I think

public class Terminal {

    private long window;

    private final boolean[][] pixels;
    private float[][] display;
    private FloatBuffer textureBuffer;

    public Terminal() {
        this.pixels = new boolean[64][32];
        this.display = new float[64][32];
        this.textureBuffer = BufferUtils.createFloatBuffer(64 * 32 * 4);
    }


    public void init() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(640, 320, "CHIP8", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowSizeCallback(window, (window, width, height) ->
            glViewport(0, 0, width, height)
        );

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, glGenTextures());
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        buildBufferFromDisplay();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 32, 0, GL_RGBA, GL_FLOAT, textureBuffer);
    }

    public void start() {
        glfwShowWindow(window);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            synchronized (pixels) {
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < 32; j++) {
                        if (pixels[i][j]) {
                            display[i][j] = 1.0f;
                        } else if (display[i][j] > 0.1f) {
                            display[i][j] -= 0.5f;
                        }
                    }
                }
            }

            buildBufferFromDisplay();
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 64, 32, GL_RGBA, GL_FLOAT, textureBuffer);

            glBegin(GL_TRIANGLE_STRIP);
            glTexCoord2f(0, 0);
            glVertex2f(-1, 1);
            glTexCoord2f(1, 0);
            glVertex2f(1, 1);
            glTexCoord2f(0, 1);
            glVertex2f(-1, -1);
            glTexCoord2f(1, 1);
            glVertex2f(1, -1);
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }


    public void setKeyCallbacks(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(window, callback);
    }

    public void setDisplay(boolean[][] display) {
        synchronized (pixels) {
            for (int i = 0; i < 64; i++) {
                System.arraycopy(display[i], 0, pixels[i], 0, 32);
            }
        }
    }


    private void buildBufferFromDisplay() {
        textureBuffer.clear();

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                textureBuffer.put(0.561f + -0.439f * sqrtLog2(display[x][y] + 1));
                textureBuffer.put(0.62f + -0.479f * sqrtLog2(display[x][y] + 1));
                textureBuffer.put(0.4f + -0.361f * sqrtLog2(display[x][y] + 1));
                textureBuffer.put(0.0f);
            }
        }

        textureBuffer.flip();
    }

    private float sqrtLog2(float x) {
        return (float) Math.sqrt(Math.log(x) / Math.log(2));
    }

}
