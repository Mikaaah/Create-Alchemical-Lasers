#!/usr/bin/env python3
"""Build original mono sound assets for the Alchemical Laser.

Charge is a reference to the installed Minecraft respawn-anchor event.
Everything synthesized here is deterministic original oscillator/noise design.
"""
from pathlib import Path
import argparse, json, hashlib, math, subprocess, zipfile
import numpy as np
from scipy.io import wavfile
from scipy.signal import butter, sosfilt

SR=48000
SCRIPT=Path(__file__).resolve()
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--source-jar',type=Path,required=True)
parser.add_argument('--output',type=Path,default=SCRIPT.parents[1])
parser.add_argument('--vanilla-assets',type=Path,default=Path('/home/agent/.gradle/caches/neoformruntime/assets'))
args=parser.parse_args()
ROOT=args.output;ASSETS=ROOT/'assets/alchemicallaser';SOUNDS=ASSETS/'sounds'
WORK=ROOT/'authoring-wav';SOUNDS.mkdir(parents=True,exist_ok=True);WORK.mkdir(exist_ok=True)
RNG=np.random.default_rng(202611160843)

def time(seconds):return np.arange(round(SR*seconds),dtype=np.float64)/SR
def band_noise(n,low,high):
    noise=RNG.standard_normal(n)
    filt=butter(3,[low,high],btype='bandpass',fs=SR,output='sos')
    sound=sosfilt(filt,noise)
    return sound/(np.std(sound)+1e-12)
def periodic_noise(n,low,high):
    f=np.fft.rfftfreq(n,1/SR)
    # Integer FFT frequencies make the noise genuinely periodic over this loop.
    shape=np.zeros_like(f);keep=(f>=low)&(f<=high)
    shape[keep]=(f[keep]/low)**-.7
    shape*=np.minimum(1,np.maximum(0,(f-low)/80))*np.minimum(1,np.maximum(0,(high-f)/250))
    spectrum=shape*np.exp(1j*RNG.uniform(0,2*np.pi,len(f)))
    sound=np.fft.irfft(spectrum,n=n)
    return sound/(np.std(sound)+1e-12)
def edge_fade(sound,attack=.004,release=.018):
    n=len(sound);a=min(round(attack*SR),n);b=min(round(release*SR),n)
    if a:sound[:a]*=np.sin(np.linspace(0,np.pi/2,a))**2
    if b:sound[-b:]*=np.sin(np.linspace(np.pi/2,0,b))**2
    return sound
def saturate(sound,drive=1.0):return np.tanh(sound*drive)
def normalise(sound,peak=.90):
    sound=sound-np.mean(sound)
    return sound*(peak/(np.max(np.abs(sound))+1e-12))
def pulse_noise(t,start,decay,low,high,amount=1):
    d=t-start;env=np.where(d>=0,np.exp(-np.maximum(d,0)/decay),0)
    return band_noise(len(t),low,high)*env*amount

samples={}
# Two-second seamless magnetic/industrial beam bed. Every oscillator and LFO
# completes an integer number of cycles within the loop, including the FFT air.
t=time(2.0)
core=.51*np.sin(2*np.pi*55*t+.22*np.sin(2*np.pi*6.5*t))
core+=.26*np.sin(2*np.pi*82.5*t+.8+.12*np.sin(2*np.pi*4*t))
body=.17*np.sin(2*np.pi*110*t+.20*np.sin(2*np.pi*8*t))
body+=.10*np.sin(2*np.pi*165*t+.45)+.055*np.sin(2*np.pi*330*t)
grit=periodic_noise(len(t),260,2400)*.050*(.82+.18*np.sin(2*np.pi*8*t))
current=.045*np.sin(2*np.pi*660*t+.8*np.sin(2*np.pi*4.0*t))
loop=saturate((core+body)*(1+.055*np.sin(2*np.pi*8*t))+grit+current,1.28)
samples['beam']=normalise(loop,.92)
# Move the circular boundary to a quiet, low-slope point. The waveform remains
# mathematically periodic and this also minimises Vorbis start-window error.
edge_score=np.abs(samples['beam'])+15*np.abs(np.roll(samples['beam'],-1)-samples['beam'])
samples['beam']=np.roll(samples['beam'],-int(np.argmin(edge_score)))

# Ignition: weighted low drop and metallic-electric crack, then handoff to loop.
t=time(.46);freq=54+138*np.exp(-t*22);phase=2*np.pi*np.cumsum(freq)/SR
whump=.72*np.sin(phase)*np.exp(-t/ .105)
body=.25*np.sin(2*np.pi*146*t)*np.exp(-t/.07)
crack=band_noise(len(t),450,4200)*(.11*np.exp(-t/.018)+.065*np.exp(-t/.10))
arc=.14*np.sin(2*np.pi*(420*t+22*t*t))*np.exp(-t/.085)
samples['fire']=normalise(edge_fade(saturate(whump+body+crack+arc,1.45),.002,.04),.92)

# Pressure release: a valve tap, broad-band breath, then decaying low pressure.
t=time(.86);pressure=(1-np.exp(-t/ .012))*np.exp(-t/.21)
hiss=band_noise(len(t),450,5600)*pressure*.26
air=band_noise(len(t),140,1200)*pressure*.13
valve=pulse_noise(t,.0,.014,700,7000,.15)+pulse_noise(t,.055,.024,350,2100,.075)
f=41+32*np.exp(-t*6);tail=.13*np.sin(2*np.pi*np.cumsum(f)/SR)*np.exp(-t/.19)
samples['shutdown']=normalise(edge_fade(saturate(hiss+air+valve+tail,1.1),.003,.06),.86)

# Quiet short lens interaction sounds, one latch out and a lower two-part seat in.
t=time(.18)
latch=pulse_noise(t,.003,.009,1100,6500,.17)
spring=.12*np.sin(2*np.pi*(560*t-650*t*t))*np.exp(-t/.036)
slide=band_noise(len(t),850,3600)*np.exp(-((t-.055)/.034)**2)*.040
samples['lens_remove']=normalise(edge_fade(latch+spring+slide,.002,.022),.67)

t=time(.24)
click=pulse_noise(t,.003,.008,900,6000,.13)+pulse_noise(t,.078,.011,650,4300,.10)
seat=.24*np.sin(2*np.pi*128*np.maximum(0,t-.077))*np.where(t>=.077,np.exp(-(t-.077)/.045),0)
resonance=.09*np.sin(2*np.pi*640*t)*np.exp(-t/.025)+.04*np.sin(2*np.pi*1190*t)*np.exp(-t/.021)
samples['lens_install']=normalise(edge_fade(click+seat+resonance,.002,.028),.74)

with zipfile.ZipFile(args.source_jar) as archive:
    original_sounds=json.loads(archive.read('assets/alchemicallaser/sounds.json'))
    english=json.loads(archive.read('assets/alchemicallaser/lang/en_us.json'))
    for name in ['activation','complete']:
        (SOUNDS/(name+'.ogg')).write_bytes(archive.read('assets/alchemicallaser/sounds/'+name+'.ogg'))

metadata={}
for name,sound in samples.items():
    wav=WORK/(name+'.wav');ogg=SOUNDS/(name+'.ogg')
    wavfile.write(wav,SR,np.round(np.clip(sound,-1,1)*32767).astype(np.int16))
    subprocess.run(['ffmpeg','-y','-hide_banner','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','5','-ar',str(SR),'-ac','1',str(ogg)],check=True)
    decoded=subprocess.check_output(['ffmpeg','-v','error','-i',str(ogg),'-f','f32le','-acodec','pcm_f32le','-ac','1','-ar',str(SR),'-'])
    pcm=np.frombuffer(decoded,np.float32)
    metadata[name]={'file':f'sounds/{name}.ogg','seconds':len(pcm)/SR,'sample_rate':SR,'channels':1,'decoded_peak_dbfs':round(20*np.log10(np.max(np.abs(pcm))+1e-12),2),'decoded_rms_dbfs':round(20*np.log10(np.sqrt(np.mean(pcm**2))+1e-12),2),'sha256':hashlib.sha256(ogg.read_bytes()).hexdigest()}
    if name=='beam':
        metadata[name]['decoded_loop_boundary_step']=float(abs(pcm[0]-pcm[-1]))
        metadata[name]['decoded_typical_step_rms']=float(np.sqrt(np.mean(np.diff(pcm)**2)))
        assert len(pcm)==2*SR
        assert metadata[name]['decoded_loop_boundary_step']<.02
    assert np.max(np.abs(pcm))<1.0, (name,'decode clipping')

sounds=original_sounds
sounds['charge']={'subtitle':'subtitles.alchemicallaser.charge','sounds':[{'name':'minecraft:block.respawn_anchor.charge','type':'event','volume':.85,'pitch':.95}]}
for name in ['fire','beam','shutdown','lens_remove','lens_install']:
    sounds[name]={'subtitle':f'subtitles.alchemicallaser.{name}','sounds':[{'name':f'alchemicallaser:{name}','attenuation_distance':12}]}
(ASSETS/'sounds.json').write_text(json.dumps(sounds,indent=2)+'\n')
subtitles={'charge':'Laser energy charges','fire':'Heavy laser ignites','beam':'Heavy laser hums','shutdown':'Laser vents pressure','lens_remove':'Lens latch releases','lens_install':'Lens seats in socket'}
for name,value in subtitles.items():english[f'subtitles.alchemicallaser.{name}']=value
(ASSETS/'lang').mkdir(exist_ok=True)
(ASSETS/'lang/en_us.json').write_text(json.dumps(english,indent=2,ensure_ascii=False)+'\n')
metadata['charge']={'vanilla_event':'minecraft:block.respawn_anchor.charge','reference_type':'event','volume_multiplier':.85,'pitch_multiplier':.95,'embedded_mojang_audio':False}
(ROOT/'sound-metadata.json').write_text(json.dumps(metadata,indent=2,default=float)+'\n')

# Audition timeline: real local Vanilla charge, ignition/heavy current, pressure
# release, then both lens latch actions. It is a preview, not an in-game capture.
timeline=np.zeros(round(8.7*SR),np.float64)
def add(clip,start,gain):
    i=round(start*SR);n=min(len(clip),len(timeline)-i)
    timeline[i:i+n]+=clip[:n]*gain
index_path=args.vanilla_assets/'indexes/17.json'
charge_preview=False
if index_path.exists():
    index=json.loads(index_path.read_text())['objects'];key='minecraft/sounds/block/respawn_anchor/charge1.ogg'
    if key in index:
        h=index[key]['hash'];asset=args.vanilla_assets/'objects'/h[:2]/h
        if asset.exists():
            raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(asset),'-f','f32le','-acodec','pcm_f32le','-ac','1','-ar',str(SR),'-'])
            add(np.frombuffer(raw,np.float32),.15,.5);charge_preview=True
add(samples['fire'],1.5,.47)
bed=np.tile(samples['beam'],2)[:round(3.0*SR)].copy()
edge_fade(bed,.12,.20);add(bed,1.52,.42)
add(samples['shutdown'],4.53,.46)
add(samples['lens_remove'],6.10,.65)
add(samples['lens_install'],6.70,.65)
timeline=normalise(timeline,.94)
preview=ROOT/'Alchemical-Laser-Sound-Preview.wav'
wavfile.write(preview,SR,np.round(timeline*32767).astype(np.int16))
subprocess.run(['ffmpeg','-y','-hide_banner','-loglevel','error','-i',str(preview),'-c:a','libmp3lame','-q:a','2',str(ROOT/'Alchemical-Laser-Sound-Preview.mp3')],check=True)
(ROOT/'sound-preview-timeline.json').write_text(json.dumps({'preview':'Audition montage, not in-game capture; dry assets are mixed at demonstration levels. The mod has positional attenuation, lens/RPM pitch and a configurable volume multiplier.','seconds':8.7,'real_vanilla_charge_in_preview':charge_preview,'timeline':[{'seconds':.15,'event':'Vanilla respawn-anchor charge'},{'seconds':1.5,'event':'Custom heavy ignition'},{'seconds':1.52,'event':'Heavy beam loop, repeated continuously for3s'},{'seconds':4.53,'event':'Vent/shutdown pressure release'},{'seconds':6.10,'event':'Lens removal latch'},{'seconds':6.70,'event':'Lens seating clunk'}]},indent=2)+'\n')
print(json.dumps({'output':str(ROOT),'generated':list(samples),'vanilla_charge_preview':charge_preview,'loop_seconds':metadata['beam']['seconds'],'loop_boundary_step':metadata['beam']['decoded_loop_boundary_step']},indent=2))
