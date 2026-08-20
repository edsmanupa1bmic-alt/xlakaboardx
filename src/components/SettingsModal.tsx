import React, { useState } from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import {
  X,
  Volume2,
  Vibrate,
  Sliders,
  Type,
  BookOpen,
  Info,
  Check,
  RotateCcw,
  Sparkles,
  Smile,
  Keyboard as KeyboardIcon,
  Trash2
} from 'lucide-react';
import { SoundEffectType, EmojiStyleType, KeyboardLanguage } from '../types';
import { WordPredictionEngine } from '../engines/WordPredictionEngine';

export const SettingsModal: React.FC = () => {
  const {
    config,
    updateConfig,
    setActiveModal
  } = useKeyboard();

  const [activeTab, setActiveTab] = useState<'preferences' | 'typing' | 'layout' | 'dictionary' | 'about'>('preferences');
  const [learnedData, setLearnedData] = useState(() => WordPredictionEngine.getInstance().getLearnedWords());
  const [newWordInput, setNewWordInput] = useState('');
  const [newWordLang, setNewWordLang] = useState<'si' | 'en'>('si');

  const handleClearDictionary = () => {
    if (window.confirm('Clear all learned user words from dictionary?')) {
      WordPredictionEngine.getInstance().clearLearnedWords();
      setLearnedData({ sinhala: [], english: [] });
    }
  };

  const handleAddCustomWord = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newWordInput.trim()) return;
    WordPredictionEngine.getInstance().learnWord(newWordInput.trim(), newWordLang === 'si');
    setLearnedData(WordPredictionEngine.getInstance().getLearnedWords());
    setNewWordInput('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-2xl bg-zinc-900 border border-zinc-800 text-zinc-100 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-zinc-900/90">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-blue-600/20 text-blue-400">
              <Sliders className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Keyboard Settings</h2>
              <p className="text-xs text-zinc-400">Fossify / Flashboard / Lakmal Keyboard</p>
            </div>
          </div>
          <button
            onClick={() => setActiveModal(null)}
            className="p-1.5 rounded-full hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Strip */}
        <div className="flex border-b border-zinc-800 bg-zinc-950/60 px-6 overflow-x-auto no-scrollbar">
          <button
            onClick={() => setActiveTab('preferences')}
            className={`py-3 px-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all cursor-pointer ${
              activeTab === 'preferences'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Sound & Haptics
          </button>
          <button
            onClick={() => setActiveTab('typing')}
            className={`py-3 px-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all cursor-pointer ${
              activeTab === 'typing'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Typing & Prediction
          </button>
          <button
            onClick={() => setActiveTab('layout')}
            className={`py-3 px-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all cursor-pointer ${
              activeTab === 'layout'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Layout & Appearance
          </button>
          <button
            onClick={() => setActiveTab('dictionary')}
            className={`py-3 px-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all cursor-pointer ${
              activeTab === 'dictionary'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Learned Dictionary
          </button>
          <button
            onClick={() => setActiveTab('about')}
            className={`py-3 px-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all cursor-pointer ${
              activeTab === 'about'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            About
          </button>
        </div>

        {/* Tab Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {/* TAB 1: PREFERENCES (HAPTICS & SOUND) */}
          {activeTab === 'preferences' && (
            <div className="space-y-5">
              {/* Vibration Toggle */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-zinc-700/50 text-blue-400">
                    <Vibrate className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">Vibrate on Keypress</h4>
                    <p className="text-[11px] text-zinc-400">Provides physical haptic response during key taps</p>
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={config.vibrateOnKeypress}
                  onChange={e => updateConfig({ vibrateOnKeypress: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Sound Style Selection */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800 space-y-3">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-zinc-700/50 text-blue-400">
                    <Volume2 className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">Keypress Sound Effect</h4>
                    <p className="text-[11px] text-zinc-400">Select simulated mechanical keyboard feedback sound</p>
                  </div>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 pt-1">
                  {[
                    { id: 'modern', name: 'Modern Bubble' },
                    { id: 'system', name: 'Crisp Click' },
                    { id: 'typewriter', name: 'Typewriter' },
                    { id: 'soft', name: 'Soft Thud' },
                    { id: 'none', name: 'Muted (None)' }
                  ].map(s => (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => updateConfig({ soundOnKeypress: s.id as SoundEffectType })}
                      className={`px-3 py-2 rounded-lg text-xs font-medium border text-left transition-all cursor-pointer ${
                        config.soundOnKeypress === s.id
                          ? 'border-blue-500 bg-blue-600/20 text-blue-400 font-bold'
                          : 'border-zinc-700 bg-zinc-800/60 text-zinc-300 hover:bg-zinc-700/60'
                      }`}
                    >
                      {s.name}
                    </button>
                  ))}
                </div>

                {config.soundOnKeypress !== 'none' && (
                  <div className="pt-2 border-t border-zinc-700/50">
                    <div className="flex justify-between text-xs text-zinc-300 mb-1">
                      <span>Sound Volume</span>
                      <span className="font-mono text-blue-400">{config.keypressSoundVolume}%</span>
                    </div>
                    <input
                      type="range"
                      min="5"
                      max="100"
                      value={config.keypressSoundVolume}
                      onChange={e => updateConfig({ keypressSoundVolume: Number(e.target.value) })}
                      className="w-full accent-blue-500 cursor-pointer"
                    />
                  </div>
                )}
              </div>

              {/* Popup on keypress */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Keypress Preview Popup</h4>
                  <p className="text-[11px] text-zinc-400">Show magnified character bubble above pressed keys</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.showPopupOnKeypress}
                  onChange={e => updateConfig({ showPopupOnKeypress: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Touch & Hold Long-press Symbols */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800 space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-white">Long-press for Symbols</h4>
                    <p className="text-[11px] text-zinc-400">Hold any letter key to insert its secondary symbol</p>
                  </div>
                  <input
                    type="checkbox"
                    checked={config.touchHoldSymbols}
                    onChange={e => updateConfig({ touchHoldSymbols: e.target.checked })}
                    className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                  />
                </div>
                {config.touchHoldSymbols && (
                  <div>
                    <div className="flex justify-between text-xs text-zinc-300 mb-1">
                      <span>Long-press Delay</span>
                      <span className="font-mono text-blue-400">{config.touchHoldDelay || 350} ms</span>
                    </div>
                    <input
                      type="range"
                      min="200"
                      max="700"
                      step="25"
                      value={config.touchHoldDelay || 350}
                      onChange={e => updateConfig({ touchHoldDelay: Number(e.target.value) })}
                      className="w-full accent-blue-500 cursor-pointer"
                    />
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 2: TYPING & PREDICTION */}
          {activeTab === 'typing' && (
            <div className="space-y-4">
              {/* Default Language Selector */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800 space-y-2">
                <h4 className="text-xs font-bold text-white">Primary Input Mode</h4>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  {[
                    { id: 'sinhala_singlish', name: 'Sinhala (Singlish Phonetic)' },
                    { id: 'sinhala_wijesekara', name: 'Sinhala (Wijesekara Layout)' },
                    { id: 'english_qwerty', name: 'English (US QWERTY)' }
                  ].map(l => (
                    <button
                      key={l.id}
                      type="button"
                      onClick={() => updateConfig({ keyboardLanguage: l.id as KeyboardLanguage })}
                      className={`p-2.5 rounded-lg text-xs font-medium border text-left transition-all cursor-pointer ${
                        config.keyboardLanguage === l.id
                          ? 'border-blue-500 bg-blue-600/20 text-blue-400 font-bold'
                          : 'border-zinc-700 bg-zinc-800/60 text-zinc-300 hover:bg-zinc-700/60'
                      }`}
                    >
                      {l.name}
                    </button>
                  ))}
                </div>
              </div>

              {/* Suggestion Strip Toggle */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Smartbar Suggestion Strip</h4>
                  <p className="text-[11px] text-zinc-400">Display word predictions, emoji suggestions, and quick actions</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.suggestionStripEnabled}
                  onChange={e => updateConfig({ suggestionStripEnabled: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Smart Auto Correction */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Smart Typo Auto-Correction</h4>
                  <p className="text-[11px] text-zinc-400">Fix common Sinhala and English typos upon pressing spacebar</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.smartAutoCorrection}
                  onChange={e => updateConfig({ smartAutoCorrection: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Double space for period */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Double-Space Period</h4>
                  <p className="text-[11px] text-zinc-400">Double-tapping spacebar automatically inserts a period followed by space</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.doubleSpacePeriod}
                  onChange={e => updateConfig({ doubleSpacePeriod: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Auto capitalization */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Auto-Capitalize Sentences</h4>
                  <p className="text-[11px] text-zinc-400">Automatically switch to uppercase after period in English mode</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.enableSentencesCapitalization}
                  onChange={e => updateConfig({ enableSentencesCapitalization: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>
            </div>
          )}

          {/* TAB 3: LAYOUT & KEYS */}
          {activeTab === 'layout' && (
            <div className="space-y-4">
              {/* Keyboard Height Slider */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div className="flex justify-between text-xs text-zinc-300 mb-1">
                  <span className="font-bold">Keyboard Height Scale</span>
                  <span className="font-mono text-blue-400">{config.keyboardHeightPercentage}%</span>
                </div>
                <input
                  type="range"
                  min="80"
                  max="130"
                  value={config.keyboardHeightPercentage}
                  onChange={e => updateConfig({ keyboardHeightPercentage: Number(e.target.value) })}
                  className="w-full accent-blue-500 cursor-pointer"
                />
              </div>

              {/* Font Scale Slider */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div className="flex justify-between text-xs text-zinc-300 mb-1">
                  <span className="font-bold">Key Font Scale</span>
                  <span className="font-mono text-blue-400">{config.fontScale}%</span>
                </div>
                <input
                  type="range"
                  min="80"
                  max="130"
                  value={config.fontScale}
                  onChange={e => updateConfig({ fontScale: Number(e.target.value) })}
                  className="w-full accent-blue-500 cursor-pointer"
                />
              </div>

              {/* Numbers Row Toggle */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Dedicated Numbers Row</h4>
                  <p className="text-[11px] text-zinc-400">Display 0-9 digits at the top row of the letter keyboard</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.showNumbersRow}
                  onChange={e => updateConfig({ showNumbersRow: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Emoji Key Toggle */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Show Emoji Key on Bottom Row</h4>
                  <p className="text-[11px] text-zinc-400">Dedicated smiley button next to spacebar</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.showEmojiKey}
                  onChange={e => updateConfig({ showEmojiKey: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Language Switch Key Toggle */}
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <div>
                  <h4 className="text-xs font-bold text-white">Show Language Switch Globe</h4>
                  <p className="text-[11px] text-zinc-400">Quickly toggle between Singlish, Wijesekara and English</p>
                </div>
                <input
                  type="checkbox"
                  checked={config.showLanguageSwitchKey}
                  onChange={e => updateConfig({ showLanguageSwitchKey: e.target.checked })}
                  className="w-4 h-4 accent-blue-500 rounded cursor-pointer"
                />
              </div>

              {/* Emoji Style Selection */}
              <div className="p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800 space-y-2">
                <h4 className="text-xs font-bold text-white">Emoji Render Style</h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  {[
                    { id: 'whatsapp', name: 'WhatsApp' },
                    { id: 'apple', name: 'Apple iOS' },
                    { id: 'google', name: 'Google' },
                    { id: 'system', name: 'System' }
                  ].map(style => (
                    <button
                      key={style.id}
                      type="button"
                      onClick={() => updateConfig({ emojiStyle: style.id as EmojiStyleType })}
                      className={`px-3 py-2 rounded-lg text-xs font-medium border text-center transition-all cursor-pointer ${
                        config.emojiStyle === style.id
                          ? 'border-blue-500 bg-blue-600/20 text-blue-400 font-bold'
                          : 'border-zinc-700 bg-zinc-800/60 text-zinc-300 hover:bg-zinc-700/60'
                      }`}
                    >
                      {style.name}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: LEARNED DICTIONARY */}
          {activeTab === 'dictionary' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold text-white">Personal Learned Words</h4>
                  <p className="text-[11px] text-zinc-400">Words automatically learned as you type to improve suggestions</p>
                </div>
                <button
                  type="button"
                  onClick={handleClearDictionary}
                  className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-rose-600/20 text-rose-400 hover:bg-rose-600/30 text-xs font-medium cursor-pointer transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Clear All</span>
                </button>
              </div>

              {/* Add custom word form */}
              <form onSubmit={handleAddCustomWord} className="flex gap-2 p-2 rounded-lg bg-zinc-800/60 border border-zinc-700">
                <input
                  type="text"
                  placeholder="Add custom word (e.g. කොළඹ or Colombo)..."
                  value={newWordInput}
                  onChange={e => setNewWordInput(e.target.value)}
                  className="flex-1 px-3 py-1 text-xs bg-zinc-900 rounded border border-zinc-700 text-white outline-hidden"
                />
                <select
                  value={newWordLang}
                  onChange={e => setNewWordLang(e.target.value as 'si' | 'en')}
                  className="text-xs px-2 py-1 bg-zinc-900 text-zinc-300 rounded border border-zinc-700 outline-hidden"
                >
                  <option value="si">Sinhala</option>
                  <option value="en">English</option>
                </select>
                <button
                  type="submit"
                  className="px-3 py-1 bg-blue-600 hover:bg-blue-500 text-white text-xs font-medium rounded cursor-pointer"
                >
                  Add Word
                </button>
              </form>

              {/* List of learned words */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3 rounded-lg bg-zinc-800/40 border border-zinc-800">
                  <h5 className="text-xs font-bold text-amber-400 mb-2">Sinhala Learned ({learnedData.sinhala.length})</h5>
                  <div className="max-h-40 overflow-y-auto space-y-1 text-xs">
                    {learnedData.sinhala.length > 0 ? (
                      learnedData.sinhala.map(([w, f]) => (
                        <div key={w} className="flex justify-between py-0.5 border-b border-zinc-800">
                          <span className="font-sinhala text-zinc-200">{w}</span>
                          <span className="text-[10px] text-zinc-500 font-mono">{f}</span>
                        </div>
                      ))
                    ) : (
                      <p className="text-[11px] text-zinc-500 italic">No custom words learned yet.</p>
                    )}
                  </div>
                </div>

                <div className="p-3 rounded-lg bg-zinc-800/40 border border-zinc-800">
                  <h5 className="text-xs font-bold text-blue-400 mb-2">English Learned ({learnedData.english.length})</h5>
                  <div className="max-h-40 overflow-y-auto space-y-1 text-xs">
                    {learnedData.english.length > 0 ? (
                      learnedData.english.map(([w, f]) => (
                        <div key={w} className="flex justify-between py-0.5 border-b border-zinc-800">
                          <span className="text-zinc-200">{w}</span>
                          <span className="text-[10px] text-zinc-500 font-mono">{f}</span>
                        </div>
                      ))
                    ) : (
                      <p className="text-[11px] text-zinc-500 italic">No custom words learned yet.</p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 5: ABOUT */}
          {activeTab === 'about' && (
            <div className="space-y-4 text-xs text-zinc-300 leading-relaxed">
              <div className="flex items-center gap-3 p-4 rounded-xl bg-blue-600/10 border border-blue-500/20">
                <div className="p-3 rounded-xl bg-blue-600 text-white shadow-md">
                  <KeyboardIcon className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-white">Lakmal Keyboard (xLakaBoardx)</h3>
                  <p className="text-[11px] text-zinc-400">Open-source Sinhala & English Intelligent Keyboard</p>
                  <p className="text-[10px] text-blue-400 font-mono mt-0.5">Version 1.0.0 Web Edition (GPL-3.0)</p>
                </div>
              </div>

              <div className="space-y-2 p-3.5 rounded-xl bg-zinc-800/40 border border-zinc-800">
                <h4 className="font-bold text-white">Key Features Faithfully Ported:</h4>
                <ul className="list-disc list-inside space-y-1 text-zinc-400 text-[11px]">
                  <li>Real-time Singlish phonetic transliteration engine with full consonant & vowel combinations.</li>
                  <li>Traditional Sinhala Wijesekara layout mapping.</li>
                  <li>Trie-based Sinhala and English predictive autocomplete & typo auto-correction.</li>
                  <li>Built-in Smartbar with inline math calculator (<code className="text-zinc-300">250*4=</code>).</li>
                  <li>Clipboard items manager with pin, export, and import support.</li>
                  <li>Rich emoji categories, search, and emoji render styles.</li>
                  <li>Advanced Theme Creator with live color customizer, key translucency, corner radius, borders, wallpaper support, and RGB animations.</li>
                  <li>Synthesized mechanical sound feedback and Web Audio effects.</li>
                </ul>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
