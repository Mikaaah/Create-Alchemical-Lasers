package nl.mikaaah.alchemicallaser.client;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;

/** Create's native shaft, oriented and rotated around the machine's horizontal kinetic axis. */
public final class LaserKineticVisual extends SingleAxisRotatingVisual<LaserBlockEntity> {
  public LaserKineticVisual(VisualizationContext context, LaserBlockEntity be, float partial) {
    super(context, be, partial, Models.partial(LaserPartials.SIDE_SHAFT));
  }
}
