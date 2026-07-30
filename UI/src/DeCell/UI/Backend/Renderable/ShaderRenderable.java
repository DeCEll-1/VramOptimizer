package DeCell.UI.Backend.Renderable;

import DeCell.VFXCommons.Shader;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class ShaderRenderable extends PluginRenderable {
    public Shader shader;
    private Consumer<Shader> uniformUpdater = null;

    public ShaderRenderable(Shader _shader) {
        this.shader = _shader;
    }

    @Override
    public void renderBelow(float alpha) {
        if (zone == null)
            return;
        if (shader == null)
            return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        if (uniformUpdater != null)
            uniformUpdater.accept(shader);
        zone.render();
        shader.unbind();

        GL11.glDisable(GL11.GL_BLEND);
    }

    public ShaderRenderable setShader(Shader _shader) {
        if (this.shader != null && !this.shader.isDisposed())
            this.shader.dispose();
        this.shader = _shader;
        return this;
    }

    public ShaderRenderable setBeforeRender(Consumer<Shader> updater) {
        this.uniformUpdater = updater;
        return this;
    }

    public Shader getShader() {
        return shader;
    }

}