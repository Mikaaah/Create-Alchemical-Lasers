ServerEvents.recipes(event => {
  event.recipes.alchemicallaser.laser_processing()
    .LENS('minecraft:coal').COLOR('#FF8033').CONSUME(true)
    .INPUT('minecraft:sand').OUTPUT('minecraft:glass')
    .duration(20).minimumRpm(16).id('tnk:kjs_consumable')
  event.recipes.alchemicallaser.laser_processing(
    'minecraft:diamond', '2244FF', false, 'minecraft:dirt', 'minecraft:emerald'
  ).id('tnk:kjs_positional')
  event.recipes.alchemicallaser.laser_processing()
    .lens('minecraft:diamond').color('#2244FF').consume(false)
    .input('minecraft:gravel').output('minecraft:flint')
    .id('alchemicallaser:custom_same_namespace')
  event.recipes.alchemicallaser.laser_processing()
    .LENS('alchemicallaser:power_lens')
    .INPUT('minecraft:granite').OUTPUT('minecraft:stone')
    .id('tnk:kjs_default_color')
  const incomplete = 'create:incomplete_precision_mechanism'
  event.recipes.create.sequenced_assembly(['minecraft:diamond'], 'minecraft:iron_ingot', [
    event.recipes.alchemicallaser.laser_processing()
      .LENS('minecraft:coal').COLOR('#FF8033').CONSUME(true)
      .INPUT(incomplete).OUTPUT(incomplete).duration(20),
    event.recipes.create.pressing(incomplete, incomplete),
    event.recipes.alchemicallaser.laser_processing()
      .LENS('alchemicallaser:eclipse_lens').COLOR('#A286DC').CONSUME(false)
      .INPUT(incomplete).OUTPUT(incomplete).duration(20)
  ]).transitionalItem(incomplete).loops(2).id('tnk:kjs_assembly')
})
