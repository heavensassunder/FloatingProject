import React, { useState, useEffect } from 'react';
import { SlimeStats, SlimeMood, calculateMood, copyWithHungerDelta } from '../types';
import { slimeStore } from '../services/slimeStore';
import { OpenRouterClient } from '../services/openRouter';
import {
  Key,
  Save,
  Play,
  Layers,
  Heart,
  Zap,
  Coffee,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  Smartphone,
  Eye,
  Info,
  Sliders,
  ExternalLink,
} from 'lucide-react';
import { SlimeCanvas } from './SlimeCanvas';

interface MainSettingsViewProps {
  stats: SlimeStats;
  onUpdateStats: (newStats: SlimeStats) => void;
  floatingEnabled: boolean;
  onToggleFloating: (enabled: boolean) => void;
}

export const MainSettingsView: React.FC<MainSettingsViewProps> = ({
  stats,
  onUpdateStats,
  floatingEnabled,
  onToggleFloating,
}) => {
  const [apiKeyInput, setApiKeyInput] = useState('');
  const [saveStatus, setSaveStatus] = useState<string | null>(null);
  const [testStatus, setTestStatus] = useState<{
    loading: boolean;
    response?: string;
    error?: string;
  }>({ loading: false });

  const [activeTab, setActiveTab] = useState<'settings' | 'pet' | 'vision'>('settings');

  const mood: SlimeMood = calculateMood(stats);

  useEffect(() => {
    setApiKeyInput(slimeStore.getApiKey());
  }, []);

  const handleSaveApiKey = () => {
    const key = apiKeyInput.trim();
    if (!key) {
      setSaveStatus('Please enter an API key');
      setTimeout(() => setSaveStatus(null), 3000);
      return;
    }
    slimeStore.saveApiKey(key);
    setSaveStatus('Saved API key');
    setTimeout(() => setSaveStatus(null), 3000);
  };

  const handleTestAI = async () => {
    const key = apiKeyInput.trim() || slimeStore.getApiKey();
    if (!key) {
      setTestStatus({
        loading: false,
        error: 'Enter API key first to test AI',
      });
      return;
    }

    setTestStatus({ loading: true });
    try {
      const client = new OpenRouterClient(key);
      const sys =
        'You are Slime, a cute floating buddy on user\'s phone. Short playful replies (max 2 sentences).';
      const text = await client.chat(sys, 'Hello slime! Say something playful.');
      setTestStatus({
        loading: false,
        response: text,
      });
    } catch (err: any) {
      setTestStatus({
        loading: false,
        error: err.message || 'Error communicating with OpenRouter',
      });
    }
  };

  const handleFeed = () => {
    const updated = copyWithHungerDelta(stats, 20);
    const withHappiness = {
      ...updated,
      happiness: Math.min(100, updated.happiness + 5),
    };
    onUpdateStats(withHappiness);
  };

  const handlePet = () => {
    onUpdateStats({
      ...stats,
      happiness: Math.min(100, stats.happiness + 15),
    });
  };

  const handleNap = () => {
    onUpdateStats({
      ...stats,
      energy: Math.min(100, stats.energy + 35),
      happiness: Math.min(100, stats.happiness + 5),
    });
  };

  const handleResetStats = () => {
    onUpdateStats({
      hunger: 100,
      happiness: 100,
      energy: 100,
      level: 1,
    });
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* App Header */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-8 bg-slate-800/80 backdrop-blur-md p-6 rounded-3xl border border-slate-700/60 shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-gradient-to-tr from-sky-600 to-cyan-400 rounded-2xl flex items-center justify-center shadow-lg shadow-sky-500/20">
            <span className="text-3xl">💧</span>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-white">Floating Slime Pet</h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-sky-500/20 text-sky-400 border border-sky-500/30">
                v1.0
              </span>
            </div>
            <p className="text-sm text-slate-400">
              Interactive floating desktop companion & AI conversational buddy
            </p>
          </div>
        </div>

        {/* Floating Overlay Status Toggle (Overlay Permission) */}
        <div className="flex items-center gap-3">
          <button
            onClick={() => onToggleFloating(!floatingEnabled)}
            className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-medium text-sm transition-all shadow-md ${
              floatingEnabled
                ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-900/30'
                : 'bg-slate-700 hover:bg-slate-600 text-slate-300'
            }`}
          >
            <Layers className="w-4 h-4" />
            <span>{floatingEnabled ? 'Overlay Enabled 💧' : 'Enable Overlay'}</span>
          </button>
        </div>
      </div>

      {/* Foreground Service Notification Banner (simulating Android floating service) */}
      {floatingEnabled && (
        <div className="mb-8 bg-gradient-to-r from-sky-950/60 to-slate-900 border border-sky-500/30 rounded-2xl p-4 flex items-center justify-between gap-4 shadow-lg animate-in fade-in">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-sky-500/20 text-sky-400 flex items-center justify-center shrink-0">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <div className="text-sm font-semibold text-sky-200">
                Foreground Service: Slime is floating 💧
              </div>
              <div className="text-xs text-slate-400">
                Overlay active on screen. Drag freely or tap Slime to talk.
              </div>
            </div>
          </div>
          <div className="text-xs font-mono px-2.5 py-1 rounded bg-sky-500/10 text-sky-300 border border-sky-500/20">
            Active
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-700/80 pb-3 mb-6">
        <button
          onClick={() => setActiveTab('settings')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-colors ${
            activeTab === 'settings'
              ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
              : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
          }`}
        >
          <Sliders className="w-4 h-4" />
          <span>App Controls & API</span>
        </button>
        <button
          onClick={() => setActiveTab('pet')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-colors ${
            activeTab === 'pet'
              ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
              : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
          }`}
        >
          <Heart className="w-4 h-4" />
          <span>Slime Stats & Care</span>
        </button>
        <button
          onClick={() => setActiveTab('vision')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-colors ${
            activeTab === 'vision'
              ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
              : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
          }`}
        >
          <Eye className="w-4 h-4" />
          <span>Screen Sight & Interactions</span>
        </button>
      </div>

      {/* Tab 1: Settings & API Key Configuration (MainActivity.kt) */}
      {activeTab === 'settings' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* API Key Box */}
          <div className="bg-slate-800/60 rounded-3xl p-6 border border-slate-700/60 shadow-lg flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 mb-3">
                <Key className="w-5 h-5 text-sky-400" />
                <h2 className="text-lg font-semibold text-white">OpenRouter API Configuration</h2>
              </div>
              <p className="text-sm text-slate-400 mb-4">
                Enter your OpenRouter API key to enable intelligent, playful conversations and screen perception for Slime.
              </p>

              <div className="space-y-3">
                <label className="block text-xs font-medium uppercase tracking-wider text-slate-300">
                  OpenRouter API Key
                </label>
                <div className="relative">
                  <input
                    type="password"
                    id="api_key_input"
                    value={apiKeyInput}
                    onChange={(e) => setApiKeyInput(e.target.value)}
                    placeholder="sk-or-v1-..."
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500"
                  />
                </div>

                {saveStatus && (
                  <div
                    className={`flex items-center gap-2 text-xs font-medium p-2 rounded-lg ${
                      saveStatus.includes('Please')
                        ? 'bg-amber-950/60 text-amber-300 border border-amber-800/40'
                        : 'bg-emerald-950/60 text-emerald-300 border border-emerald-800/40'
                    }`}
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    <span>{saveStatus}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-3 pt-6 border-t border-slate-700/60 mt-6">
              <button
                id="save_button"
                onClick={handleSaveApiKey}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-medium text-sm transition-colors shadow-lg shadow-sky-600/20 cursor-pointer"
              >
                <Save className="w-4 h-4" />
                <span>Save API Key</span>
              </button>

              <button
                id="test_button"
                onClick={handleTestAI}
                disabled={testStatus.loading}
                className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-700 hover:bg-slate-600 text-slate-200 font-medium text-sm transition-colors cursor-pointer disabled:opacity-50"
              >
                <Play className="w-4 h-4 text-emerald-400" />
                <span>{testStatus.loading ? 'Testing...' : 'Test AI'}</span>
              </button>
            </div>
          </div>

          {/* AI Status & Test Output */}
          <div className="bg-slate-800/60 rounded-3xl p-6 border border-slate-700/60 shadow-lg flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 mb-3">
                <Sparkles className="w-5 h-5 text-amber-400" />
                <h2 className="text-lg font-semibold text-white">AI Diagnostics & Response</h2>
              </div>
              <p className="text-sm text-slate-400 mb-4">
                Verify AI connectivity with OpenRouter (<code className="text-sky-300">openai/gpt-4o-mini</code>).
              </p>

              {testStatus.loading && (
                <div className="bg-slate-900/80 border border-sky-500/30 rounded-2xl p-4 flex items-center gap-3 text-sky-300">
                  <div className="w-4 h-4 border-2 border-sky-400 border-t-transparent rounded-full animate-spin" />
                  <span className="text-sm font-medium">Communicating with Slime AI...</span>
                </div>
              )}

              {testStatus.response && (
                <div className="bg-slate-900/90 border border-emerald-500/40 rounded-2xl p-4 text-emerald-200 animate-in fade-in">
                  <div className="flex items-center gap-1.5 text-xs font-semibold uppercase text-emerald-400 mb-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Slime Response</span>
                  </div>
                  <p className="text-sm italic">"{testStatus.response}"</p>
                </div>
              )}

              {testStatus.error && (
                <div className="bg-rose-950/60 border border-rose-500/40 rounded-2xl p-4 text-rose-200 animate-in fade-in">
                  <div className="flex items-center gap-1.5 text-xs font-semibold uppercase text-rose-400 mb-1.5">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    <span>Error</span>
                  </div>
                  <p className="text-xs font-mono">{testStatus.error}</p>
                </div>
              )}

              {!testStatus.loading && !testStatus.response && !testStatus.error && (
                <div className="bg-slate-900/40 border border-slate-700/50 rounded-2xl p-4 text-slate-400 text-sm">
                  Click <strong>Test AI</strong> to test your key with Slime's greeting prompt.
                </div>
              )}
            </div>

            {/* Quick Link & Overlay Info */}
            <div className="mt-6 pt-4 border-t border-slate-700/60 text-xs text-slate-400 flex items-center justify-between">
              <span>OpenRouter Model: <span className="text-slate-200">gpt-4o-mini</span></span>
              <a
                href="https://openrouter.ai/keys"
                target="_blank"
                rel="noreferrer"
                className="text-sky-400 hover:underline flex items-center gap-1"
              >
                <span>Get API key</span>
                <ExternalLink className="w-3 h-3" />
              </a>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Slime Stats & Care (SlimeStats & HungerWorker) */}
      {activeTab === 'pet' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Slime Preview Card */}
          <div className="bg-slate-800/60 rounded-3xl p-6 border border-slate-700/60 shadow-lg flex flex-col items-center justify-center text-center">
            <div className="mb-2">
              <SlimeCanvas mood={mood} size={160} />
            </div>
            <div className="mt-2">
              <h3 className="text-xl font-bold text-white capitalize">Slime ({mood.toLowerCase()})</h3>
              <p className="text-xs text-slate-400 mt-1">Level {stats.level} Digital Companion</p>
            </div>
            <div className="mt-4 flex items-center gap-2">
              <span
                className={`text-xs px-3 py-1 rounded-full font-semibold border ${
                  mood === 'HAPPY'
                    ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                    : mood === 'SAD'
                    ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                    : mood === 'SLEEPY'
                    ? 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                    : 'bg-sky-500/20 text-sky-400 border-sky-500/30'
                }`}
              >
                Mood: {mood}
              </span>
            </div>
          </div>

          {/* Pet Stats Meters */}
          <div className="md:col-span-2 bg-slate-800/60 rounded-3xl p-6 border border-slate-700/60 shadow-lg flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-white">Vitals & HungerWorker Status</h3>
                <button
                  onClick={handleResetStats}
                  className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-200 transition-colors"
                  title="Reset stats to 100%"
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Reset Stats</span>
                </button>
              </div>

              {/* Hunger Bar */}
              <div className="space-y-2 mb-4">
                <div className="flex justify-between text-sm font-medium">
                  <span className="flex items-center gap-1.5 text-amber-400">
                    <Coffee className="w-4 h-4" />
                    Hunger (Degrades via HungerWorker)
                  </span>
                  <span className="text-slate-300">{stats.hunger} / 100</span>
                </div>
                <div className="w-full bg-slate-900 h-3 rounded-full overflow-hidden p-0.5 border border-slate-700">
                  <div
                    style={{ width: `${stats.hunger}%` }}
                    className={`h-full rounded-full transition-all duration-300 ${
                      stats.hunger > 50
                        ? 'bg-amber-500'
                        : stats.hunger > 20
                        ? 'bg-amber-600'
                        : 'bg-rose-600'
                    }`}
                  />
                </div>
              </div>

              {/* Happiness Bar */}
              <div className="space-y-2 mb-4">
                <div className="flex justify-between text-sm font-medium">
                  <span className="flex items-center gap-1.5 text-pink-400">
                    <Heart className="w-4 h-4" />
                    Happiness
                  </span>
                  <span className="text-slate-300">{stats.happiness} / 100</span>
                </div>
                <div className="w-full bg-slate-900 h-3 rounded-full overflow-hidden p-0.5 border border-slate-700">
                  <div
                    style={{ width: `${stats.happiness}%` }}
                    className="h-full rounded-full bg-pink-500 transition-all duration-300"
                  />
                </div>
              </div>

              {/* Energy Bar */}
              <div className="space-y-2 mb-6">
                <div className="flex justify-between text-sm font-medium">
                  <span className="flex items-center gap-1.5 text-sky-400">
                    <Zap className="w-4 h-4" />
                    Energy
                  </span>
                  <span className="text-slate-300">{stats.energy} / 100</span>
                </div>
                <div className="w-full bg-slate-900 h-3 rounded-full overflow-hidden p-0.5 border border-slate-700">
                  <div
                    style={{ width: `${stats.energy}%` }}
                    className="h-full rounded-full bg-sky-500 transition-all duration-300"
                  />
                </div>
              </div>
            </div>

            {/* Quick Pet Care Actions */}
            <div className="pt-4 border-t border-slate-700/60">
              <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                Interact with Slime
              </div>
              <div className="grid grid-cols-3 gap-3">
                <button
                  onClick={handleFeed}
                  className="flex flex-col items-center justify-center p-3 rounded-2xl bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-amber-300 transition-all active:scale-95"
                >
                  <Coffee className="w-5 h-5 mb-1" />
                  <span className="text-xs font-semibold">Feed</span>
                  <span className="text-[10px] text-amber-400/80">+20 Hunger</span>
                </button>

                <button
                  onClick={handlePet}
                  className="flex flex-col items-center justify-center p-3 rounded-2xl bg-pink-500/10 hover:bg-pink-500/20 border border-pink-500/30 text-pink-300 transition-all active:scale-95"
                >
                  <Heart className="w-5 h-5 mb-1" />
                  <span className="text-xs font-semibold">Pet</span>
                  <span className="text-[10px] text-pink-400/80">+15 Happy</span>
                </button>

                <button
                  onClick={handleNap}
                  className="flex flex-col items-center justify-center p-3 rounded-2xl bg-sky-500/10 hover:bg-sky-500/20 border border-sky-500/30 text-sky-300 transition-all active:scale-95"
                >
                  <Zap className="w-5 h-5 mb-1" />
                  <span className="text-xs font-semibold">Nap</span>
                  <span className="text-[10px] text-sky-400/80">+35 Energy</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Screen Sight & Gestures (ScreenshotHelper & FloatingService Gestures) */}
      {activeTab === 'vision' && (
        <div className="bg-slate-800/60 rounded-3xl p-6 border border-slate-700/60 shadow-lg space-y-6">
          <div className="flex items-center gap-2">
            <Eye className="w-5 h-5 text-emerald-400" />
            <h2 className="text-lg font-semibold text-white">Floating Slime Gestures & Screen Perception</h2>
          </div>

          <p className="text-sm text-slate-300 leading-relaxed">
            In the Android version, the Slime hovered above other apps via a foreground service with gestures registered via <code className="text-sky-300">GestureDetector</code> and screen observation via <code className="text-sky-300">ScreenshotHelper</code>.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-slate-900/80 border border-slate-700 rounded-2xl p-4 flex flex-col justify-between">
              <div>
                <div className="w-8 h-8 rounded-lg bg-sky-500/20 text-sky-400 flex items-center justify-center font-bold text-sm mb-3">
                  1
                </div>
                <h4 className="text-sm font-semibold text-white mb-1">Single Tap / Click</h4>
                <p className="text-xs text-slate-400">
                  Triggers <code className="text-sky-300">slimeTapTalk()</code>. Slime ponders and gives a cute, playful reply.
                </p>
              </div>
              <div className="mt-3 text-xs text-sky-400 font-mono">Gesture: Tap</div>
            </div>

            <div className="bg-slate-900/80 border border-slate-700 rounded-2xl p-4 flex flex-col justify-between">
              <div>
                <div className="w-8 h-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-sm mb-3">
                  2
                </div>
                <h4 className="text-sm font-semibold text-white mb-1">Double Tap / Double Click</h4>
                <p className="text-xs text-slate-400">
                  Triggers <code className="text-emerald-300">slimeAskWhatYouSee()</code>. Slime observes your active screen and comments on it.
                </p>
              </div>
              <div className="mt-3 text-xs text-emerald-400 font-mono">Gesture: Double Tap</div>
            </div>

            <div className="bg-slate-900/80 border border-slate-700 rounded-2xl p-4 flex flex-col justify-between">
              <div>
                <div className="w-8 h-8 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold text-sm mb-3">
                  3
                </div>
                <h4 className="text-sm font-semibold text-white mb-1">Long Press / Hold</h4>
                <p className="text-xs text-slate-400">
                  Triggers <code className="text-amber-300">slimeFeed()</code>. Slime eagerly eats ("Nom! +20 hunger") with animated chomping.
                </p>
              </div>
              <div className="mt-3 text-xs text-amber-400 font-mono">Gesture: Hold / 600ms</div>
            </div>
          </div>

          <div className="bg-sky-950/40 border border-sky-500/20 rounded-2xl p-4 flex items-start gap-3">
            <Info className="w-5 h-5 text-sky-400 shrink-0 mt-0.5" />
            <div className="text-xs text-slate-300 space-y-1">
              <p className="font-semibold text-sky-200">Interactive Floating Physics</p>
              <p>
                You can drag the Slime anywhere across the entire browser viewport. When released, it smoothly snaps to the nearest margin or stays in place just like the Android WindowManager overlay.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
