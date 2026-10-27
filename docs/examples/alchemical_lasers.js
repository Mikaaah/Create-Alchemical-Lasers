// Place in kubejs/server_scripts/. KubeJS Create is needed for the assembly helper.
ServerEvents.recipes(event => {
  event.recipes.alchemicallaser.laser_processing()
    .LENS('minecraft:coal').COLOR('#FF8033').CONSUME(true)
    .INPUT('minecraft:sand').OUTPUT('minecraft:glass')
    .duration(20).minimumRpm(16).id('tnk:coal_laser_glass')
  event.recipes.alchemicallaser.laser_processing(
    'minecraft:diamond', '2244FF', false, 'minecraft:dirt', 'minecraft:emerald'
  ).id('tnk:diamond_laser_emerald')
  event.recipes.alchemicallaser.laser_processing()
    .lens('minecraft:diamond').color('#2244FF').consume(false)
    .input('minecraft:gravel').output('minecraft:flint')
    .id('tnk:diamond_laser_flint')
  const incomplete = 'create:incomplete_precision_mechanism'
  event.recipes.create.sequenced_assembly(['minecraft:diamond'], 'minecraft:iron_ingot', [
    event.recipes.alchemicallaser.laser_processing()
      .LENS('minecraft:coal').COLOR('#FF8033').CONSUME(true)
      .INPUT(incomplete).OUTPUT(incomplete).duration(20),
    event.recipes.create.pressing(incomplete, incomplete),
    event.recipes.alchemicallaser.laser_processing()
      .LENS('alchemicallaser:eclipse_lens').COLOR('#A286DC').CONSUME(false)
      .INPUT(incomplete).OUTPUT(incomplete).duration(20)
  ]).transitionalItem(incomplete).loops(2).id('tnk:laser_assembly')
})
