import React, { useState } from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import { KeyboardTheme } from '../types';
import {
  X,
  Check,
  Palette,
  Image as ImageIcon,
  Sparkles,
  Sliders,
  Plus,
  Trash2,
  Upload
} from 'lucide-react';

export const ThemeCustomizerModal: React.FC = () => {
  const {
    themes,
    activeTheme,
    setActiveThemeId,
    saveCustomTheme,
    deleteCustomTheme,
    config,
    updateConfig,
    setActiveModal
  } = useKeyboard();

  const [activeTab, setActiveTab] = useState<'presets' | 'customizer'>('presets');

  // Custom theme editor state
  const [draftTheme, setDraftTheme] = useState<KeyboardTheme>(() => ({
    ...activeTheme,
    id: `custom_${Date.now()}`,
    name: 'My Custom Theme',
    author: 'You',
    isBuiltIn: false
  }));

  const handleApplyPreset = (themeId: string) => {
    setActiveThemeId(themeId);
  };

  const handleSaveCustom = (e: React.FormEvent) => {
    e.preventDefault();
    const newTheme: KeyboardTheme = {
      ...draftTheme,
      id: draftTheme.id.startsWith('custom_') ? draftTheme.id : `custom_${Date.now()}`
    };
    saveCustomTheme(newTheme);
    setActiveTab('presets');
  };

  const handleWallpaperUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = event => {
      const url = event.target?.result as string;
      setDraftTheme(prev => ({ ...prev, wallpaperUrl: url }));
      updateConfig({ bgImageEnabled: true });
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-2xl bg-zinc-900 border border-zinc-800 text-zinc-100 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-zinc-900/90">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-blue-600/20 text-blue-400">
              <Palette className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Theme & Appearance Customizer</h2>
              <p className="text-xs text-zinc-400">Ported from Flashboard & Lakmal Keyboard ThemeEngine</p>
            </div>
          </div>
          <button
            onClick={() => setActiveModal(null)}
            className="p-1.5 rounded-full hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-zinc-800 bg-zinc-950/60 px-6">
          <button
            onClick={() => setActiveTab('presets')}
            className={`py-3 px-4 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
              activeTab === 'presets'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Built-in Themes ({themes.length})
          </button>
          <button
            onClick={() => setActiveTab('customizer')}
            className={`py-3 px-4 text-xs font-semibold border-b-2 transition-all cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'customizer'
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Theme Creator & Live Style</span>
          </button>
        </div>

        {/* Modal Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {activeTab === 'presets' ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                {themes.map(theme => {
                  const isCurrent = theme.id === activeTheme.id;
                  return (
                    <div
                      key={theme.id}
                      onClick={() => handleApplyPreset(theme.id)}
                      className={`relative p-3.5 rounded-xl border transition-all cursor-pointer flex flex-col justify-between overflow-hidden group shadow-sm ${
                        isCurrent
                          ? 'ring-2 ring-blue-500 border-blue-500 shadow-blue-500/10'
                          : 'border-zinc-800 hover:border-zinc-700 hover:bg-zinc-800/40'
                      }`}
                      style={{ backgroundColor: theme.keyboardBgColor }}
                    >
                      {/* Theme Header */}
                      <div className="flex items-center justify-between mb-3">
                        <div>
                          <h4 className="text-xs font-bold" style={{ color: theme.keyTextColor }}>
                            {theme.name}
                          </h4>
                          <span className="text-[10px] opacity-70" style={{ color: theme.keyTextColor }}>
                            By {theme.author} {theme.isBuiltIn ? '• Built-in' : '• Custom'}
                          </span>
                        </div>
                        {isCurrent && (
                          <div className="p-1 rounded-full bg-blue-500 text-white shadow-xs">
                            <Check className="w-3 h-3 stroke-[3]" />
                          </div>
                        )}
                      </div>

                      {/* Mini Keypad Preview */}
                      <div
                        className="p-2 rounded-lg flex flex-col gap-1 border"
                        style={{
                          backgroundColor: theme.smartbarBgColor,
                          borderColor: theme.strokeColor
                        }}
                      >
                        <div className="flex gap-1">
                          {['Q', 'W', 'E', 'R', 'T'].map(k => (
                            <div
                              key={k}
                              className="flex-1 h-5 rounded text-[10px] flex items-center justify-center font-medium shadow-xs"
                              style={{
                                backgroundColor: theme.keyBgColor,
                                color: theme.keyTextColor,
                                borderRadius: `${theme.cornerRadius || 4}px`
                              }}
                            >
                              {k}
                            </div>
                          ))}
                          <div
                            className="flex-1 h-5 rounded text-[10px] flex items-center justify-center font-medium shadow-xs"
                            style={{
                              backgroundColor: theme.accentKeyBgColor,
                              color: theme.accentKeyTextColor,
                              borderRadius: `${theme.cornerRadius || 4}px`
                            }}
                          >
                            ↵
                          </div>
                        </div>
                        <div
                          className="w-full h-3 rounded flex items-center justify-center text-[8px] font-medium"
                          style={{
                            backgroundColor: theme.keyBgColor,
                            color: theme.keyTextColor,
                            borderRadius: `${theme.cornerRadius || 4}px`
                          }}
                        >
                          xLakaBoardx
                        </div>
                      </div>

                      {/* Custom Theme Delete Option */}
                      {!theme.isBuiltIn && (
                        <div className="mt-2 flex justify-end">
                          <button
                            onClick={e => {
                              e.stopPropagation();
                              deleteCustomTheme(theme.id);
                            }}
                            className="p-1 rounded hover:bg-rose-500/20 text-rose-400 text-xs flex items-center gap-1"
                          >
                            <Trash2 className="w-3 h-3" />
                            <span>Delete</span>
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {/* Add New Custom Theme Action */}
              <button
                onClick={() => {
                  setDraftTheme({
                    ...activeTheme,
                    id: `custom_${Date.now()}`,
                    name: 'My Custom Theme',
                    author: 'User',
                    isBuiltIn: false
                  });
                  setActiveTab('customizer');
                }}
                className="w-full py-3 border border-dashed border-zinc-700 hover:border-blue-500 rounded-xl text-xs font-semibold text-zinc-300 hover:text-blue-400 flex items-center justify-center gap-2 transition-colors cursor-pointer bg-zinc-900/50"
              >
                <Plus className="w-4 h-4" />
                <span>Create & Customize New Theme</span>
              </button>
            </div>
          ) : (
            /* Customizer Tab */
            <form onSubmit={handleSaveCustom} className="space-y-6">
              {/* Theme Name & Author */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-zinc-300 mb-1">Theme Name</label>
                  <input
                    type="text"
                    value={draftTheme.name}
                    onChange={e => setDraftTheme(prev => ({ ...prev, name: e.target.value }))}
                    className="w-full px-3 py-1.5 text-xs bg-zinc-800 border border-zinc-700 rounded-lg text-white outline-hidden focus:border-blue-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-zinc-300 mb-1">Spacebar Text</label>
                  <input
                    type="text"
                    value={config.customSpacebarText}
                    onChange={e => updateConfig({ customSpacebarText: e.target.value })}
                    placeholder="e.g. xLakaBoardx"
                    className="w-full px-3 py-1.5 text-xs bg-zinc-800 border border-zinc-700 rounded-lg text-white outline-hidden focus:border-blue-500"
                  />
                </div>
              </div>

              {/* Color Pickers Grid */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-zinc-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Palette className="w-3.5 h-3.5 text-blue-400" />
                  <span>Color Palette</span>
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Keyboard Background</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={draftTheme.keyboardBgColor}
                        onChange={e => setDraftTheme(prev => ({ ...prev, keyboardBgColor: e.target.value }))}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{draftTheme.keyboardBgColor}</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Key Background</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={draftTheme.keyBgColor}
                        onChange={e => setDraftTheme(prev => ({ ...prev, keyBgColor: e.target.value }))}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{draftTheme.keyBgColor}</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Key Text Color</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={draftTheme.keyTextColor}
                        onChange={e => setDraftTheme(prev => ({ ...prev, keyTextColor: e.target.value }))}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{draftTheme.keyTextColor}</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Action / Enter Key</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={draftTheme.accentKeyBgColor}
                        onChange={e => setDraftTheme(prev => ({ ...prev, accentKeyBgColor: e.target.value }))}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{draftTheme.accentKeyBgColor}</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Smartbar Background</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={draftTheme.smartbarBgColor || '#1B1C22'}
                        onChange={e => setDraftTheme(prev => ({ ...prev, smartbarBgColor: e.target.value }))}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{draftTheme.smartbarBgColor}</span>
                    </div>
                  </div>

                  <div>
                    <label className="text-[11px] text-zinc-400 block mb-1">Spacebar Color</label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        value={config.spacebarColor || '#0066FF'}
                        onChange={e => updateConfig({ spacebarColor: e.target.value })}
                        className="w-8 h-8 rounded border border-zinc-700 bg-transparent cursor-pointer"
                      />
                      <span className="text-xs font-mono text-zinc-300">{config.spacebarColor}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Geometry & Stroke Sliders */}
              <div className="space-y-4 border-t border-zinc-800 pt-4">
                <h4 className="text-xs font-bold text-zinc-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Sliders className="w-3.5 h-3.5 text-blue-400" />
                  <span>Geometry & Border Effects</span>
                </h4>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {/* Corner Radius */}
                  <div>
                    <div className="flex justify-between text-xs text-zinc-300 mb-1">
                      <span>Key Corner Radius</span>
                      <span className="font-mono text-blue-400">{draftTheme.cornerRadius || 8}px</span>
                    </div>
                    <input
                      type="range"
                      min="0"
                      max="20"
                      value={draftTheme.cornerRadius || 8}
                      onChange={e => setDraftTheme(prev => ({ ...prev, cornerRadius: Number(e.target.value) }))}
                      className="w-full accent-blue-500 cursor-pointer"
                    />
                  </div>

                  {/* Translucency */}
                  <div>
                    <div className="flex justify-between text-xs text-zinc-300 mb-1">
                      <span>Key Translucency Alpha</span>
                      <span className="font-mono text-blue-400">{config.keyTranslucencyAlpha || 95}%</span>
                    </div>
                    <input
                      type="range"
                      min="40"
                      max="100"
                      value={config.keyTranslucencyAlpha || 95}
                      onChange={e => updateConfig({ keyTranslucencyAlpha: Number(e.target.value) })}
                      className="w-full accent-blue-500 cursor-pointer"
                    />
                  </div>

                  {/* Key Border Width */}
                  <div>
                    <div className="flex justify-between text-xs text-zinc-300 mb-1">
                      <span>Key Border Stroke Width</span>
                      <span className="font-mono text-blue-400">{config.keyBorderWidth}px</span>
                    </div>
                    <input
                      type="range"
                      min="0"
                      max="4"
                      step="0.5"
                      value={config.keyBorderWidth}
                      onChange={e => updateConfig({ keyBorderWidth: Number(e.target.value) })}
                      className="w-full accent-blue-500 cursor-pointer"
                    />
                  </div>

                  {/* RGB Animated Text Effect */}
                  <div className="flex items-center justify-between p-2 rounded-lg bg-zinc-800/50 border border-zinc-700">
                    <div>
                      <span className="text-xs font-semibold text-zinc-200 block">RGB Gamer Glow Text</span>
                      <span className="text-[10px] text-zinc-400">Animated chromatic cycling for key labels</span>
                    </div>
                    <input
                      type="checkbox"
                      checked={config.rgbTextEnabled}
                      onChange={e => updateConfig({ rgbTextEnabled: e.target.checked })}
                      className="w-4 h-4 rounded accent-blue-500 cursor-pointer"
                    />
                  </div>
                </div>
              </div>

              {/* Wallpaper Background Settings */}
              <div className="space-y-3 border-t border-zinc-800 pt-4">
                <h4 className="text-xs font-bold text-zinc-300 uppercase tracking-wider flex items-center gap-1.5">
                  <ImageIcon className="w-3.5 h-3.5 text-blue-400" />
                  <span>Custom Keyboard Wallpaper</span>
                </h4>

                <div className="flex items-center gap-4">
                  <label className="flex items-center gap-2 px-3 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-xs font-medium cursor-pointer text-zinc-200 transition-colors">
                    <Upload className="w-4 h-4 text-blue-400" />
                    <span>Upload Image</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleWallpaperUpload}
                      className="hidden"
                    />
                  </label>

                  {draftTheme.wallpaperUrl && (
                    <button
                      type="button"
                      onClick={() => setDraftTheme(prev => ({ ...prev, wallpaperUrl: undefined }))}
                      className="text-xs text-rose-400 hover:underline cursor-pointer"
                    >
                      Remove wallpaper
                    </button>
                  )}
                </div>

                {draftTheme.wallpaperUrl && (
                  <div className="grid grid-cols-2 gap-4 pt-2">
                    <div>
                      <div className="flex justify-between text-xs text-zinc-300 mb-1">
                        <span>Background Dim</span>
                        <span className="font-mono text-blue-400">{Math.round((config.bgDimOpacity || 0.5) * 100)}%</span>
                      </div>
                      <input
                        type="range"
                        min="0"
                        max="1"
                        step="0.05"
                        value={config.bgDimOpacity || 0.5}
                        onChange={e => updateConfig({ bgDimOpacity: Number(e.target.value) })}
                        className="w-full accent-blue-500 cursor-pointer"
                      />
                    </div>
                    <div>
                      <div className="flex justify-between text-xs text-zinc-300 mb-1">
                        <span>Background Blur</span>
                        <span className="font-mono text-blue-400">{config.bgBlurRadius || 0}px</span>
                      </div>
                      <input
                        type="range"
                        min="0"
                        max="10"
                        value={config.bgBlurRadius || 0}
                        onChange={e => updateConfig({ bgBlurRadius: Number(e.target.value) })}
                        className="w-full accent-blue-500 cursor-pointer"
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* Save & Apply Button */}
              <div className="flex justify-end gap-3 pt-4 border-t border-zinc-800">
                <button
                  type="button"
                  onClick={() => setActiveTab('presets')}
                  className="px-4 py-2 rounded-lg text-xs font-semibold text-zinc-400 hover:text-white bg-zinc-800 hover:bg-zinc-700 cursor-pointer transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg text-xs font-semibold text-white bg-blue-600 hover:bg-blue-500 shadow-md shadow-blue-600/30 cursor-pointer transition-all active:scale-95"
                >
                  Save & Apply Theme
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
