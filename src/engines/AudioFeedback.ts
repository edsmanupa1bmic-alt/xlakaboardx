import { SoundEffectType } from '../types';

class AudioFeedbackEngine {
  private audioCtx: AudioContext | null = null;

  private getAudioContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.audioCtx) {
      const AudioCtxClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (AudioCtxClass) {
        this.audioCtx = new AudioCtxClass();
      }
    }
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
    return this.audioCtx;
  }

  playKeypressSound(type: SoundEffectType, volume: number = 50) {
    if (type === 'none' || volume <= 0) return;

    try {
      const ctx = this.getAudioContext();
      if (!ctx) return;

      const normalizedVol = Math.max(0.01, Math.min(1.0, (volume / 100) * 0.4));
      const now = ctx.currentTime;

      if (type === 'system') {
        // High-pitch crisp subtle mechanical click
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(1200, now);
        osc.frequency.exponentialRampToValueAtTime(300, now + 0.03);

        gain.gain.setValueAtTime(normalizedVol, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.03);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(now);
        osc.stop(now + 0.035);
      } else if (type === 'modern') {
        // Modern soft bubble pop / tick
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(650, now);
        osc.frequency.exponentialRampToValueAtTime(180, now + 0.04);

        gain.gain.setValueAtTime(normalizedVol * 1.2, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.04);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(now);
        osc.stop(now + 0.045);
      } else if (type === 'typewriter') {
        // Vintage typewriter mechanical tap
        const bufferSize = ctx.sampleRate * 0.025;
        const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
        const data = buffer.getChannelData(0);
        for (let i = 0; i < bufferSize; i++) {
          data[i] = Math.random() * 2 - 1;
        }

        const noise = ctx.createBufferSource();
        noise.buffer = buffer;

        const filter = ctx.createBiquadFilter();
        filter.type = 'bandpass';
        filter.frequency.value = 1800;
        filter.Q.value = 3;

        const gain = ctx.createGain();
        gain.gain.setValueAtTime(normalizedVol * 1.5, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.025);

        noise.connect(filter);
        filter.connect(gain);
        gain.connect(ctx.destination);

        noise.start(now);
      } else if (type === 'soft') {
        // Deep muted thud
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(220, now);
        osc.frequency.exponentialRampToValueAtTime(80, now + 0.05);

        gain.gain.setValueAtTime(normalizedVol * 0.8, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.05);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(now);
        osc.stop(now + 0.055);
      }
    } catch {
      // Audio play error or browser policy
    }
  }

  triggerHaptic(durationMs: number = 20) {
    if (typeof window !== 'undefined' && 'vibrate' in navigator) {
      try {
        navigator.vibrate(durationMs);
      } catch {
        // Ignore vibration errors
      }
    }
  }
}

export const AudioFeedback = new AudioFeedbackEngine();
