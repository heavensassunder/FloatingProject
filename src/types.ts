export type SlimeMood = 'HAPPY' | 'SAD' | 'SLEEPY' | 'NEUTRAL';

export interface SlimeStats {
  hunger: number;    // 0..100 (100 = full)
  happiness: number; // 0..100
  energy: number;    // 0..100
  level: number;
}

export const DEFAULT_STATS: SlimeStats = {
  hunger: 100,
  happiness: 100,
  energy: 100,
  level: 1,
};

export function copyWithHungerDelta(stats: SlimeStats, delta: number): SlimeStats {
  const newHunger = Math.max(0, Math.min(100, stats.hunger + delta));
  // If hunger drops too low, happiness also drops slightly
  const happinessDelta = newHunger < 30 ? -1 : 0;
  const newHappiness = Math.max(0, Math.min(100, stats.happiness + happinessDelta));
  return {
    ...stats,
    hunger: newHunger,
    happiness: newHappiness,
  };
}

export function calculateMood(stats: SlimeStats): SlimeMood {
  if (stats.energy < 25) {
    return 'SLEEPY';
  }
  if (stats.hunger < 35 || stats.happiness < 35) {
    return 'SAD';
  }
  if (stats.hunger > 60 && stats.happiness > 60) {
    return 'HAPPY';
  }
  return 'NEUTRAL';
}
