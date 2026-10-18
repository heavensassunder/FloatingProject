import React, { useEffect } from 'react';
import { X, Sparkles, AlertCircle } from 'lucide-react';

interface ChatBubbleProps {
  text: string | null;
  onDismiss: () => void;
  isThinking?: boolean;
  isError?: boolean;
}

export const ChatBubble: React.FC<ChatBubbleProps> = ({
  text,
  onDismiss,
  isThinking = false,
  isError = false,
}) => {
  useEffect(() => {
    if (!text) return;
    const timer = setTimeout(() => {
      onDismiss();
    }, 6000); // 6 seconds auto-dismiss

    return () => clearTimeout(timer);
  }, [text, onDismiss]);

  if (!text) return null;

  return (
    <div className="absolute bottom-[105%] left-1/2 -translate-x-1/2 z-50 min-w-[200px] max-w-[280px] pointer-events-auto transition-all duration-200 animate-in fade-in zoom-in-95">
      <div
        className={`relative px-4 py-3 rounded-2xl shadow-2xl backdrop-blur-md border ${
          isError
            ? 'bg-rose-950/90 border-rose-500/50 text-rose-100'
            : isThinking
            ? 'bg-sky-950/90 border-sky-400/50 text-sky-100'
            : 'bg-slate-900/95 border-sky-400/30 text-white'
        }`}
      >
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-sky-400 uppercase tracking-wider mb-1">
            {isThinking ? (
              <>
                <Sparkles className="w-3.5 h-3.5 animate-spin" />
                <span>Thinking...</span>
              </>
            ) : isError ? (
              <>
                <AlertCircle className="w-3.5 h-3.5 text-rose-400" />
                <span className="text-rose-400">Slime Note</span>
              </>
            ) : (
              <>
                <span>💧 Slime says</span>
              </>
            )}
          </div>
          <button
            onClick={onDismiss}
            aria-label="Dismiss message"
            className="text-slate-400 hover:text-white transition-colors p-0.5 rounded-full hover:bg-white/10"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>

        <p className="text-sm font-medium leading-relaxed whitespace-pre-wrap break-words text-slate-100">
          {text}
        </p>

        {/* Speech bubble beak / arrow pointing to the Slime below */}
        <div
          className={`absolute left-1/2 -translate-x-1/2 -bottom-2 w-0 h-0 border-x-8 border-x-transparent border-t-8 ${
            isError
              ? 'border-t-rose-950/90'
              : isThinking
              ? 'border-t-sky-950/90'
              : 'border-t-slate-900/95'
          }`}
        />
      </div>
    </div>
  );
};
