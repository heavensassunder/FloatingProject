import { slimeStore } from './slimeStore';
import { copyWithHungerDelta } from '../types';

class HungerWorkerManager {
  private timerId: number | null = null;
  private intervalMs: number = 30000; // 30 seconds

  start(): void {
    if (this.timerId !== null) return;
    this.timerId = window.setInterval(() => {
      this.tick();
    }, this.intervalMs);
  }

  stop(): void {
    if (this.timerId !== null) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  tick(): void {
    const current = slimeStore.getStats();
    // Degrade hunger by 1 (like Android HungerWorker)
    const updated = copyWithHungerDelta(current, -1);
    slimeStore.saveStats(updated);
  }
}

export const hungerWorker = new HungerWorkerManager();
