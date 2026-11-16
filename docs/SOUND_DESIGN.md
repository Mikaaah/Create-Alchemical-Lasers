# Alchemical Laser sound design

The charge uses the installed Minecraft event `minecraft:block.respawn_anchor.charge`.
The event was verified against the Minecraft1.21.1 asset index and `sounds.json`.
It selects one of Vanilla's three charge variations. This package references the
event; it does not distribute those Vanilla audio files as mod assets.

The new beam, ignition, pressure release and lens sounds are deterministic original
sound designs made with oscillators, filtered noise, envelopes and light saturation.
The source generator is `tools/build_sound_assets.py`.

| Event | Asset / reference | Purpose |
| --- | --- | --- |
| `charge` | Vanilla respawn-anchor charge event | Magical charge; state-controlled so long tails cannot overlap cycles |
| `fire` | `sounds/fire.ogg` | Low-weighted ignition impulse with a short electrical crack |
| `beam` | `sounds/beam.ogg` | Seamless2second heavy current loop:55Hz magnetic body,82.5Hz secondary,110/165/330Hz harmonics and filtered electrical air |
| `shutdown` | `sounds/shutdown.ogg` | Valve tap, pressure hiss and falling low-frequency tail for the smoke discharge |
| `lens_remove` | `sounds/lens_remove.ogg` | Short latch release with a restrained spring/slide sound |
| `lens_install` | `sounds/lens_install.ogg` | Two-part metal seat with a lower mechanical clunk |
| `activation` | Existing audio retained | Existing machine activation cue |
| `complete` | Existing audio retained | Existing short processing-complete cue |

All new OGG assets are mono48kHz Vorbis, so the normal positional sound engine can
attenuate them from the block. Custom file entries use12block attenuation distance.
The existing sound-volume setting remains the overall user control. Lens pitch and
moderate RPM variation are applied by the runtime. The code integration handles
the beam fade, state transitions, smoke particles and one-shot lens interaction cues.

The beam waveform is mathematically periodic over2seconds, and its loop boundary
is placed at a quiet low-slope point before encoding. Decoded loop verification:
96,000samples,2.000seconds, boundary step0.00284 versus typical sample-step RMS0.00798.
The decoded source peak is below0dBFS for every new file; full figures and hashes are
in `sound-metadata.json`.

## Preview

`Alchemical-Laser-Sound-Preview.mp3` / `.wav` is an8.7second audition montage:

| Time | Sound |
| --- | --- |
|0.15s | A local Vanilla respawn-anchor charge variation |
|1.50s | Custom heavy beam ignition |
|1.52–4.52s | Heavy beam current with a smooth entrance and exit |
|4.53s | Vent / smoke-discharge hiss |
|6.10s | Lens removal latch |
|6.70s | Lens insertion seating clunk |

The montage uses demonstration mix levels. In-game volume also depends on the
Blocks slider, mod volume setting, source distance and current lens/RPM pitch.
It is an audio audition, not an in-game recording.

## Regeneration

```bash
python3 tools/build_sound_assets.py --source-jar /path/to/alchemical-lasers.jar --output /path/to/output
```

Python dependencies:NumPy and SciPy. `ffmpeg` with Vorbis and MP3 encoding must be
available on PATH. The optional `--vanilla-assets` argument points to an existing
Minecraft asset cache for the audition's charge sound; the distributable mod assets
always use the event reference.

The integrated runtime owns one charge instance per machine. It releases over two ticks when charging ends. The beam fades over four ticks; normal completion and interrupted firing each emit one shutdown hiss and smoke puff.
