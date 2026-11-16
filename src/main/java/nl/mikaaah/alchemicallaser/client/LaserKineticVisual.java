package nl.mikaaah.alchemicallaser.client;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.*;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.function.Consumer;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;
import org.joml.Matrix4f;

/** Flywheel1.0+ instances the X-axis shaft. Animated housing/beam remain in the BER. */
public final class LaserKineticVisual extends KineticBlockEntityVisual<LaserBlockEntity>
    implements SimpleDynamicVisual {
  private final TransformedInstance shaft;

  public LaserKineticVisual(VisualizationContext context, LaserBlockEntity be, float partialTick) {
    super(context, be, partialTick);
    shaft =
        instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(LaserPartials.SIDE_SHAFT))
            .createInstance();
    animate(partialTick);
    updateLight(partialTick);
  }

  private void animate(float partial) {
    Matrix4f matrix =
        new Matrix4f()
            .translate(visualPos.getX() + .5f, visualPos.getY() + .5f, visualPos.getZ() + .5f)
            .rotateY(
                blockEntity
                            .getBlockState()
                            .getValue(nl.mikaaah.alchemicallaser.LaserBlock.HORIZONTAL_FACING)
                            .getAxis()
                        == net.minecraft.core.Direction.Axis.Z
                    ? -(float) Math.PI / 2
                    : 0)
            .rotateX(KineticShaftRenderer.angle(blockEntity))
            .translate(-.5f, -.5f, -.5f);
    shaft.setTransform(matrix).setChanged();
  }

  @Override
  public void beginFrame(DynamicVisual.Context context) {
    animate(context.partialTick());
  }

  @Override
  public void update(float partialTick) {
    animate(partialTick);
  }

  @Override
  public void updateLight(float partialTick) {
    relight(shaft);
  }

  @Override
  protected void _delete() {
    shaft.delete();
  }

  @Override
  public void collectCrumblingInstances(Consumer<Instance> consumer) {
    consumer.accept(shaft);
  }
}
