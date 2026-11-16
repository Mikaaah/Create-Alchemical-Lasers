package nl.mikaaah.creationaugmenter.client;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import nl.mikaaah.creationaugmenter.CreationAugmenterBlockEntity;

/** Create's native shaft, oriented and rotated around the machine's horizontal kinetic axis. */
public final class CreationKineticVisual
    extends SingleAxisRotatingVisual<CreationAugmenterBlockEntity> {
  public CreationKineticVisual(
      VisualizationContext context, CreationAugmenterBlockEntity be, float partial) {
    super(context, be, partial, Models.partial(CreationPartials.SIDE_SHAFT));
  }
}
