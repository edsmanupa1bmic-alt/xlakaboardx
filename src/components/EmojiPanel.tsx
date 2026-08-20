import React, { useState, useMemo } from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import { EMOJI_CATEGORIES } from '../data/emojiData';
import {
  Smile,
  User,
  PawPrint,
  Utensils,
  Plane,
  Trophy,
  Lightbulb,
  Hash,
  Flag,
  Clock,
  Delete,
  Search,
  Keyboard as KeyboardIcon
} from 'lucide-react';
import { EmojiStyleType } from '../types';

export const EmojiPanel: React.FC = () => {
  const {
    activeTheme,
    config,
    updateConfig,
    insertTextDirectly,
    addRecentEmoji,
    handleBackspace,
    setLayoutMode
  } = useKeyboard();

  const [activeCategory, setActiveCategory] = useState<string>('smileys_emotion');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const categoryIcons: Record<string, React.ReactNode> = {
    smileys_emotion: <Smile className="w-4 h-4" />,
    people_body: <User className="w-4 h-4" />,
    animals_nature: <PawPrint className="w-4 h-4" />,
    food_drink: <Utensils className="w-4 h-4" />,
    travel_places: <Plane className="w-4 h-4" />,
    activities: <Trophy className="w-4 h-4" />,
    objects: <Lightbulb className="w-4 h-4" />,
    symbols: <Hash className="w-4 h-4" />,
    flags: <Flag className="w-4 h-4" />,
    recent: <Clock className="w-4 h-4" />
  };

  const handleEmojiClick = (emoji: string) => {
    insertTextDirectly(emoji);
    addRecentEmoji(emoji);
  };

  const displayedEmojis = useMemo(() => {
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      // Flatten all emojis and filter
      const all: string[] = [];
      EMOJI_CATEGORIES.forEach(cat => {
        cat.emojis.forEach(e => all.push(e));
      });
      return all.filter(e => e.includes(q)).slice(0, 80);
    }

    if (activeCategory === 'recent') {
      return config.recentlyUsedEmojis || [];
    }

    const cat = EMOJI_CATEGORIES.find(c => c.id === activeCategory);
    return cat ? cat.emojis : [];
  }, [activeCategory, searchQuery, config.recentlyUsedEmojis]);

  const emojiFontClass =
    config.emojiStyle === 'whatsapp'
      ? 'font-sans'
      : config.emojiStyle === 'apple'
      ? 'font-serif'
      : 'font-sans';

  return (
    <div
      className="w-full flex flex-col select-none relative overflow-hidden"
      style={{
        backgroundColor: activeTheme.keyboardBgColor,
        color: activeTheme.keyTextColor,
        height: '270px'
      }}
    >
      {/* Category Tabs Header */}
      <div
        className="flex items-center justify-between px-2 py-1 border-b overflow-x-auto no-scrollbar gap-1"
        style={{
          backgroundColor: activeTheme.smartbarBgColor,
          borderColor: activeTheme.strokeColor
        }}
      >
        <button
          onClick={() => setActiveCategory('recent')}
          className={`p-2 rounded-lg transition-colors cursor-pointer ${
            activeCategory === 'recent'
              ? 'bg-white/20 text-white font-bold'
              : 'text-zinc-400 hover:text-white hover:bg-white/10'
          }`}
          title="Recently Used"
        >
          {categoryIcons.recent}
        </button>

        {EMOJI_CATEGORIES.map(cat => (
          <button
            key={cat.id}
            onClick={() => {
              setActiveCategory(cat.id);
              setSearchQuery('');
            }}
            className={`p-2 rounded-lg transition-colors cursor-pointer shrink-0 ${
              activeCategory === cat.id && !searchQuery
                ? 'bg-white/20 text-white font-bold shadow-xs'
                : 'text-zinc-400 hover:text-white hover:bg-white/10'
            }`}
            title={cat.name}
          >
            {categoryIcons[cat.id]}
          </button>
        ))}
      </div>

      {/* Fast search strip */}
      <div className="flex items-center gap-2 px-3 py-1.5 bg-black/10 border-b border-white/5">
        <div className="relative flex-1 flex items-center">
          <Search className="w-3.5 h-3.5 absolute left-2.5 text-zinc-400" />
          <input
            type="text"
            placeholder="Search emojis..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="w-full pl-8 pr-2 py-1 text-xs rounded-md bg-white/10 text-white placeholder-zinc-400 outline-hidden focus:ring-1 focus:ring-blue-500 border border-white/10"
          />
        </div>

        {/* Emoji Style Picker pill */}
        <select
          value={config.emojiStyle}
          onChange={e => updateConfig({ emojiStyle: e.target.value as EmojiStyleType })}
          className="text-[11px] px-2 py-1 rounded bg-white/10 text-zinc-200 border border-white/10 outline-hidden cursor-pointer"
          title="Select Emoji Style"
        >
          <option value="whatsapp" className="bg-zinc-900 text-white">WhatsApp Style</option>
          <option value="apple" className="bg-zinc-900 text-white">Apple iOS Style</option>
          <option value="google" className="bg-zinc-900 text-white">Google Android</option>
          <option value="system" className="bg-zinc-900 text-white">System Default</option>
        </select>
      </div>

      {/* Emoji Grid */}
      <div className="flex-1 overflow-y-auto p-2 grid grid-cols-7 sm:grid-cols-9 md:grid-cols-11 gap-1.5 content-start">
        {displayedEmojis.length > 0 ? (
          displayedEmojis.map((emoji, index) => (
            <button
              key={`${emoji}-${index}`}
              onClick={() => handleEmojiClick(emoji)}
              className={`text-2xl sm:text-3xl p-1.5 rounded-lg hover:bg-white/15 transition-transform active:scale-125 cursor-pointer flex items-center justify-center ${emojiFontClass}`}
              style={{
                fontSize: `${config.emojiScale || 100}%`
              }}
            >
              {emoji}
            </button>
          ))
        ) : (
          <div className="col-span-full py-8 text-center text-xs text-zinc-400">
            No emojis found for "{searchQuery}"
          </div>
        )}
      </div>

      {/* Bottom Bar: Back to Keyboard & Backspace */}
      <div
        className="flex items-center justify-between px-3 py-1.5 border-t"
        style={{
          backgroundColor: activeTheme.smartbarBgColor,
          borderColor: activeTheme.strokeColor
        }}
      >
        <button
          onClick={() => setLayoutMode('letters')}
          className="flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-semibold hover:bg-white/15 transition-colors cursor-pointer"
          style={{
            backgroundColor: activeTheme.keyBgColor,
            color: activeTheme.keyTextColor,
            border: `1px solid ${activeTheme.strokeColor}`
          }}
        >
          <KeyboardIcon className="w-3.5 h-3.5" />
          <span>ABC</span>
        </button>

        <span className="text-[11px] opacity-60">
          {activeCategory === 'recent'
            ? 'Recently Used Emojis'
            : EMOJI_CATEGORIES.find(c => c.id === activeCategory)?.name || 'Emojis'}
        </span>

        <button
          onClick={handleBackspace}
          className="p-1.5 px-3 rounded-md hover:bg-white/15 transition-colors cursor-pointer"
          style={{
            backgroundColor: activeTheme.keyBgColor,
            color: activeTheme.keyTextColor,
            border: `1px solid ${activeTheme.strokeColor}`
          }}
          title="Backspace"
        >
          <Delete className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
