import React, { useState, useEffect } from 'react';
import { SlimeStats, DEFAULT_STATS, copyWithHungerDelta } from './types';
import { slimeStore } from './services/slimeStore';
import { hungerWorker } from './services/hungerWorker';
import { MainSettingsView } from './components/MainSettingsView';
import { FloatingSlimeOverlay } from './components/FloatingSlimeOverlay';

export const App: React.FC = () => {
  const [stats, setStats] = useState<SlimeStats>(slimeStore.getStats() || DEFAULT_STATS);
  const [floatingEnabled, setFloatingEnabled] = useState<boolean>(slimeStore.isFloatingEnabled());

  useEffect(() => {
    // Start background HungerWorker (drops hunger over time like Android WorkManager)
    hungerWorker.start();

    // Subscribe to stats changes
    const unsubStats = slimeStore.subscribeStats((newStats) => {
      setStats(newStats);
    });

    // Subscribe to floating enabled status
    const unsubFloating = slimeStore.subscribeFloating((enabled) => {
      setFloatingEnabled(enabled);
    });

    return () => {
      hungerWorker.stop();
      unsubStats();
      unsubFloating();
    };
  }, []);

  const handleUpdateStats = (newStats: SlimeStats) => {
    slimeStore.saveStats(newStats);
  };

  const handleFeed = () => {
    const updated = copyWithHungerDelta(stats, 20);
    const withHappiness = {
      ...updated,
      happiness: Math.min(100, updated.happiness + 5),
    };
    slimeStore.saveStats(withHappiness);
  };

  const handleToggleFloating = (enabled: boolean) => {
    slimeStore.setFloatingEnabled(enabled);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 relative overflow-hidden flex flex-col justify-between">
      {/* Background ambient gradient */}
      <div className="fixed inset-0 pointer-events-none opacity-25">
        <div className="absolute -top-40 -left-40 w-96 h-96 bg-sky-500 rounded-full blur-3xl" />
        <div className="absolute bottom-10 right-10 w-96 h-96 bg-blue-600 rounded-full blur-3xl" />
      </div>

      {/* Main Settings & Controls View */}
      <main className="relative z-10 flex-1">
        <MainSettingsView
          stats={stats}
          onUpdateStats={handleUpdateStats}
          floatingEnabled={floatingEnabled}
          onToggleFloating={handleToggleFloating}
        />
      </main>

      {/* Live Floating Slime Overlay (Window / Screen Pet) */}
      {floatingEnabled && (
        <FloatingSlimeOverlay
          stats={stats}
          onFeed={handleFeed}
          onStatsChange={handleUpdateStats}
        />
      )}

      {/* Footer */}
      <footer className="relative z-10 text-center py-4 border-t border-slate-800/80 text-xs text-slate-500">
        Floating Slime Pet &bull; Rewritten from Android to React
      </footer>
    </div>
  );
};

export default App;
