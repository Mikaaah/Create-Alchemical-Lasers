package nl.mikaaah.alchemicallaser.client;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import nl.mikaaah.alchemicallaser.*;

/** Native GPU rotation for the horizontal authored 4x4 shaft. */
public final class LaserKineticVisual extends OrientedRotatingVisual<LaserBlockEntity> {
  public LaserKineticVisual(VisualizationContext context, LaserBlockEntity be, float partial) {
    super(
        context,
        be,
        partial,
        Direction.EAST,
        Direction.get(
            Direction.AxisDirection.POSITIVE,
            be.getBlockState().getValue(LaserBlock.HORIZONTAL_FACING).getAxis()),
        Models.partial(LaserPartials.SIDE_SHAFT));
  }
}
