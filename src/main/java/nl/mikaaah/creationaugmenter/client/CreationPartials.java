package nl.mikaaah.creationaugmenter.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import nl.mikaaah.creationaugmenter.CreationAugmenter;

public final class CreationPartials {
  public static final PartialModel SIDE_SHAFT =
      PartialModel.of(CreationAugmenter.id("block/creation_augmenter/shaft"));

  private CreationPartials() {}

  /** Touch before baking so Flywheel registers the partial model dependency. */
  public static void init() {}
}
