import React from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import {
  Smile,
  ClipboardList,
  Palette,
  Settings,
  HelpCircle,
  Volume2,
  Trash2,
  Globe,
  Sparkles,
  Calculator,
  Check
} from 'lucide-react';
import { SinglishParser } from '../engines/SinglishParser';

export const Smartbar: React.FC = () => {
  const {
    activeTheme,
    suggestions,
    inputBuffer,
    config,
    handleSuggestionSelect,
    clearEditor,
    setLayoutMode,
    setActiveModal,
    updateConfig,
    editorText,
    insertTextDirectly
  } = useKeyboard();

  const isSinglish = config.keyboardLanguage === 'sinhala_singlish';
  const isWijesekara = config.keyboardLanguage === 'sinhala_wijesekara';

  const toggleLanguage = () => {
    if (config.keyboardLanguage === 'sinhala_singlish') {
      updateConfig({ keyboardLanguage: 'sinhala_wijesekara' });
    } else if (config.keyboardLanguage === 'sinhala_wijesekara') {
      updateConfig({ keyboardLanguage: 'english_qwerty' });
    } else {
      updateConfig({ keyboardLanguage: 'sinhala_singlish' });
    }
  };

  const speakCurrentText = () => {
    if ('speechSynthesis' in window && (editorText || inputBuffer)) {
      const textToSpeak = inputBuffer ? (isSinglish ? SinglishParser.parse(inputBuffer) : inputBuffer) : editorText;
      const utterance = new SpeechSynthesisUtterance(textToSpeak);
      // If Sinhala, set lang or default
      if (isSinglish || isWijesekara) {
        utterance.lang = 'si-LK';
      } else {
        utterance.lang = 'en-US';
      }
      window.speechSynthesis.speak(utterance);
    }
  };

  return (
    <div
      className="w-full flex items-center justify-between px-2 py-1 select-none border-b overflow-x-auto no-scrollbar transition-colors"
      style={{
        backgroundColor: activeTheme.smartbarBgColor || '#18181B',
        color: activeTheme.smartbarTextColor || '#FAFAFA',
        borderColor: activeTheme.strokeColor,
        minHeight: '44px'
      }}
    >
      {/* Suggestions view when buffer exists */}
      {inputBuffer ? (
        <div className="flex items-center gap-1.5 w-full overflow-x-auto py-0.5 no-scrollbar">
          {suggestions.length > 0 ? (
            suggestions.map((sug, idx) => {
              const isFirst = idx === 0 || sug.isPrimary;
              return (
                <button
                  key={`${sug.text}-${idx}`}
                  onClick={() => handleSuggestionSelect(sug)}
                  className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-all shrink-0 cursor-pointer shadow-sm active:scale-95 ${
                    isFirst
                      ? 'ring-1 ring-white/20 font-bold'
                      : 'hover:bg-white/10'
                  }`}
                  style={{
                    backgroundColor: isFirst
                      ? activeTheme.accentKeyBgColor
                      : activeTheme.keyBgColor,
                    color: isFirst
                      ? activeTheme.accentKeyTextColor
                      : activeTheme.keyTextColor,
                    border: `1px solid ${activeTheme.strokeColor}`,
                    fontFamily: sug.isSinhala ? "'Noto Sans Sinhala', sans-serif" : 'inherit'
                  }}
                >
                  {sug.isCalculation ? (
                    <Calculator className="w-3.5 h-3.5 opacity-80" />
                  ) : sug.isCorrection ? (
                    <Check className="w-3.5 h-3.5 opacity-80 text-emerald-400" />
                  ) : isFirst ? (
                    <Sparkles className="w-3 h-3 opacity-75" />
                  ) : null}
                  <span>{sug.text}</span>
                </button>
              );
            })
          ) : (
            // Live Singlish Transliteration preview fallback
            <div className="flex items-center gap-2 text-xs opacity-75 px-2">
              <span className="font-mono text-zinc-400">{inputBuffer}</span>
              <span>➔</span>
              <span className="font-medium text-emerald-400 font-sinhala">
                {isSinglish ? SinglishParser.parse(inputBuffer) : inputBuffer}
              </span>
            </div>
          )}
        </div>
      ) : (
        /* Default Action Toolbar when idle */
        <div className="flex items-center justify-between w-full">
          {/* Left Actions: Language and Quick Tools */}
          <div className="flex items-center gap-1 sm:gap-2">
            {/* Language toggle chip */}
            <button
              onClick={toggleLanguage}
              className="flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold hover:bg-white/10 transition-colors cursor-pointer border shadow-xs"
              style={{
                borderColor: activeTheme.strokeColor,
                backgroundColor: activeTheme.keyBgColor,
                color: activeTheme.keyTextColor
              }}
              title="Switch Keyboard Language"
            >
              <Globe className="w-3.5 h-3.5" />
              <span>
                {config.keyboardLanguage === 'sinhala_singlish'
                  ? 'සිංහල (Singlish)'
                  : config.keyboardLanguage === 'sinhala_wijesekara'
                  ? 'සිංහල (Wijesekara)'
                  : 'English (US)'}
              </span>
            </button>

            {/* Singlish Guide Helper */}
            {isSinglish && (
              <button
                onClick={() => setActiveModal('singlish_guide')}
                className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
                title="Singlish Typing Guide & Rules"
              >
                <HelpCircle className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Center / Right Shortcuts */}
          <div className="flex items-center gap-1 sm:gap-1.5">
            {/* Emoji Panel */}
            <button
              onClick={() => setLayoutMode('emoji')}
              className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
              title="Emoji Picker"
            >
              <Smile className="w-4 h-4" />
            </button>

            {/* Clips / Clipboard */}
            <button
              onClick={() => setLayoutMode('clips')}
              className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
              title="Clipboard Items (Clips)"
            >
              <ClipboardList className="w-4 h-4" />
            </button>

            {/* Theme Customizer */}
            <button
              onClick={() => setActiveModal('themes')}
              className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
              title="Theme Customizer"
            >
              <Palette className="w-4 h-4" />
            </button>

            {/* Text to speech / speak */}
            <button
              onClick={speakCurrentText}
              className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
              title="Speak text aloud"
            >
              <Volume2 className="w-4 h-4" />
            </button>

            {/* Settings */}
            <button
              onClick={() => setActiveModal('settings')}
              className="p-1.5 rounded-full hover:bg-white/10 transition-colors cursor-pointer opacity-85 hover:opacity-100"
              title="Keyboard Settings"
            >
              <Settings className="w-4 h-4" />
            </button>

            {/* Clear Editor */}
            {editorText.length > 0 && (
              <button
                onClick={clearEditor}
                className="p-1.5 rounded-full hover:bg-rose-500/20 text-rose-400 hover:text-rose-300 transition-colors cursor-pointer"
                title="Clear Text"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
