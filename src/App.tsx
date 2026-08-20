import React, { useState, useEffect } from 'react';
import { KeyboardProvider, useKeyboard } from './context/KeyboardContext';
import { KeyboardRoot } from './components/KeyboardRoot';
import { SettingsModal } from './components/SettingsModal';
import { ThemeCustomizerModal } from './components/ThemeCustomizerModal';
import { AndroidApkModal } from './components/AndroidApkModal';
import {
  Copy,
  Trash2,
  Settings,
  Palette,
  ClipboardList,
  Volume2,
  VolumeX,
  BookOpen,
  Download,
  Check,
  Sparkles,
  Smartphone,
  Monitor,
  HelpCircle,
  X,
  Keyboard as KeyboardIcon,
  Volume1,
  MessageSquare,
  Android
} from 'lucide-react';

const SINGLISH_CHEAT_SHEET = [
  { eng: 'a / aa', sin: 'අ / ආ', example: 'amma -> අම්මා, aaththa -> ආත්තා' },
  { eng: 'ae / aae', sin: 'ඇ / ඈ', example: 'aenda -> ඇඳ' },
  { eng: 'i / ii (ee)', sin: 'ඉ / ඊ', example: 'irida -> ඉරිදා, eeye -> ඊයේ' },
  { eng: 'u / uu (oo)', sin: 'උ / ඌ', example: 'udeta -> උදේට, oora -> ඌරා' },
  { eng: 'e / ee', sin: 'එ / ඒ', example: 'elawalu -> එළවලු, eeka -> ඒක' },
  { eng: 'o / oo', sin: 'ඔ / ඕ', example: 'onama -> ඕනෑම' },
  { eng: 'k / g / c', sin: 'ක / ග / ච', example: 'kaama -> කෑම, gedara -> ගෙදර' },
  { eng: 't / d / th / dh', sin: 'ට / ඩ / ත / ද', example: 'tikak -> ටිකක්, thaniwa -> තනිව' },
  { eng: 'p / b', sin: 'ප / බ', example: 'poth -> පොත්, balla -> බල්ලා' },
  { eng: 'n / N / m', sin: 'න / ණ / ම', example: 'nangi -> නංගි, maama -> මාමා' },
  { eng: 'y / r / l / L / v / w', sin: 'ය / ර / ල / ළ / ව', example: 'lanka -> ලංකා, wathura -> වතුර' },
  { eng: 's / sh / Sh / h', sin: 'ස / ශ / ෂ / හ', example: 'sithuwili -> සිතුවිලි, shaanthi -> ශාන්ති' },
  { eng: 'nd / ng / ngy', sin: 'ඳ / ඟ / ඥ', example: 'kandha -> කන්ද, saMgaya -> සංඝයා' }
];

const SAMPLE_TEXTS = [
  { label: 'Greeting', text: 'ආයුබෝවන්! ඔබට සුභ දවසක් වේවා!' },
  { label: 'Singlish sample', text: 'mama lakboard pavichchi karanawa, eka nisa liyanna godak lesiyi.' },
  { label: 'Sinhala Song Lyric', text: 'රන්වන් කරල් සැලෙනා... අපේ සරු කෙතේ...' },
  { label: 'Wijesekara Test', text: 'ශ්‍රී ලංකා ප්‍රජාතාන්ත්‍රික සමාජවාදී ජනරජය' }
];

const KeyboardAppContent: React.FC = () => {
  const {
    editorText,
    setEditorText,
    inputBuffer,
    textareaRef,
    clearEditor,
    config,
    updateConfig,
    activeTheme,
    activeModal,
    setActiveModal,
    setLayoutMode,
    layoutMode,
    insertTextDirectly,
    showKeyPreview
  } = useKeyboard();

  const [copied, setCopied] = useState(false);
  const [showCheatsheet, setShowCheatsheet] = useState(false);
  const [deviceView, setDeviceView] = useState<'mobile' | 'docked'>('mobile');

  // Copy text to clipboard
  const handleCopy = async () => {
    if (!editorText) return;
    try {
      await navigator.clipboard.writeText(editorText);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy', err);
    }
  };

  // Export as text file
  const handleDownload = () => {
    if (!editorText) return;
    const blob = new Blob([editorText], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `lakboard_note_${Date.now()}.txt`;
    link.click();
    URL.revokeObjectURL(url);
  };

  // Word & character stats
  const charCount = editorText.length;
  const wordCount = editorText.trim() ? editorText.trim().split(/\s+/).length : 0;

  // Language display name
  const languageLabel =
    config.keyboardLanguage === 'sinhala_singlish'
      ? 'Singlish (සිංග්ලිෂ්)'
      : config.keyboardLanguage === 'sinhala_wijesekara'
      ? 'Wijesekara (විජේසේකර)'
      : 'English (QWERTY)';

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100 flex flex-col items-center justify-between selection:bg-blue-600 selection:text-white font-sans">
      {/* Top Application Navigation Bar */}
      <header className="w-full border-b border-zinc-800/80 bg-zinc-900/80 backdrop-blur-md sticky top-0 z-30 px-4 py-2.5">
        <div className="max-w-5xl mx-auto flex items-center justify-between flex-wrap gap-2">
          {/* Logo & Brand Info */}
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-md shadow-blue-500/20 text-white font-black text-sm">
              LB
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-sm font-bold tracking-tight text-white">Lakmal Keyboard</h1>
                <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-blue-500/10 text-blue-400 font-semibold border border-blue-500/20">
                  xLakaBoardx
                </span>
              </div>
              <p className="text-[11px] text-zinc-400">Sinhala & English Smart Input System</p>
            </div>
          </div>

          {/* Quick Config Badges & Actions */}
          <div className="flex items-center gap-2">
            {/* View Mode Toggle */}
            <div className="hidden sm:flex items-center bg-zinc-800/80 p-0.5 rounded-lg border border-zinc-700/50">
              <button
                id="btn-mobile-view"
                onClick={() => setDeviceView('mobile')}
                className={`flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium transition-all ${
                  deviceView === 'mobile'
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-zinc-400 hover:text-zinc-200'
                }`}
                title="Mobile Phone View"
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span>Phone</span>
              </button>
              <button
                id="btn-docked-view"
                onClick={() => setDeviceView('docked')}
                className={`flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium transition-all ${
                  deviceView === 'docked'
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-zinc-400 hover:text-zinc-200'
                }`}
                title="Docked Full View"
              >
                <Monitor className="w-3.5 h-3.5" />
                <span>Docked</span>
              </button>
            </div>

            {/* Sound Toggle */}
            <button
              id="btn-sound-toggle"
              onClick={() => updateConfig({ soundOnKeypress: config.soundOnKeypress === 'none' ? 'modern' : 'none' })}
              className={`p-2 rounded-lg border transition-colors ${
                config.soundOnKeypress !== 'none'
                  ? 'bg-zinc-800 border-zinc-700 text-blue-400 hover:bg-zinc-700'
                  : 'bg-zinc-900 border-zinc-800 text-zinc-500 hover:text-zinc-300'
              }`}
              title={config.soundOnKeypress !== 'none' ? 'Audio Feedback Active' : 'Muted'}
            >
              {config.soundOnKeypress !== 'none' ? (
                <Volume2 className="w-4 h-4" />
              ) : (
                <VolumeX className="w-4 h-4" />
              )}
            </button>

            {/* Android APK / Install Button */}
            <button
              id="btn-android-apk"
              onClick={() => setActiveModal('android_apk')}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 border border-emerald-500/30 text-xs font-semibold transition-all hover:scale-105 shadow-sm shadow-emerald-500/10"
              title="Install WebAPK or Generate Android APK"
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Android APK</span>
            </button>

            {/* Singlish Guide / Cheatsheet */}
            <button
              id="btn-cheatsheet"
              onClick={() => setShowCheatsheet(true)}
              className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-zinc-200 text-xs font-medium transition-colors"
              title="Singlish Typing Guide"
            >
              <BookOpen className="w-3.5 h-3.5 text-amber-400" />
              <span className="hidden sm:inline">Singlish Guide</span>
            </button>

            {/* Theme Customizer Button */}
            <button
              id="btn-theme-modal"
              onClick={() => setActiveModal('theme_customizer')}
              className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-zinc-200 text-xs font-medium transition-colors"
              title="Change Theme & Appearance"
            >
              <Palette className="w-3.5 h-3.5 text-pink-400" />
              <span className="hidden md:inline">Themes</span>
            </button>

            {/* Settings Button */}
            <button
              id="btn-settings-modal"
              onClick={() => setActiveModal('settings')}
              className="p-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-zinc-200 transition-colors"
              title="Keyboard Preferences & Settings"
            >
              <Settings className="w-4 h-4 text-blue-400" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Workspace Area */}
      <main className="w-full flex-1 flex flex-col items-center justify-start p-2 sm:p-4 max-w-5xl mx-auto">
        <div
          className={`w-full flex flex-col transition-all duration-300 ${
            deviceView === 'mobile'
              ? 'max-w-[440px] bg-zinc-900/90 border border-zinc-800 rounded-3xl shadow-2xl p-3 sm:p-4 my-2'
              : 'max-w-4xl bg-zinc-900/50 border border-zinc-800/80 rounded-2xl p-4 my-2'
          }`}
        >
          {/* Editor Header Bar */}
          <div className="flex items-center justify-between pb-2 mb-2 border-b border-zinc-800/80 text-xs text-zinc-400 flex-wrap gap-2">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded-md bg-zinc-800 border border-zinc-700/60 font-medium text-zinc-300">
                {languageLabel}
              </span>
              <span className="text-[11px] text-zinc-400">
                Theme: <strong className="text-zinc-300">{activeTheme.name}</strong>
              </span>
            </div>

            {/* Action Tools */}
            <div className="flex items-center gap-1.5">
              <button
                id="btn-copy-text"
                onClick={handleCopy}
                disabled={!editorText}
                className="flex items-center gap-1 px-2.5 py-1 rounded-md bg-zinc-800 hover:bg-zinc-700 disabled:opacity-40 disabled:hover:bg-zinc-800 text-zinc-200 text-xs font-medium transition-colors"
                title="Copy Text"
              >
                {copied ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    <span className="text-emerald-400">Copied!</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    <span>Copy</span>
                  </>
                )}
              </button>

              <button
                id="btn-export-text"
                onClick={handleDownload}
                disabled={!editorText}
                className="p-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 disabled:opacity-40 disabled:hover:bg-zinc-800 text-zinc-300 transition-colors"
                title="Export as .txt"
              >
                <Download className="w-3.5 h-3.5" />
              </button>

              <button
                id="btn-clear-text"
                onClick={clearEditor}
                disabled={!editorText && !inputBuffer}
                className="p-1.5 rounded-md bg-zinc-800 hover:bg-red-950/40 hover:text-red-400 disabled:opacity-40 text-zinc-300 transition-colors"
                title="Clear Text"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          {/* Quick Phrase Samples Pill Strip */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-2 mb-1 scrollbar-none">
            <span className="text-[11px] text-zinc-400 whitespace-nowrap flex items-center gap-1">
              <Sparkles className="w-3 h-3 text-amber-400" /> Samples:
            </span>
            {SAMPLE_TEXTS.map((sample, idx) => (
              <button
                key={idx}
                onClick={() => insertTextDirectly(sample.text)}
                className="text-[11px] px-2 py-0.5 rounded-full bg-zinc-800/80 hover:bg-zinc-700/80 border border-zinc-700/50 text-zinc-300 whitespace-nowrap transition-colors"
              >
                {sample.label}
              </button>
            ))}
          </div>

          {/* Real-time Transliteration Input Buffer Bar */}
          {inputBuffer && (
            <div className="mb-2 px-3 py-1.5 rounded-lg bg-blue-950/50 border border-blue-500/30 flex items-center justify-between text-xs text-blue-200 animate-in fade-in duration-100">
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-blue-600 text-white uppercase tracking-wider">
                  Converting
                </span>
                <span className="font-mono text-zinc-300">"{inputBuffer}"</span>
              </div>
              <span className="text-[11px] text-blue-300">Press Space or select suggestion to insert</span>
            </div>
          )}

          {/* Primary Textarea Output Canvas */}
          <div className="relative mb-3">
            <textarea
              ref={textareaRef as React.RefObject<HTMLTextAreaElement>}
              value={editorText}
              onChange={(e) => setEditorText(e.target.value)}
              placeholder="Tap keyboard keys or use your hardware keyboard to type in Sinhala & English..."
              rows={deviceView === 'mobile' ? 5 : 7}
              className="w-full px-3.5 py-3 rounded-xl bg-zinc-950/80 border border-zinc-800 text-zinc-100 placeholder:text-zinc-600 focus:outline-hidden focus:border-blue-500 focus:ring-1 focus:ring-blue-500 text-base leading-relaxed resize-none transition-all"
              style={{
                fontFamily: "'Plus Jakarta Sans', 'Noto Sans Sinhala', sans-serif"
              }}
            />
            {/* Word & Char counter badge */}
            <div className="absolute bottom-2.5 right-3 text-[11px] text-zinc-400 bg-zinc-900/80 px-2 py-0.5 rounded-md border border-zinc-800">
              {wordCount} words • {charCount} chars
            </div>
          </div>

          {/* Keyboard Container Frame */}
          <div className="w-full mt-1">
            <KeyboardRoot />
          </div>
        </div>
      </main>

      {/* Floating Key Preview Bubble */}
      {showKeyPreview && (
        <div className="fixed bottom-40 left-1/2 -translate-x-1/2 z-50 pointer-events-none animate-in zoom-in-90 duration-75">
          <div className="w-16 h-16 rounded-2xl bg-zinc-800 border-2 border-blue-500 text-white shadow-2xl flex items-center justify-center font-bold text-2xl">
            {showKeyPreview}
          </div>
        </div>
      )}

      {/* Modals */}
      {activeModal === 'settings' && <SettingsModal />}
      {activeModal === 'theme_customizer' && <ThemeCustomizerModal />}
      {activeModal === 'android_apk' && <AndroidApkModal />}

      {/* Singlish Guide / Cheatsheet Drawer Modal */}
      {showCheatsheet && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="w-full max-w-2xl bg-zinc-900 border border-zinc-800 text-zinc-100 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-zinc-900/90">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-lg bg-amber-500/20 text-amber-400">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-bold text-white">Singlish Typing Guide</h2>
                  <p className="text-xs text-zinc-400">How to type Sinhala using English letters (Singlish)</p>
                </div>
              </div>
              <button
                id="btn-close-cheatsheet"
                onClick={() => setShowCheatsheet(false)}
                className="p-1.5 rounded-lg hover:bg-zinc-800 text-zinc-400 hover:text-zinc-200 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 overflow-y-auto space-y-4">
              <div className="p-4 rounded-xl bg-blue-950/30 border border-blue-500/30 text-xs text-blue-200 space-y-1">
                <p className="font-semibold text-blue-300">💡 Quick Tip for Singlish:</p>
                <p>Type English phonetic sounds (e.g., <span className="font-mono text-white">gedara</span>) and Lakmal Keyboard will automatically transliterate it to <span className="font-bold text-white">ගෙදර</span> in real-time as you type!</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {SINGLISH_CHEAT_SHEET.map((item, idx) => (
                  <div key={idx} className="p-3 rounded-xl bg-zinc-800/60 border border-zinc-700/50 flex flex-col gap-1">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-blue-400 font-bold text-sm">{item.eng}</span>
                      <span className="text-base font-bold text-zinc-100">{item.sin}</span>
                    </div>
                    <div className="text-xs text-zinc-400">{item.example}</div>
                  </div>
                ))}
              </div>

              <div className="p-4 rounded-xl bg-zinc-800/40 border border-zinc-700/50 space-y-2">
                <h3 className="text-xs font-bold text-zinc-300 uppercase tracking-wider">Useful Shortcuts & Modifiers</h3>
                <ul className="text-xs text-zinc-400 space-y-1 list-disc list-inside">
                  <li><strong className="text-zinc-200">Capital Letters (Shift)</strong>: Creates long vowels (e.g. <span className="font-mono">A</span> = ආ, <span className="font-mono">I</span> = ඊ, <span className="font-mono">U</span> = ඌ, <span className="font-mono">E</span> = ඒ, <span className="font-mono">O</span> = ඕ) and retroflex consonants (<span className="font-mono">T</span> = ට, <span className="font-mono">D</span> = ඩ, <span className="font-mono">N</span> = ණ, <span className="font-mono">L</span> = ළ).</li>
                  <li><strong className="text-zinc-200">Inline Calculator</strong>: Type mathematical equations like <span className="font-mono">25*4=</span> or <span className="font-mono">100+250=</span> and the smartbar will instantly calculate the result!</li>
                  <li><strong className="text-zinc-200">Clipboard Manager</strong>: Access saved clips and frequently used messages anytime using the Clipboard button on the smartbar.</li>
                </ul>
              </div>
            </div>

            <div className="px-6 py-3 border-t border-zinc-800 bg-zinc-900/90 flex justify-end">
              <button
                id="btn-cheatsheet-done"
                onClick={() => setShowCheatsheet(false)}
                className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold transition-colors"
              >
                Got It
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default function App() {
  return (
    <KeyboardProvider>
      <KeyboardAppContent />
    </KeyboardProvider>
  );
}
