import { DEFAULT_STATS, SlimeStats } from '../types';

const KEY_API = 'openrouter_api_key';
const KEY_STATS = 'slime_stats_json';
const KEY_FLOATING_ENABLED = 'slime_floating_enabled';

type Listener<T> = (value: T) => void;

class SlimeStore {
  private apiListeners: Set<Listener<string>> = new Set();
  private statsListeners: Set<Listener<SlimeStats>> = new Set();
  private floatingListeners: Set<Listener<boolean>> = new Set();

  getApiKey(): string {
    return localStorage.getItem(KEY_API) || '';
  }

  saveApiKey(key: string): void {
    localStorage.setItem(KEY_API, key);
    this.notifyApiListeners(key);
  }

  subscribeApiKey(listener: Listener<string>): () => void {
    this.apiListeners.add(listener);
    listener(this.getApiKey());
    return () => this.apiListeners.delete(listener);
  }

  private notifyApiListeners(key: string): void {
    for (const listener of this.apiListeners) {
      listener(key);
    }
  }

  getStats(): SlimeStats {
    const raw = localStorage.getItem(KEY_STATS);
    if (!raw) return { ...DEFAULT_STATS };
    try {
      const parsed = JSON.parse(raw);
      return {
        hunger: typeof parsed.hunger === 'number' ? parsed.hunger : 100,
        happiness: typeof parsed.happiness === 'number' ? parsed.happiness : 100,
        energy: typeof parsed.energy === 'number' ? parsed.energy : 100,
        level: typeof parsed.level === 'number' ? parsed.level : 1,
      };
    } catch {
      return { ...DEFAULT_STATS };
    }
  }

  saveStats(stats: SlimeStats): void {
    localStorage.setItem(KEY_STATS, JSON.stringify(stats));
    this.notifyStatsListeners(stats);
  }

  subscribeStats(listener: Listener<SlimeStats>): () => void {
    this.statsListeners.add(listener);
    listener(this.getStats());
    return () => this.statsListeners.delete(listener);
  }

  private notifyStatsListeners(stats: SlimeStats): void {
    for (const listener of this.statsListeners) {
      listener(stats);
    }
  }

  isFloatingEnabled(): boolean {
    const raw = localStorage.getItem(KEY_FLOATING_ENABLED);
    return raw !== null ? raw === 'true' : true;
  }

  setFloatingEnabled(enabled: boolean): void {
    localStorage.setItem(KEY_FLOATING_ENABLED, enabled ? 'true' : 'false');
    for (const listener of this.floatingListeners) {
      listener(enabled);
    }
  }

  subscribeFloating(listener: Listener<boolean>): () => void {
    this.floatingListeners.add(listener);
    listener(this.isFloatingEnabled());
    return () => this.floatingListeners.delete(listener);
  }
}

export const slimeStore = new SlimeStore();
