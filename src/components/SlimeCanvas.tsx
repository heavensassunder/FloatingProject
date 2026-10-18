import React, { useEffect, useRef } from 'react';
import { SlimeMood } from '../types';

interface SlimeCanvasProps {
  mood: SlimeMood;
  size?: number;
  bounceOffset?: number;
  isEating?: boolean;
}

export const SlimeCanvas: React.FC<SlimeCanvasProps> = ({
  mood,
  size = 180,
  bounceOffset = 0,
  isEating = false,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Use device pixel ratio for super-crisp rendering
    const dpr = window.devicePixelRatio || 1;
    canvas.width = size * dpr;
    canvas.height = size * dpr;
    ctx.scale(dpr, dpr);

    ctx.clearRect(0, 0, size, size);

    const w = size;
    const h = size;
    const cx = w / 2;
    const cy = h / 2 + bounceOffset;
    const r = Math.min(w, h) * 0.45;

    // 1. Draw Body with RadialGradient (#4FC3F7 -> #0288D1)
    const gradX = cx - r * 0.2;
    const gradY = cy - r * 0.3;
    const gradient = ctx.createRadialGradient(gradX, gradY, 0, gradX, gradY, r * 1.1);
    gradient.addColorStop(0, '#4FC3F7');
    gradient.addColorStop(1, '#0288D1');

    ctx.save();
    ctx.beginPath();
    // Cute squishy slime shape: slightly wider at bottom
    ctx.ellipse(cx, cy, r, r * 0.95, 0, 0, Math.PI * 2);
    ctx.fillStyle = gradient;
    ctx.shadowColor = 'rgba(2, 136, 209, 0.4)';
    ctx.shadowBlur = 14;
    ctx.shadowOffsetY = 6;
    ctx.fill();
    ctx.restore();

    // 2. Body Shine Highlight
    ctx.save();
    ctx.beginPath();
    const shineX = cx - r * 0.4;
    const shineY = cy - r * 0.55;
    const shineRadiusX = r * 0.22;
    const shineRadiusY = r * 0.15;
    ctx.ellipse(shineX, shineY, shineRadiusX, shineRadiusY, -Math.PI / 6, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255, 255, 255, 0.45)';
    ctx.fill();
    ctx.restore();

    // Secondary smaller shine dot
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx - r * 0.52, cy - r * 0.38, r * 0.06, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255, 255, 255, 0.35)';
    ctx.fill();
    ctx.restore();

    // 3. Eyes
    const eyeOffsetX = r * 0.38;
    const eyeOffsetY = r * -0.08;
    const baseEyeR = r * 0.14;

    const drawEye = (eyeX: number, eyeY: number, eyeRadius: number) => {
      // White sclera
      ctx.beginPath();
      ctx.arc(eyeX, eyeY, eyeRadius, 0, Math.PI * 2);
      ctx.fillStyle = '#FFFFFF';
      ctx.fill();

      // Black pupil
      ctx.beginPath();
      ctx.arc(eyeX, eyeY, eyeRadius * 0.55, 0, Math.PI * 2);
      ctx.fillStyle = '#111827';
      ctx.fill();

      // Specular highlight in pupil
      ctx.beginPath();
      ctx.arc(
        eyeX - eyeRadius * 0.22,
        eyeY - eyeRadius * 0.22,
        eyeRadius * 0.18,
        0,
        Math.PI * 2
      );
      ctx.fillStyle = '#FFFFFF';
      ctx.fill();
    };

    if (mood === 'SLEEPY') {
      // Sleepy curved lines (closed eyes)
      ctx.save();
      ctx.strokeStyle = '#1E293B';
      ctx.lineWidth = Math.max(3, r * 0.05);
      ctx.lineCap = 'round';

      // Left eye arc
      ctx.beginPath();
      ctx.arc(cx - eyeOffsetX, cy + eyeOffsetY + 2, baseEyeR, Math.PI * 1.1, Math.PI * 1.9, false);
      ctx.stroke();

      // Right eye arc
      ctx.beginPath();
      ctx.arc(cx + eyeOffsetX, cy + eyeOffsetY + 2, baseEyeR, Math.PI * 1.1, Math.PI * 1.9, false);
      ctx.stroke();
      ctx.restore();
    } else if (mood === 'HAPPY' || isEating) {
      // Bigger animated happy eyes
      drawEye(cx - eyeOffsetX, cy + eyeOffsetY, baseEyeR * 1.15);
      drawEye(cx + eyeOffsetX, cy + eyeOffsetY, baseEyeR * 1.15);

      // Cute pink blush on cheeks!
      ctx.save();
      ctx.fillStyle = 'rgba(255, 130, 160, 0.45)';
      ctx.beginPath();
      ctx.ellipse(cx - eyeOffsetX - 4, cy + eyeOffsetY + 16, r * 0.14, r * 0.08, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.beginPath();
      ctx.ellipse(cx + eyeOffsetX + 4, cy + eyeOffsetY + 16, r * 0.14, r * 0.08, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    } else if (mood === 'SAD') {
      // Drooping smaller eyes shifted down
      drawEye(cx - eyeOffsetX, cy + eyeOffsetY + r * 0.06, baseEyeR * 0.85);
      drawEye(cx + eyeOffsetX, cy + eyeOffsetY + r * 0.06, baseEyeR * 0.85);
    } else {
      // NEUTRAL
      drawEye(cx - eyeOffsetX, cy + eyeOffsetY, baseEyeR);
      drawEye(cx + eyeOffsetX, cy + eyeOffsetY, baseEyeR);
    }

    // 4. Smile / Mouth
    ctx.save();
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = Math.max(3.5, r * 0.065);
    ctx.lineCap = 'round';

    const mouthY = cy + r * 0.22;

    if (isEating) {
      // Open mouth eating animation!
      ctx.beginPath();
      ctx.arc(cx, mouthY, r * 0.15, 0, Math.PI, false);
      ctx.fillStyle = '#C2185B';
      ctx.fill();
      ctx.stroke();
    } else if (mood === 'SAD') {
      // Frown arc
      ctx.beginPath();
      ctx.arc(cx, mouthY + 10, r * 0.2, Math.PI * 1.25, Math.PI * 1.75, false);
      ctx.stroke();
    } else if (mood === 'SLEEPY') {
      // Small relaxed flat mouth
      ctx.beginPath();
      ctx.moveTo(cx - r * 0.1, mouthY);
      ctx.lineTo(cx + r * 0.1, mouthY);
      ctx.stroke();
    } else if (mood === 'HAPPY') {
      // Big open smile
      ctx.beginPath();
      ctx.arc(cx, mouthY - 4, r * 0.24, Math.PI * 0.15, Math.PI * 0.85, false);
      ctx.stroke();
    } else {
      // Neutral gentle smile
      ctx.beginPath();
      ctx.arc(cx, mouthY - 2, r * 0.2, Math.PI * 0.2, Math.PI * 0.8, false);
      ctx.stroke();
    }
    ctx.restore();
  }, [mood, size, bounceOffset, isEating]);

  return (
    <canvas
      ref={canvasRef}
      style={{ width: size, height: size }}
      className="pointer-events-none drop-shadow-lg transition-transform duration-150"
    />
  );
};
