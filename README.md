# Floating Slime Pet 💧

An interactive floating desktop companion and AI conversational buddy rewritten in React + TypeScript + Vite.

## Features

- **Interactive Floating Overlay**: Drag Slime anywhere around the browser window with smooth physics and edge-snapping, replicating the Android WindowManager overlay.
- **Dynamic Emotion & Expressions**: Slime visually reacts to its vitals and user interactions with custom canvas rendering matching `SlimeView.kt` (HAPPY, SAD, SLEEPY, and NEUTRAL moods with dynamic eyes, mouth arcs, and specular shines).
- **Three Core Gestures**:
  - **Single Tap**: Slime Tap Talk (`slimeTapTalk`) — Slime chats with playful replies using OpenRouter (`openai/gpt-4o-mini`).
  - **Double Tap**: Screen Perception (`slimeAskWhatYouSee`) — Slime observes what's on your screen and shares thoughts.
  - **Long Press / Hold**: Feed Slime (`slimeFeed`) — restores +20 hunger with feeding bounce animation and speech bubble ("Nom! +20 hunger").
- **Care Vitals & Background Worker**: Port of Android's `SlimeStats` and `HungerWorker` with continuous hunger decay over time, happiness, energy, and level progression.
- **OpenRouter AI Connectivity**: Configure your API key, test AI connectivity, and manage settings in real time.
- **Foreground Service Simulation**: Toggle overlay visibility on and off seamlessly.

## Getting Started

1. Install dependencies:
   ```bash
   npm install
   ```
2. Run development server:
   ```bash
   npm run dev
   ```
3. Build for production:
   ```bash
   npm run build
   ```
