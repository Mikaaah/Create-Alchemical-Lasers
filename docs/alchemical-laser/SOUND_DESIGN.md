# Alchemical Laser sound design

Charge references Minecraft's `minecraft:block.respawn_anchor.charge` event.
Shutoff uses `SoundEvents.LAVA_EXTINGUISH`, the Vanilla lava-cooling hiss.
Vanilla audio files are not redistributed as mod assets. The heavy beam,
ignition and lens interaction sounds are deterministic original designs made
with oscillators, filtered noise, envelopes and light saturation.

| Event | Asset / reference | Purpose |
| --- | --- | --- |
| `charge` | Vanilla respawn-anchor charge event | State-controlled magical charge; long tails cannot overlap processing cycles |
| `fire` | `sounds/fire.ogg` | Low ignition impulse with a short electrical crack |
| `beam` | `sounds/beam.ogg` | Seamless 2-second heavy current loop with a 55Hz magnetic body, harmonics and filtered electrical air |
| Shutoff | Direct `SoundEvents.LAVA_EXTINGUISH` call | One cooling hiss when processing finishes or firing is interrupted; volume `0.5F × config`, pitch `1.15F` |
| `shutdown` | Vanilla lava-extinguish event reference | Retained sound ID for compatibility; the runtime shutoff uses the direct server call above |
| `lens_remove` | `sounds/lens_remove.ogg` | Short latch release with a restrained spring/slide sound |
| `lens_install` | `sounds/lens_install.ogg` | Two-part metal seat with a lower mechanical clunk |
| `activation` | Existing audio retained | Existing machine activation cue |
| `complete` | Existing audio retained | Existing short processing-complete cue |

Custom sound assets are mono 48kHz Vorbis for positional attenuation; custom
entries use a 12-block attenuation distance. Audio uses Minecraft's Blocks
category and the mod's sound-volume setting. Charge and beam pitch retain
their lens/RPM behavior. The shutoff hiss has fixed pitch `1.15F`, independent
of lens and RPM. No custom shutdown OGG is used.

Each machine owns one charge instance. The instance releases over two ticks
when charging ends. The beam loop fades over four ticks and stops on shutdown,
unload, removal, world change or leaving range. Normal completion and interrupted
processing each produce one server-side cooling hiss. Cancelling charge before
firing does not trigger the firing shutoff.

The client emits up to two `ParticleTypes.SMOKE` particles at each endpoint on
the firing-to-stopped edge: one plume at the moving nozzle, one at the fixed
impact. Each particle has scale `0.28`, a 10–15 tick lifetime, and restored upward
velocity `0.02–0.04`. Decreased particle settings allow one per endpoint; Minimal
allows none. There is no large-smoke burst. Active beams retain the layered
geometry, original chamber arcs and sparse lens-colored impact sparks; soul,
flame and continuous beam-smoke emissions are removed.

The beam waveform is periodic over 2 seconds and its loop boundary is placed at
a quiet low-slope point before encoding. Decoded loop verification measured
96,000 samples, a boundary step of `0.00284`, and typical sample-step RMS of
`0.00798`. See `sound-metadata.json` for asset measurements and hashes.

## Preview

`preview/Alchemical-Laser-Sound-Preview.mp3` is an 8.7-second audition montage:

| Time | Sound |
| --- | --- |
| 0.15s | A local Vanilla respawn-anchor charge variation |
| 1.50s | Custom heavy beam ignition |
| 1.52–4.52s | Heavy beam current with a smooth entrance and exit |
| 4.53s | Vanilla lava-extinguish cooling hiss |
| 6.10s | Lens removal latch |
| 6.70s | Lens insertion seating clunk |

The montage uses demonstration mix levels. In-game volume also depends on the
Blocks slider, mod volume, source distance and lens/RPM pitch. This is an audio
audition, not an in-game recording.

## Regeneration

```bash
python3 tools/build_sound_assets.py --source-jar /path/to/alchemical-lasers.jar --output /path/to/project
```

Requires NumPy, SciPy, and `ffmpeg` with Vorbis/MP3 encoding. The optional
`--vanilla-assets` argument points to an existing Minecraft asset cache for the
audition's charge and cooling samples. Runtime resources use Vanilla event
references, so the mod does not bundle those samples. The generator writes
custom assets under `src/main/resources`, authoring WAVs under `art/audio-authoring`,
audio documentation under `docs`, and the audition under `preview`.
