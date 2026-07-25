# Texture Optimization Mod for Starsector

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE) ![wakatime](https://wakatime.com/badge/user/031e2989-d7de-482e-b163-53be0a26f8aa/project/0ada6441-05a6-4d80-94c7-6efebf21afeb.svg)
![80% less vram usage!!!](/Promotional/Card.GIF)

A performance and memory optimization mod for **Starsector** that converts vanilla and modded textures into the **DirectDraw Surface (DDS)** format with BC7 compression.

By leveraging native GPU-supported texture compression, this mod reduces VRAM usage by **over 75%**.

---

## Key Features

* **Massive VRAM Reduction:** Cuts texture memory footprint by 75% to 85%, allowing you to run massive modpacks without hitting memory limits.
* **Smart Caching & Verification:** Caches processed assets and tracks file modification dates to avoid redundant conversions.

---

## 📦 Installation & Setup

[![trios](https://trilink.wispborne.com/badges/install-badge-forthebadge.svg)](https://trilink.wispborne.com/open.html?mod=%7B%22url%22%3A%22https%3A%2F%2Fraw.githubusercontent.com%2FDeCEll-1%2FVramOptimizer%2Frefs%2Fheads%2Fmaster%2FVOpt.version%22%2C%22id%22%3A%22VramOptimizer%22%2C%22version%22%3A%221.0.0%22%7D)

1. Choose the correct download archive for your operating system and architecture:
   * **Windows (64-bit):** [`VramOptimizer-win-x64.zip`](https://github.com/DeCEll-1/VramOptimizer/releases/latest/download/VramOptimizer-win-x64.zip)
   * **Windows (32-bit):** [`VramOptimizer-win-x86.zip`](https://github.com/DeCEll-1/VramOptimizer/releases/latest/download/VramOptimizer-win-x86.zip)
   * **Linux (64-bit):** [`VramOptimizer-linux-x64.tar.gz`](https://github.com/DeCEll-1/VramOptimizer/releases/latest/download/VramOptimizer-linux-x64.tar.gz)
   * **macOS (64-bit):** [`VramOptimizer-osx-x64.tar.gz`](https://github.com/DeCEll-1/VramOptimizer/releases/latest/download/VramOptimizer-osx-x64.tar.gz)

2. Extract the archive. Inside, you will find the main mod folder named **`VramOptimizer`**.
3. Move or copy the **`VramOptimizer`** folder directly into your Starsector **`mods/`** directory

**Run the Generator:**

* Navigate into your Starsector `mods/VramOptimizer/bin/` folder.
* Run the executable (`DDSCreator.exe` on Windows or the equivalent binary on Linux/macOS).
* Use the interactive menu to review or edit your enabled mods, then select **ProcessMods** to generate your DDS textures.
* This may take quite long especially depending on your hardware

1. Launch the Starsector Launcher and enable **VramOptimizer** in your mod list.

---

## ⚙️ Requirements & Compatibility

* **GPU Requirements (BC7 / DDS):** A graphics card supporting **BC7 (Block Compression 7)** texture decompression. Practically any modern GPU from the last decade supports this natively in hardware, including:

---

## 📜 Credits

* **Libraries Used:**
* `BCnEncoder.Net` (v2.3.0) - Texture compression
* `CsvHelper` (v33.1.0) - Data parsing
* `Magick.NET-Q16-AnyCPU` (v14.15.0) - Image manipulation
* `Newtonsoft.Json` (v13.0.5-beta1) - JSON serialization/deserialization
* `ShellProgressBar` (v5.2.0) - CLI progress bars
* `Spectre.Console` (v0.57.2) - Interactive CLI UI
