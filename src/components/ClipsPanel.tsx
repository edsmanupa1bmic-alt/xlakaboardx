import React, { useState } from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import {
  Pin,
  Trash2,
  Plus,
  Copy,
  Download,
  Upload,
  Search,
  Keyboard as KeyboardIcon,
  Check
} from 'lucide-react';

export const ClipsPanel: React.FC = () => {
  const {
    activeTheme,
    clips,
    addClip,
    deleteClip,
    togglePinClip,
    exportClips,
    importClips,
    insertTextDirectly,
    setLayoutMode
  } = useKeyboard();

  const [searchQuery, setSearchQuery] = useState('');
  const [newClipText, setNewClipText] = useState('');
  const [isAdding, setIsAdding] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [importNotice, setImportNotice] = useState<string | null>(null);

  const handleAddNew = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newClipText.trim()) return;
    addClip(newClipText.trim(), false);
    setNewClipText('');
    setIsAdding(false);
  };

  const handleCopy = (id: string, text: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text);
    }
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleExport = () => {
    const data = exportClips();
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `lakmal_keyboard_clips_${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = event => {
      const content = event.target?.result as string;
      const success = importClips(content);
      if (success) {
        setImportNotice('Clips imported successfully!');
      } else {
        setImportNotice('Failed to parse clips JSON.');
      }
      setTimeout(() => setImportNotice(null), 3000);
    };
    reader.readAsText(file);
  };

  const filteredClips = clips.filter(clip =>
    clip.value.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const pinnedClips = filteredClips.filter(c => c.isPinned);
  const unpinnedClips = filteredClips.filter(c => !c.isPinned);

  return (
    <div
      className="w-full flex flex-col select-none relative overflow-hidden"
      style={{
        backgroundColor: activeTheme.keyboardBgColor,
        color: activeTheme.keyTextColor,
        height: '270px'
      }}
    >
      {/* Header bar */}
      <div
        className="flex items-center justify-between px-3 py-1.5 border-b gap-2"
        style={{
          backgroundColor: activeTheme.smartbarBgColor,
          borderColor: activeTheme.strokeColor
        }}
      >
        <div className="flex items-center gap-2 flex-1">
          <button
            onClick={() => setLayoutMode('letters')}
            className="flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold hover:bg-white/15 transition-colors cursor-pointer"
            style={{
              backgroundColor: activeTheme.keyBgColor,
              color: activeTheme.keyTextColor,
              border: `1px solid ${activeTheme.strokeColor}`
            }}
          >
            <KeyboardIcon className="w-3.5 h-3.5" />
            <span>ABC</span>
          </button>
          <span className="text-xs font-bold tracking-wide">Clipboard Manager</span>
        </div>

        {/* Action icons: Add clip, Export, Import */}
        <div className="flex items-center gap-1">
          <button
            onClick={() => setIsAdding(prev => !prev)}
            className="flex items-center gap-1 px-2 py-1 rounded bg-blue-600 hover:bg-blue-500 text-white text-xs font-medium cursor-pointer transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add</span>
          </button>

          <button
            onClick={handleExport}
            className="p-1.5 rounded hover:bg-white/10 text-zinc-300 hover:text-white transition-colors cursor-pointer"
            title="Export Clips to JSON"
          >
            <Download className="w-3.5 h-3.5" />
          </button>

          <label
            className="p-1.5 rounded hover:bg-white/10 text-zinc-300 hover:text-white transition-colors cursor-pointer"
            title="Import Clips from JSON"
          >
            <Upload className="w-3.5 h-3.5" />
            <input
              type="file"
              accept=".json,application/json"
              onChange={handleImportFile}
              className="hidden"
            />
          </label>
        </div>
      </div>

      {/* Add New Clip Form if open */}
      {isAdding && (
        <form onSubmit={handleAddNew} className="p-2 bg-black/25 border-b border-white/10 flex gap-2">
          <input
            type="text"
            placeholder="Type or paste clip text..."
            value={newClipText}
            onChange={e => setNewClipText(e.target.value)}
            autoFocus
            className="flex-1 px-2.5 py-1 text-xs rounded bg-white/10 text-white placeholder-zinc-400 outline-hidden border border-white/15 focus:ring-1 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium rounded cursor-pointer"
          >
            Save
          </button>
          <button
            type="button"
            onClick={() => setIsAdding(false)}
            className="px-2 py-1 bg-zinc-700 hover:bg-zinc-600 text-white text-xs rounded cursor-pointer"
          >
            Cancel
          </button>
        </form>
      )}

      {/* Search Input & Status notice */}
      <div className="px-3 py-1 bg-black/10 flex items-center gap-2 border-b border-white/5">
        <Search className="w-3.5 h-3.5 text-zinc-400 shrink-0" />
        <input
          type="text"
          placeholder="Filter saved clips..."
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          className="w-full text-xs bg-transparent text-white placeholder-zinc-400 outline-hidden py-0.5"
        />
        {importNotice && (
          <span className="text-[10px] text-emerald-400 whitespace-nowrap">{importNotice}</span>
        )}
      </div>

      {/* Clips List */}
      <div className="flex-1 overflow-y-auto p-2 space-y-1.5">
        {pinnedClips.length > 0 && (
          <div className="space-y-1">
            <div className="text-[10px] uppercase tracking-wider text-amber-400/80 font-bold px-1 flex items-center gap-1">
              <Pin className="w-2.5 h-2.5" /> Pinned Items
            </div>
            {pinnedClips.map(clip => (
              <div
                key={clip.id}
                onClick={() => insertTextDirectly(clip.value)}
                className="group flex items-center justify-between p-2 rounded-lg transition-all hover:bg-white/15 cursor-pointer shadow-xs border"
                style={{
                  backgroundColor: activeTheme.keyBgColor,
                  borderColor: 'rgba(245, 158, 11, 0.4)'
                }}
              >
                <p className="text-xs truncate flex-1 pr-2 font-medium" style={{ color: activeTheme.keyTextColor }}>
                  {clip.value}
                </p>
                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={e => handleCopy(clip.id, clip.value, e)}
                    className="p-1 rounded hover:bg-white/20 text-zinc-400 hover:text-white"
                    title="Copy to clipboard"
                  >
                    {copiedId === clip.id ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  </button>
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      togglePinClip(clip.id);
                    }}
                    className="p-1 rounded hover:bg-white/20 text-amber-400 hover:text-amber-300"
                    title="Unpin"
                  >
                    <Pin className="w-3.5 h-3.5 fill-amber-400" />
                  </button>
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      deleteClip(clip.id);
                    }}
                    className="p-1 rounded hover:bg-rose-500/20 text-rose-400 hover:text-rose-300"
                    title="Delete"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {unpinnedClips.length > 0 && (
          <div className="space-y-1">
            {pinnedClips.length > 0 && (
              <div className="text-[10px] uppercase tracking-wider text-zinc-400 font-bold px-1 pt-1">
                Recent Clips
              </div>
            )}
            {unpinnedClips.map(clip => (
              <div
                key={clip.id}
                onClick={() => insertTextDirectly(clip.value)}
                className="group flex items-center justify-between p-2 rounded-lg transition-all hover:bg-white/15 cursor-pointer shadow-xs border"
                style={{
                  backgroundColor: activeTheme.keyBgColor,
                  borderColor: activeTheme.strokeColor
                }}
              >
                <p className="text-xs truncate flex-1 pr-2 font-medium" style={{ color: activeTheme.keyTextColor }}>
                  {clip.value}
                </p>
                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={e => handleCopy(clip.id, clip.value, e)}
                    className="p-1 rounded hover:bg-white/20 text-zinc-400 hover:text-white"
                    title="Copy to clipboard"
                  >
                    {copiedId === clip.id ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  </button>
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      togglePinClip(clip.id);
                    }}
                    className="p-1 rounded hover:bg-white/20 text-zinc-400 hover:text-amber-400"
                    title="Pin"
                  >
                    <Pin className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      deleteClip(clip.id);
                    }}
                    className="p-1 rounded hover:bg-rose-500/20 text-zinc-400 hover:text-rose-400"
                    title="Delete"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {filteredClips.length === 0 && (
          <div className="py-8 text-center text-xs text-zinc-400">
            {searchQuery ? `No clips matching "${searchQuery}"` : 'Clipboard is empty. Add quick snippets with + Add!'}
          </div>
        )}
      </div>
    </div>
  );
};
