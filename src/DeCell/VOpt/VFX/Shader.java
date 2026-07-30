package DeCell.VOpt.VFX;

import DeCell.VOpt.VOpt;
import com.fs.starfarer.api.Global;
import org.lwjgl.opengl.*;

import java.io.IOException;

public class Shader {

    private int handle = 0;
    private boolean initialized = false;
    private boolean disposed = false;

    private String vertexSource;
    private String fragmentSource;
    private String name;

    private ShaderUniformManager uniformManager;

    public Shader(String vertexSource, String fragmentSource) {
        this(vertexSource, fragmentSource, "");
    }

    public Shader(String vertexSource, String fragmentSource, String name) {
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
        this.name = name != null ? name : "";
    }

    public static Shader fromFile(String vertexSource, String fragmentSource) {
        return fromFile(vertexSource, fragmentSource, "");
    }

    public static Shader fromFile(String vertexSource, String fragmentSource, String name) {
        Shader shader = null;
        try {
            String vert = Global.getSettings().loadText(vertexSource);
            String frag = Global.getSettings().loadText(fragmentSource);
            shader = new Shader(vert, frag, name).init();
        } catch (IOException e) {
        }
        return shader;
    }

    public Shader init() {
        if (initialized || disposed) return this;

        // Create program
        handle = GL20.glCreateProgram();
        this.uniformManager = new ShaderUniformManager(this.handle); // We'll create this class too

        // Compile shaders
        int vertexShader = compileShader(vertexSource, ShaderType.VertexShader);
        int fragmentShader = compileShader(fragmentSource, ShaderType.FragmentShader);

        // Attach and link
        GL20.glAttachShader(handle, vertexShader);
        GL20.glAttachShader(handle, fragmentShader);

        GL20.glLinkProgram(handle);
        checkLinkStatus();

        // Cleanup individual shaders
        GL20.glDetachShader(handle, vertexShader);
        GL20.glDetachShader(handle, fragmentShader);
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        initialized = true;
        return this;
    }

    private int compileShader(String source, int type) {
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, source);
        GL20.glCompileShader(shaderID);

        checkCompileStatus(shaderID);

        return shaderID;
    }

    private void checkCompileStatus(int shaderID) {
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shaderID, 4096);
            VOpt.LogErr("shader compilation failed for '" + name + "':\n" + log);
            // You can throw here if you prefer strict behavior
        }
    }

    private void checkLinkStatus() {
        if (GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(handle, 4096);
            VOpt.LogErr("Shader program linking failed for '" + name + "':\n" + log);
        }
    }


    public void bind() {
        GL20.glUseProgram(handle);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void dispose() {
        if (disposed || handle == 0) return;
        unbind();
        GL20.glDeleteProgram(handle);
        handle = 0;
        disposed = true;
    }
    // no finalize/deconstruct because java fucking sucks


    // the rest of the code from here is fuckass getters and setters because java is a bitch of a language that doesnt
    // have jackshit

    public int getHandle() {
        return handle;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public String getVertexSource() {
        return vertexSource;
    }

    public String getFragmentSource() {
        return fragmentSource;
    }

    public String getName() {
        return name;
    }

    public ShaderUniformManager getUniformManager() {
        return uniformManager;
    }

    @Override
    public String toString() {
        return "Shader{" +
                "handle=" + handle +
                ", initialized=" + initialized +
                ", disposed=" + disposed +
                ", vertexSource='" + vertexSource + '\'' +
                ", fragmentSource='" + fragmentSource + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}