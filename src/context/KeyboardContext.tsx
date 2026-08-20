import React, { createContext, useContext, useState, useEffect, useCallback, useMemo, useRef } from 'react';
import {
  KeyboardConfig,
  KeyboardLanguage,
  KeyboardLayoutMode,
  KeyboardTheme,
  ClipItem,
  SuggestionItem
} from '../types';
import { BUILTIN_THEMES } from '../data/themes';
import { SinglishParser } from '../engines/SinglishParser';
import { WijesekaraEngine } from '../engines/WijesekaraEngine';
import { InlineCalculator } from '../engines/InlineCalculator';
import { WordPredictionEngine } from '../engines/WordPredictionEngine';
import { AudioFeedback } from '../engines/AudioFeedback';

interface KeyboardContextType {
  editorText: string;
  setEditorText: React.Dispatch<React.SetStateAction<string>>;
  inputBuffer: string;
  setInputBuffer: React.Dispatch<React.SetStateAction<string>>;
  suggestions: SuggestionItem[];
  config: KeyboardConfig;
  updateConfig: (partial: Partial<KeyboardConfig>) => void;
  themes: KeyboardTheme[];
  activeTheme: KeyboardTheme;
  setActiveThemeId: (id: string) => void;
  saveCustomTheme: (theme: KeyboardTheme) => void;
  deleteCustomTheme: (themeId: string) => void;
  clips: ClipItem[];
  addClip: (value: string, isPinned?: boolean) => void;
  deleteClip: (id: string) => void;
  togglePinClip: (id: string) => void;
  exportClips: () => string;
  importClips: (jsonData: string) => boolean;
  shiftState: 'off' | 'shift' | 'caps_lock';
  toggleShift: () => void;
  layoutMode: KeyboardLayoutMode;
  setLayoutMode: (mode: KeyboardLayoutMode) => void;
  activeModal: string | null;
  setActiveModal: (modal: string | null) => void;
  handleKeyClick: (keyChar: string, isAction?: boolean) => void;
  handleBackspace: () => void;
  handleSpace: () => void;
  handleEnter: () => void;
  handleSuggestionSelect: (suggestion: SuggestionItem) => void;
  clearEditor: () => void;
  insertTextDirectly: (text: string) => void;
  addRecentEmoji: (emoji: string) => void;
  textareaRef: React.RefObject<HTMLTextAreaElement | null>;
  showKeyPreview: string | null;
  setShowKeyPreview: (key: string | null) => void;
}

const DEFAULT_CONFIG: KeyboardConfig = {
  keyboardLanguage: 'sinhala_singlish',
  vibrateOnKeypress: true,
  soundOnKeypress: 'modern',
  keypressSoundVolume: 45,
  showPopupOnKeypress: true,
  enableSentencesCapitalization: true,
  smartAutoCorrection: true,
  showEmojiKey: true,
  showLanguageSwitchKey: true,
  showNumbersRow: true,
  keyBordersEnabled: true,
  keyBorderWidth: 1,
  rgbTextEnabled: false,
  bgImageEnabled: false,
  bgDimOpacity: 0.5,
  bgBlurRadius: 0,
  keyTranslucencyAlpha: 95,
  customSpacebarText: 'xLakaBoardx',
  keyTextColor: '#FFFFFF',
  spacebarColor: '#0066FF',
  activeThemeId: 'builtin_default_dark',
  emojiStyle: 'whatsapp',
  recentlyUsedEmojis: ['❤️', '😂', '😊', '😏', '😒', '😌', '🥺', '🥲', '😮‍💨', '😁', '🔥', '👍'],
  numberRowPasswords: true,
  showCommaKey: true,
  showPeriodKey: true,
  fontScale: 100,
  emojiScale: 100,
  keyboardHeightPercentage: 100,
  suggestionStripEnabled: true,
  oneHandedMode: 'off',
  doubleSpacePeriod: true,
  autoSpacePunctuation: true,
  touchHoldSymbols: true,
  touchHoldDelay: 300,
  spellCheckEnabled: true,
  nextWordSuggestions: true,
};

const DEFAULT_CLIPS: ClipItem[] = [
  {
    id: 'clip-1',
    value: 'ආයුබෝවන්! මම Lakmal Keyboard භාවිතා කරමි.',
    isPinned: true,
    createdAt: Date.now() - 100000
  },
  {
    id: 'clip-2',
    value: 'ස්තූතියි, සුබ දවසක් වේවා!',
    isPinned: true,
    createdAt: Date.now() - 80000
  },
  {
    id: 'clip-3',
    value: 'Thank you for using Lakmal Keyboard for Sinhala and English typing!',
    isPinned: false,
    createdAt: Date.now() - 50000
  }
];

const KeyboardContext = createContext<KeyboardContextType | null>(null);

export const KeyboardProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  // Editor and Input Buffer State
  const [editorText, setEditorText] = useState<string>(
    'ආයුබෝවන්! Welcome to Lakmal Keyboard (xLakaBoardx).\nTry typing in Singlish (e.g. mama oyata adareyi) or English!'
  );
  const [inputBuffer, setInputBuffer] = useState<string>('');
  const [suggestions, setSuggestions] = useState<SuggestionItem[]>([]);

  // Shift & Layout State
  const [shiftState, setShiftState] = useState<'off' | 'shift' | 'caps_lock'>('off');
  const [layoutMode, setLayoutMode] = useState<KeyboardLayoutMode>('letters');
  const [activeModal, setActiveModal] = useState<string | null>(null);
  const [showKeyPreview, setShowKeyPreview] = useState<string | null>(null);

  // Config State
  const [config, setConfig] = useState<KeyboardConfig>(() => {
    try {
      const saved = localStorage.getItem('lakmal_keyboard_config');
      if (saved) {
        return { ...DEFAULT_CONFIG, ...JSON.parse(saved) };
      }
    } catch {
      // ignore
    }
    return DEFAULT_CONFIG;
  });

  // Themes State
  const [customThemes, setCustomThemes] = useState<KeyboardTheme[]>(() => {
    try {
      const saved = localStorage.getItem('lakmal_custom_themes');
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {
      // ignore
    }
    return [];
  });

  // Clips State
  const [clips, setClips] = useState<ClipItem[]>(() => {
    try {
      const saved = localStorage.getItem('lakmal_clips_data');
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {
      // ignore
    }
    return DEFAULT_CLIPS;
  });

  const predictionEngine = useMemo(() => WordPredictionEngine.getInstance(), []);

  // Save config changes
  const updateConfig = useCallback((partial: Partial<KeyboardConfig>) => {
    setConfig(prev => {
      const next = { ...prev, ...partial };
      try {
        localStorage.setItem('lakmal_keyboard_config', JSON.stringify(next));
      } catch {
        // ignore
      }
      return next;
    });
  }, []);

  // Save custom themes
  const saveCustomTheme = useCallback((theme: KeyboardTheme) => {
    setCustomThemes(prev => {
      const existing = prev.findIndex(t => t.id === theme.id);
      let updated: KeyboardTheme[];
      if (existing >= 0) {
        updated = [...prev];
        updated[existing] = theme;
      } else {
        updated = [...prev, theme];
      }
      try {
        localStorage.setItem('lakmal_custom_themes', JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
    updateConfig({ activeThemeId: theme.id });
  }, [updateConfig]);

  const deleteCustomTheme = useCallback((themeId: string) => {
    setCustomThemes(prev => {
      const updated = prev.filter(t => t.id !== themeId);
      try {
        localStorage.setItem('lakmal_custom_themes', JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
    if (config.activeThemeId === themeId) {
      updateConfig({ activeThemeId: 'builtin_default_dark' });
    }
  }, [config.activeThemeId, updateConfig]);

  const themes = useMemo(() => {
    return [...BUILTIN_THEMES, ...customThemes];
  }, [customThemes]);

  const activeTheme = useMemo(() => {
    const found = themes.find(t => t.id === config.activeThemeId);
    return found || BUILTIN_THEMES[0];
  }, [themes, config.activeThemeId]);

  const setActiveThemeId = useCallback((id: string) => {
    updateConfig({ activeThemeId: id });
  }, [updateConfig]);

  // Clips Management
  const addClip = useCallback((value: string, isPinned: boolean = false) => {
    if (!value.trim()) return;
    setClips(prev => {
      // Remove duplicate if exists
      const filtered = prev.filter(c => c.value !== value.trim());
      const newClip: ClipItem = {
        id: `clip-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
        value: value.trim(),
        isPinned,
        createdAt: Date.now()
      };
      const updated = [newClip, ...filtered];
      try {
        localStorage.setItem('lakmal_clips_data', JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
  }, []);

  const deleteClip = useCallback((id: string) => {
    setClips(prev => {
      const updated = prev.filter(c => c.id !== id);
      try {
        localStorage.setItem('lakmal_clips_data', JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
  }, []);

  const togglePinClip = useCallback((id: string) => {
    setClips(prev => {
      const updated = prev.map(c => c.id === id ? { ...c, isPinned: !c.isPinned } : c);
      try {
        localStorage.setItem('lakmal_clips_data', JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
  }, []);

  const exportClips = useCallback(() => {
    return JSON.stringify(clips, null, 2);
  }, [clips]);

  const importClips = useCallback((jsonData: string): boolean => {
    try {
      const parsed = JSON.parse(jsonData);
      if (Array.isArray(parsed)) {
        setClips(parsed);
        localStorage.setItem('lakmal_clips_data', JSON.stringify(parsed));
        return true;
      }
    } catch {
      // ignore
    }
    return false;
  }, []);

  const addRecentEmoji = useCallback((emoji: string) => {
    setConfig(prev => {
      const filtered = prev.recentlyUsedEmojis.filter(e => e !== emoji);
      const updated = [emoji, ...filtered].slice(0, 24);
      const next = { ...prev, recentlyUsedEmojis: updated };
      try {
        localStorage.setItem('lakmal_keyboard_config', JSON.stringify(next));
      } catch {
        // ignore
      }
      return next;
    });
  }, []);

  // Update suggestions whenever inputBuffer or editor state changes
  useEffect(() => {
    if (!config.suggestionStripEnabled) {
      setSuggestions([]);
      return;
    }

    if (!inputBuffer) {
      setSuggestions([]);
      return;
    }

    // 1. Check for Inline Calculator first if ending with =
    const calcResult = InlineCalculator.calculate(inputBuffer);
    if (calcResult !== null) {
      setSuggestions([
        {
          text: calcResult,
          isSinhala: false,
          isPrimary: true,
          isCalculation: true
        },
        {
          text: inputBuffer,
          isSinhala: false,
          isPrimary: false
        }
      ]);
      return;
    }

    // 2. Generate predictive suggestions based on active language
    const isSinglish = config.keyboardLanguage === 'sinhala_singlish';
    const isWijesekara = config.keyboardLanguage === 'sinhala_wijesekara';

    if (isWijesekara) {
      // In Wijesekara mode, input is already direct Sinhala
      setSuggestions([
        {
          text: inputBuffer,
          isSinhala: true,
          isPrimary: true,
          isExactMatch: true
        }
      ]);
      return;
    }

    const predicted = predictionEngine.getSuggestions(inputBuffer, isSinglish);

    // 3. Auto-correction suggestion check
    if (config.smartAutoCorrection) {
      const autoCorrect = predictionEngine.getAutoCorrection(inputBuffer, isSinglish);
      if (autoCorrect && !predicted.some(p => p.text === autoCorrect)) {
        predicted.unshift({
          text: autoCorrect,
          isSinhala: isSinglish,
          isPrimary: true,
          isCorrection: true
        });
      }
    }

    setSuggestions(predicted);
  }, [inputBuffer, config.suggestionStripEnabled, config.keyboardLanguage, config.smartAutoCorrection, predictionEngine]);

  const playFeedback = useCallback(() => {
    if (config.soundOnKeypress !== 'none') {
      AudioFeedback.playKeypressSound(config.soundOnKeypress, config.keypressSoundVolume);
    }
    if (config.vibrateOnKeypress) {
      AudioFeedback.triggerHaptic(18);
    }
  }, [config.soundOnKeypress, config.keypressSoundVolume, config.vibrateOnKeypress]);

  const toggleShift = useCallback(() => {
    playFeedback();
    setShiftState(prev => {
      if (prev === 'off') return 'shift';
      if (prev === 'shift') return 'caps_lock';
      return 'off';
    });
  }, [playFeedback]);

  const insertTextDirectly = useCallback((text: string) => {
    setEditorText(prev => prev + text);
    setInputBuffer('');
    if (textareaRef.current) {
      textareaRef.current.scrollTop = textareaRef.current.scrollHeight;
    }
  }, []);

  const handleKeyClick = useCallback((rawChar: string, isAction: boolean = false) => {
    playFeedback();

    if (config.showPopupOnKeypress) {
      setShowKeyPreview(rawChar);
      setTimeout(() => setShowKeyPreview(null), 300);
    }

    const isSinglish = config.keyboardLanguage === 'sinhala_singlish';
    const isWijesekara = config.keyboardLanguage === 'sinhala_wijesekara';

    let resolvedChar = rawChar;
    if (shiftState === 'shift' || shiftState === 'caps_lock') {
      resolvedChar = rawChar.toUpperCase();
    } else {
      resolvedChar = rawChar.toLowerCase();
    }

    if (isWijesekara) {
      // Direct Wijesekara mapping
      const mapped = WijesekaraEngine.getSinhalaChar(shiftState !== 'off' ? rawChar.toUpperCase() : rawChar);
      setEditorText(prev => prev + mapped);
      setInputBuffer(prev => prev + mapped);
    } else if (isSinglish) {
      // Accumulate buffer for real-time phonetics
      setInputBuffer(prev => {
        const nextBuf = prev + resolvedChar;
        return nextBuf;
      });
    } else {
      // Standard English
      setEditorText(prev => prev + resolvedChar);
      setInputBuffer(prev => prev + resolvedChar);
    }

    // Single-use shift automatically resets to off
    if (shiftState === 'shift') {
      setShiftState('off');
    }
  }, [playFeedback, config.showPopupOnKeypress, config.keyboardLanguage, shiftState]);

  const handleBackspace = useCallback(() => {
    playFeedback();

    if (inputBuffer.length > 0) {
      setInputBuffer(prev => prev.slice(0, -1));
    } else {
      setEditorText(prev => prev.slice(0, -1));
    }
  }, [playFeedback, inputBuffer]);

  const handleSpace = useCallback(() => {
    playFeedback();

    const isSinglish = config.keyboardLanguage === 'sinhala_singlish';

    if (inputBuffer.length > 0) {
      let committedText = inputBuffer;
      if (isSinglish) {
        // Parse the whole buffer into Sinhala
        const autoCorrect = config.smartAutoCorrection ? predictionEngine.getAutoCorrection(inputBuffer, true) : null;
        committedText = autoCorrect || SinglishParser.parse(inputBuffer);
        predictionEngine.learnWord(committedText, true);
      } else {
        const autoCorrect = config.smartAutoCorrection ? predictionEngine.getAutoCorrection(inputBuffer, false) : null;
        committedText = autoCorrect || inputBuffer;
        predictionEngine.learnWord(committedText, false);
      }

      setEditorText(prev => prev + committedText + ' ');
      setInputBuffer('');
    } else {
      setEditorText(prev => {
        // Double space for period feature
        if (config.doubleSpacePeriod && prev.endsWith(' ') && !prev.endsWith('. ') && prev.length >= 2) {
          return prev.slice(0, -1) + '. ';
        }
        return prev + ' ';
      });
    }
  }, [playFeedback, config.keyboardLanguage, config.smartAutoCorrection, config.doubleSpacePeriod, inputBuffer, predictionEngine]);

  const handleEnter = useCallback(() => {
    playFeedback();

    if (inputBuffer.length > 0) {
      const isSinglish = config.keyboardLanguage === 'sinhala_singlish';
      const committed = isSinglish ? SinglishParser.parse(inputBuffer) : inputBuffer;
      predictionEngine.learnWord(committed, isSinglish);
      setEditorText(prev => prev + committed + '\n');
      setInputBuffer('');
    } else {
      setEditorText(prev => prev + '\n');
    }
  }, [playFeedback, inputBuffer, config.keyboardLanguage, predictionEngine]);

  const handleSuggestionSelect = useCallback((suggestion: SuggestionItem) => {
    playFeedback();
    const isSinhala = suggestion.isSinhala;
    predictionEngine.learnWord(suggestion.text, isSinhala);

    setEditorText(prev => prev + suggestion.text + ' ');
    setInputBuffer('');
  }, [playFeedback, predictionEngine]);

  const clearEditor = useCallback(() => {
    setEditorText('');
    setInputBuffer('');
  }, []);

  return (
    <KeyboardContext.Provider
      value={{
        editorText,
        setEditorText,
        inputBuffer,
        setInputBuffer,
        suggestions,
        config,
        updateConfig,
        themes,
        activeTheme,
        setActiveThemeId,
        saveCustomTheme,
        deleteCustomTheme,
        clips,
        addClip,
        deleteClip,
        togglePinClip,
        exportClips,
        importClips,
        shiftState,
        toggleShift,
        layoutMode,
        setLayoutMode,
        activeModal,
        setActiveModal,
        handleKeyClick,
        handleBackspace,
        handleSpace,
        handleEnter,
        handleSuggestionSelect,
        clearEditor,
        insertTextDirectly,
        addRecentEmoji,
        textareaRef,
        showKeyPreview,
        setShowKeyPreview,
      }}
    >
      {children}
    </KeyboardContext.Provider>
  );
};

export const useKeyboard = (): KeyboardContextType => {
  const context = useContext(KeyboardContext);
  if (!context) {
    throw new Error('useKeyboard must be used within a KeyboardProvider');
  }
  return context;
};
