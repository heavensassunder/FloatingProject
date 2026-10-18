import React, { useState, useEffect, useRef, useCallback } from 'react';
import { SlimeCanvas } from './SlimeCanvas';
import { ChatBubble } from './ChatBubble';
import { SlimeMood, SlimeStats, calculateMood } from '../types';
import { slimeStore } from '../services/slimeStore';
import { OpenRouterClient } from '../services/openRouter';
import { Sparkles, Utensils, MessageCircle, Eye, Move } from 'lucide-react';

interface FloatingSlimeOverlayProps {
  stats: SlimeStats;
  onFeed: () => void;
  onStatsChange: (newStats: SlimeStats) => void;
  onOpenSettings?: () => void;
}

export const FloatingSlimeOverlay: React.FC<FloatingSlimeOverlayProps> = ({
  stats,
  onFeed,
  onStatsChange,
  onOpenSettings,
}) => {
  // Coordinates of the floating slime
  const [position, setPosition] = useState({ x: 40, y: 140 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [bubbleText, setBubbleText] = useState<string | null>(null);
  const [isThinking, setIsThinking] = useState(false);
  const [isError, setIsError] = useState(false);
  const [isEating, setIsEating] = useState(false);
  const [bounce, setBounce] = useState(0);

  // Gesture handling refs
  const dragStartPosRef = useRef({ x: 0, y: 0 });
  const dragStartTimeRef = useRef(0);
  const longPressTimerRef = useRef<number | null>(null);
  const lastTapTimeRef = useRef(0);
  const isDraggingActiveRef = useRef(false);

  const mood: SlimeMood = calculateMood(stats);

  // Load initial position or center-right on mount
  useEffect(() => {
    const savedPos = localStorage.getItem('slime_overlay_pos');
    if (savedPos) {
      try {
        const parsed = JSON.parse(savedPos);
        if (typeof parsed.x === 'number' && typeof parsed.y === 'number') {
          setPosition(parsed);
          return;
        }
      } catch {}
    }
    // Default initial position near right side
    const initialX = Math.max(20, window.innerWidth - 220);
    const initialY = 160;
    setPosition({ x: initialX, y: initialY });
  }, []);

  const triggerBounce = useCallback(() => {
    setBounce(12);
    setTimeout(() => setBounce(-8), 120);
    setTimeout(() => setBounce(4), 240);
    setTimeout(() => setBounce(0), 360);
  }, []);

  // Action: Slime Tap Talk (Single Tap)
  const slimeTapTalk = useCallback(async () => {
    triggerBounce();
    setBubbleText('Thinking...');
    setIsThinking(true);
    setIsError(false);

    const apiKey = slimeStore.getApiKey();
    if (!apiKey) {
      setIsThinking(false);
      setBubbleText('No API key. Open app to set it.');
      return;
    }

    try {
      const client = new OpenRouterClient(apiKey);
      const sys = 'You are Slime, a cute floating buddy on user\'s phone or screen. Short playful replies (max 2 sentences).';
      const reply = await client.chat(sys, 'Hello slime! Say something playful.');
      setBubbleText(reply);
      setIsThinking(false);
      // Small happiness boost
      onStatsChange({
        ...stats,
        happiness: Math.min(100, stats.happiness + 4),
      });
    } catch (err: any) {
      setIsThinking(false);
      setIsError(true);
      setBubbleText(`AI error: ${err.message || 'Failed to connect'}`);
    }
  }, [stats, onStatsChange, triggerBounce]);

  // Action: Slime Ask What You See (Double Tap)
  const slimeAskWhatYouSee = useCallback(async () => {
    triggerBounce();
    setBubbleText('Looking...');
    setIsThinking(true);
    setIsError(false);

    const apiKey = slimeStore.getApiKey();
    if (!apiKey) {
      setIsThinking(false);
      setBubbleText('No API key');
      return;
    }

    try {
      const client = new OpenRouterClient(apiKey);
      const sys = 'You are Slime, a cute floating buddy. Short description of the screen when user asks to describe the screen.';
      const reply = await client.chat(sys, 'Describe what you think is on my screen (text-only).');
      setBubbleText(reply);
      setIsThinking(false);
    } catch (err: any) {
      setIsThinking(false);
      setIsError(true);
      setBubbleText(`AI error: ${err.message || 'Failed to inspect screen'}`);
    }
  }, [triggerBounce]);

  // Action: Slime Feed (Long Press / Feed action)
  const slimeFeed = useCallback(() => {
    setIsEating(true);
    triggerBounce();
    setBubbleText('Nom! +20 hunger');
    setIsThinking(false);
    setIsError(false);

    onFeed();

    setTimeout(() => {
      setIsEating(false);
    }, 1200);
  }, [onFeed, triggerBounce]);

  // Pointer Down handler
  const handlePointerDown = (e: React.PointerEvent) => {
    // Only left click or touch
    if (e.button !== 0 && e.pointerType === 'mouse') return;

    const startX = e.clientX;
    const startY = e.clientY;
    dragStartPosRef.current = { x: startX, y: startY };
    dragStartTimeRef.current = Date.now();
    isDraggingActiveRef.current = false;

    setDragOffset({
      x: startX - position.x,
      y: startY - position.y,
    });

    // Start Long Press Detection (600ms)
    longPressTimerRef.current = window.setTimeout(() => {
      if (!isDraggingActiveRef.current) {
        // Trigger Long Press Feed
        slimeFeed();
      }
    }, 600);

    const target = e.currentTarget as HTMLElement;
    target.setPointerCapture(e.pointerId);
  };

  // Pointer Move handler
  const handlePointerMove = (e: React.PointerEvent) => {
    if (dragStartTimeRef.current === 0) return;

    const dx = e.clientX - dragStartPosRef.current.x;
    const dy = e.clientY - dragStartPosRef.current.y;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist > 8) {
      // User is actively dragging
      isDraggingActiveRef.current = true;
      setIsDragging(true);

      // Cancel long press
      if (longPressTimerRef.current) {
        clearTimeout(longPressTimerRef.current);
        longPressTimerRef.current = null;
      }

      // Constrain position to screen boundaries
      const slimeSize = 160;
      const newX = Math.max(0, Math.min(window.innerWidth - slimeSize, e.clientX - dragOffset.x));
      const newY = Math.max(0, Math.min(window.innerHeight - slimeSize, e.clientY - dragOffset.y));

      setPosition({ x: newX, y: newY });
    }
  };

  // Pointer Up handler (Touch Up / Snap logic matching Android FloatingService)
  const handlePointerUp = (e: React.PointerEvent) => {
    if (longPressTimerRef.current) {
      clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }

    const wasDragging = isDraggingActiveRef.current;
    dragStartTimeRef.current = 0;
    isDraggingActiveRef.current = false;
    setIsDragging(false);

    try {
      const target = e.currentTarget as HTMLElement;
      target.releasePointerCapture(e.pointerId);
    } catch {}

    if (wasDragging) {
      // Snap to edge like Android FloatingService:
      // it.x = if (it.x < displayWidth / 2) 0 else displayWidth - slimeSize
      const slimeSize = 160;
      const displayWidth = window.innerWidth;
      const snapMargin = 16;
      const snappedX =
        position.x < displayWidth / 2
          ? snapMargin
          : displayWidth - slimeSize - snapMargin;

      const finalPos = { x: snappedX, y: position.y };
      setPosition(finalPos);
      localStorage.setItem('slime_overlay_pos', JSON.stringify(finalPos));
      return;
    }

    // Tap logic (Double tap vs Single tap)
    const now = Date.now();
    const timeSinceLastTap = now - lastTapTimeRef.current;

    if (timeSinceLastTap < 350) {
      // Double Tap detected -> slimeAskWhatYouSee()
      lastTapTimeRef.current = 0;
      slimeAskWhatYouSee();
    } else {
      // Potential Single Tap -> wait briefly to disambiguate from double tap
      lastTapTimeRef.current = now;
      setTimeout(() => {
        if (lastTapTimeRef.current === now) {
          // Single Tap confirmed -> slimeTapTalk()
          slimeTapTalk();
        }
      }, 350);
    }
  };

  return (
    <div
      style={{
        transform: `translate3d(${position.x}px, ${position.y}px, 0)`,
        transition: isDragging ? 'none' : 'transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1)',
      }}
      className="fixed top-0 left-0 z-40 touch-none select-none"
    >
      <div className="relative group flex flex-col items-center">
        {/* Chat Speech Bubble */}
        <ChatBubble
          text={bubbleText}
          isThinking={isThinking}
          isError={isError}
          onDismiss={() => setBubbleText(null)}
        />

        {/* Slime interactive avatar */}
        <div
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          className={`cursor-grab active:cursor-grabbing p-2 transition-transform duration-100 ${
            isDragging ? 'scale-105' : 'hover:scale-105'
          } ${isEating ? 'animate-slime-eat' : ''}`}
          title="Slime Pet: Tap to Talk | Double-Tap to Look | Hold to Feed | Drag to Move"
        >
          <div className="relative">
            <SlimeCanvas
              mood={mood}
              size={150}
              bounceOffset={bounce}
              isEating={isEating}
            />

            {/* Drag hint handle badge */}
            <div className="absolute -top-1 -right-1 bg-sky-500/80 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity shadow-md">
              <Move className="w-3.5 h-3.5" />
            </div>
          </div>
        </div>

        {/* Quick action pill toolbar on hover */}
        <div className="flex items-center gap-1.5 bg-slate-900/90 backdrop-blur-md border border-slate-700/60 rounded-full px-2 py-1 shadow-xl opacity-0 group-hover:opacity-100 transition-opacity text-xs mt-1 pointer-events-auto">
          <button
            onClick={(e) => {
              e.stopPropagation();
              slimeTapTalk();
            }}
            className="flex items-center gap-1 px-2 py-0.5 rounded-full hover:bg-sky-500/20 text-sky-300 font-medium transition-colors"
            title="Single Tap: Chat with Slime"
          >
            <MessageCircle className="w-3 h-3" />
            <span>Talk</span>
          </button>

          <span className="text-slate-600">|</span>

          <button
            onClick={(e) => {
              e.stopPropagation();
              slimeAskWhatYouSee();
            }}
            className="flex items-center gap-1 px-2 py-0.5 rounded-full hover:bg-emerald-500/20 text-emerald-300 font-medium transition-colors"
            title="Double Tap: Ask what Slime sees"
          >
            <Eye className="w-3 h-3" />
            <span>Look</span>
          </button>

          <span className="text-slate-600">|</span>

          <button
            onClick={(e) => {
              e.stopPropagation();
              slimeFeed();
            }}
            className="flex items-center gap-1 px-2 py-0.5 rounded-full hover:bg-amber-500/20 text-amber-300 font-medium transition-colors"
            title="Hold/Long-press: Feed Slime (+20 Hunger)"
          >
            <Utensils className="w-3 h-3" />
            <span>Feed</span>
          </button>
        </div>
      </div>
    </div>
  );
};
