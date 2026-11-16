"""Validate runtime geometry, UV bounds, resources and authoring-space animation channels."""
from pathlib import Path
import json, math
from PIL import Image
root=Path(__file__).resolve().parents[1]
a=root/'src/main/resources/assets/alchemicallaser'
required={'idle','spin_up','spin','spin_down','lens_remove','lens_insert','firing','firing_loop','kinetic_preview_60rpm'}
clips=json.loads((a/'animations/machine.authoring.json').read_text())['animations']
assert {name.rsplit('.',1)[1] for name in clips}==required
machine=json.loads((a/'geometry/machine.json').read_text())
base={bone['name'] for bone in machine['bones']}
cubes=0
for p in (a/'geometry').rglob('*.json'):
 model=json.loads(p.read_text()); names=base|{bone['name'] for bone in model['bones']}
 for bone in model['bones']:
  assert bone.get('parent') in names|{None,''}, (p,bone)
 for texture,size in model['textures'].items():
  namespace,path=texture.split(':',1); assert namespace=='alchemicallaser'
  image=a/path;assert image.is_file(),image
  with Image.open(image) as im:assert list(im.size)==size,(image,im.size,size)
 for cube in model['cubes']:
  cubes+=1
  assert cube['group'] in names,(p,cube['group'])
  assert all(math.isfinite(v) for field in ['from','to','origin','rotation'] for v in cube[field]),p
  for face in cube['faces'].values():
   w,h=model['textures'][face['texture']];u0,v0,u1,v1=face['uv']
   assert 0<=u0<=w and 0<=u1<=w and 0<=v0<=h and 0<=v1<=h,(p,face)
for clip in clips.values():
 for name,channels in clip['bones'].items():
  assert name in base,name
  for channel,frames in channels.items():
   assert channel in {'position','rotation','scale'},channel
   for time,value in frames.items():
    assert 0<=float(time)<=clip['animation_length']+1e-6,(time,clip['animation_length'])
    assert len(value)==3 and all(math.isfinite(v) for v in value),value
for particle in (a/'particles').glob('*.json'):
 for texture in json.loads(particle.read_text())['textures']:
  ns,path=texture.split(':',1);assert ns=='alchemicallaser';assert (a/'textures/particle'/f'{path}.png').is_file(),texture
print(f'Industrial assets passed: {cubes} cubes across six models, nine animation clips, UV bounds, bone parents and particle sprites.')
