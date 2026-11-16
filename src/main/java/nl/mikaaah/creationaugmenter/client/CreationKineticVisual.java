package nl.mikaaah.creationaugmenter.client;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import nl.mikaaah.creationaugmenter.*;

/** Native GPU rotation for the horizontal authored 4x4 shaft. */
public final class CreationKineticVisual
    extends OrientedRotatingVisual<CreationAugmenterBlockEntity> {
  public CreationKineticVisual(
      VisualizationContext context, CreationAugmenterBlockEntity be, float partial) {
    super(
        context,
        be,
        partial,
        Direction.EAST,
        Direction.get(
            Direction.AxisDirection.POSITIVE,
            be.getBlockState().getValue(CreationAugmenterBlock.HORIZONTAL_FACING).getAxis()),
        Models.partial(CreationPartials.SIDE_SHAFT));
  }
}
