package nl.mikaaah.alchemicallaser.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;

public final class LaserPartials {
  public static final PartialModel SIDE_SHAFT =
      PartialModel.of(AlchemicalLaser.id("block/kinetic/side_shaft"));

  private LaserPartials() {}

  /** Touch before baking so Flywheel registers the partial model dependency. */
  public static void init() {}
}
