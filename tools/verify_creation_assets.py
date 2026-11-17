"""Check both 31px machines, finite lens assets and the synchronized 25-frame core."""
from pathlib import Path
from PIL import Image
import json, math
root=Path(__file__).resolve().parents[1]; assets=root/'src/main/resources/assets';totals=[]
for ns,expected_lenses,clips in [('alchemicallaser',5,9),('creationaugmenter',3,10)]:
 a=assets/ns; model=json.loads((a/'geometry/machine.json').read_text());base={b['name'] for b in model['bones']}
 assert len(model['cubes'])=={'alchemicallaser':31,'creationaugmenter':34}[ns], (ns,'Expected simplified 31/34-cuboid machine',len(model['cubes']))
 assert model['bounds'][1]==-15 and model['bounds'][4]==16,(ns,model['bounds'])
 files=list((a/'geometry/lenses').glob('*.json'));assert len(files)==expected_lenses
 for file in [a/'geometry/machine.json']+files:
  g=json.loads(file.read_text());names=base|{b['name'] for b in g['bones']}
  assert not any('railway_casing' in json.dumps(face) for c in g['cubes'] for face in c['faces'].values())
  for texture,size in g['textures'].items():
   owner,path=texture.split(':',1);assert owner==ns
   with Image.open(a/path) as image: assert list(image.size)==size,(texture,image.size,size)
  for b in g['bones']:assert b.get('parent') in names|{None,''},(file,b)
  for c in g['cubes']:
   assert c['group'] in names
   assert all(math.isfinite(x) for k in ['from','to','origin','rotation'] for x in c[k])
   for face in c['faces'].values():
    w,h=g['textures'][face['texture']];u0,v0,u1,v1=face['uv']
    if face.get('animated_core'):assert w==16 and max(v0,v1)<=16
    assert 0<=u0<=w and 0<=u1<=w and 0<=v0<=h and 0<=v1<=h,(file,face)
 anim=json.loads((a/'animations/machine.authoring.json').read_text())['animations'];assert len(anim)==clips,(ns,len(anim))
 for name,clip in anim.items():
  for bone,channels in clip['bones'].items():
   assert bone in base,(name,bone)
   for channel,frames in channels.items():
    for time,v in frames.items():assert float(time)>=0 and len(v)==3 and all(math.isfinite(x) for x in v),(name,time,v)
 lang=json.loads((a/'lang/en_us.json').read_text())
 for name,event in json.loads((a/'sounds.json').read_text()).items():
  assert event['subtitle'] in lang
  for sound in event['sounds']:
   if isinstance(sound,dict) and sound.get('type')=='event':continue
   owner,path=(sound['name'] if isinstance(sound,dict) else sound).split(':',1)
   assert owner==ns and (a/'sounds'/f'{path}.ogg').is_file()
 totals.append((ns,len(model['cubes']),len(files),clips))
a=assets/'creationaugmenter'
for name in ['creation_core.png','creation_core_e.png']:
 with Image.open(a/'textures/block'/name) as image:assert image.size==(16,400)
 anim=json.loads((a/'textures/block'/f'{name}.mcmeta').read_text())['animation'];assert anim['frametime']==2 and anim['interpolate']
for f in (a/'textures/item').glob('*.png'):
 with Image.open(f) as image:assert image.size==(16,16),f
for p in (root/'src/main/java').rglob('*.java'):
 text=p.read_text();assert not any(x in text for x in ['ParticleTypes.SOUL','ParticleTypes.SOUL_FIRE_FLAME','ParticleTypes.LARGE_SMOKE','endVertex(']),p
print('Both-machine validation passed:',totals,'25-frame core, masks, sprites, sounds, UVs and no soul/large-smoke emissions.')
