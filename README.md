# RIMFAX GPR Processing Pipeline
## Replicating Shoemaker et al. (2024) Methodology

This pipeline processes RIMFAX ground-penetrating radar data from NASA's
Perseverance rover to map subsurface stratigraphy on the Jezero crater floor.

---

## Getting the Data

RIMFAX data is publicly available on the NASA Planetary Data System (PDS):

**URL:** https://pds-geosciences.wustl.edu/missions/mars2020/rimfax.htm

### What to download:
1. **Calibrated radar data** — contains the actual radar traces per sol
2. **Navigation/position data** — rover coordinates for each sounding
3. **Documentation** — describes file formats and metadata

### Data organization:
```
rimfax_data/
├── sol_00383/
│   ├── rimfax_00383_surface.csv
│   ├── rimfax_00383_shallow.csv
│   └── rimfax_00383_deep.csv
├── sol_00384/
│   └── ...
├── nav/
│   ├── rimfax_00383_nav.csv
│   └── ...
```

If you don't have the real data yet, the pipeline generates realistic
synthetic data automatically for demonstration purposes.

---

## Requirements

```bash
pip install numpy scipy matplotlib
```

---

## Quick Start

### Option 1: Run the full pipeline
```python
from rimfax_pipeline import run_full_pipeline

results = run_full_pipeline(
    sols=[384, 387, 389, 398],
    data_dir='./rimfax_data/',
    output_dir='./results/'
)
```

### Option 2: Step-by-step processing
```python
from rimfax_pipeline import *

# STEP 1: Load data for one sol
loader = RIMFAXDataLoader('./rimfax_data/')
data = loader.load_sol_data(384, mode='shallow')

# STEP 2: Process the radargram
processor = RIMFAXProcessor()
processed = processor.process(data)

# STEP 3: Estimate dielectric permittivity
estimator = PermittivityEstimator()
stats = estimator.fit_all_hyperbolas(
    processed['traces'],
    processed['time_axis_ns'],
    processed['positions']
)
print(f"Permittivity: {estimator.get_bulk_permittivity():.1f}")
print(f"Velocity: {estimator.get_bulk_velocity():.3f} m/ns")
print(f"Density: {estimator.get_bulk_density():.2f} g/cm³")

# STEP 4: Convert travel time to depth
converter = DepthConverter(estimator.get_bulk_velocity())
depth_axis = converter.time_to_depth(processed['time_axis_ns'])

# STEP 5: Pick reflector boundaries (automatic)
mapper = StratigraphyMapper(estimator.get_bulk_velocity())
picks = mapper.auto_pick_layers(
    processed['traces'],
    processed['time_axis_ns'],
    processed['positions']
)

# STEP 6: Map stratigraphy
if 'seitah' in picks:
    thickness = mapper.compute_maaz_thickness(picks['seitah'], processed['positions'])
    paleo = mapper.compute_paleosurface(picks['seitah'], processed['positions'])
    relief = mapper.compute_relief(paleo)
    print(f"Máaz thickness: {thickness['mean']:.2f} ± {thickness['std']:.2f} m")

# STEP 7: Visualize
viz = RIMFAXVisualizer()
viz.plot_interpreted_radargram(
    processed['traces'],
    processed['time_axis_ns'],
    processed['positions'],
    picks, sol=384
)
plt.show()
```

### Option 3: Interactive manual picking
```python
# After processing, pick layers manually by clicking on the radargram
mapper = StratigraphyMapper(0.1)
picks = mapper.manual_pick_layer(
    processed['traces'],
    processed['time_axis_ns'],
    processed['positions'],
    layer_name='seitah'
)
```

---

## Pipeline Steps Explained

| Step | Class | What it does |
|------|-------|--------------|
| 1 | `RIMFAXDataLoader` | Load PDS data, stitch surface/shallow/deep modes |
| 2 | `RIMFAXProcessor` | Background removal, Hamming window, gain, filtering |
| 3 | `PermittivityEstimator` | Find & fit hyperbolas → permittivity, velocity, density |
| 4 | `DepthConverter` | Convert two-way travel time to depth (m) |
| 5 | `StratigraphyMapper` | Pick reflector boundaries, map formations |
| 6 | `RIMFAXVisualizer` | Radargrams, thickness maps, paleosurface, relief |
| 7 | `ResultsExporter` | CSV tables matching paper's Tables A1, A2, A3 |

---

## Customizing for Different Sols

Edit the `CONFIG` dictionary at the top of `rimfax_pipeline.py`:

```python
CONFIG = {
    'sols': [400, 401, 402, ...],   # Your sols
    'data_dir': '/path/to/data/',
    'output_dir': '/path/to/results/',
    'assumed_permittivity': 9.0,     # Adjust if needed
    'gain_type': 'spreading',        # or 'agc' or 'sec'
    # ... more options
}
```

---

## Output Files

The pipeline produces:
- `radargram_solXXX.png` — Uninterpreted + interpreted radargrams
- `hyperbolas_solXXX.png` — Radargram with hyperbola fits
- `stratigraphy_solXXX.png` — Cross-section stratigraphic model
- `maaz_thickness.png` — Multi-sol thickness map
- `paleosurface.png` — Surface vs buried Séítah topography
- `relief.png` — Relief of Máaz–Séítah contact
- `stratigraphic_picks.csv` — All reflector picks (Table A1 format)
- `hyperbola_fits.csv` — All hyperbola fits (Table A2 format)
- `analysis_summary.json` — Statistics (Table A3 format)

---

## Key Equations (from the paper)

**Permittivity from velocity:**
```
εr = (c / v)²
```

**Bulk density from permittivity:**
```
ρ ≈ √εr
```

**Depth from travel time:**
```
depth = (c × Δt) / (2 × √εr)
```

**Hyperbola model:**
```
t(x) = √(t0² + (2Δx/v)²)
```

---

## Reference

Shoemaker, E. S. et al. (2024). "Observations of Igneous Subsurface
Stratigraphy during the Jezero Crater Floor Rapid Traverse from the
RIMFAX Ground-penetrating Radar." *The Planetary Science Journal*, 5:191.
https://doi.org/10.3847/PSJ/ad6445
