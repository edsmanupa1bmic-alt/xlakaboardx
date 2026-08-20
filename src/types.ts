export type KeyboardLanguage =
  | 'sinhala_singlish'
  | 'sinhala_wijesekara'
  | 'english_qwerty';

export type KeyboardLayoutMode =
  | 'letters'
  | 'symbols'
  | 'symbols_alt'
  | 'phone'
  | 'emoji'
  | 'clips';

export type OneHandedMode = 'off' | 'left' | 'right';

export type SoundEffectType = 'system' | 'modern' | 'typewriter' | 'soft' | 'none';

export type EmojiStyleType = 'whatsapp' | 'apple' | 'google' | 'system';

export interface SuggestionItem {
  text: string;
  isSinhala: boolean;
  isPrimary?: boolean;
  isExactMatch?: boolean;
  isCalculation?: boolean;
  isCorrection?: boolean;
}

export interface ClipItem {
  id: string;
  value: string;
  isPinned: boolean;
  createdAt: number;
}

export interface KeyboardTheme {
  id: string;
  name: string;
  author: string;
  isBuiltIn: boolean;
  isNight: boolean;
  keyboardBgColor: string;
  keyBgColor: string;
  keyBgPressedColor: string;
  keyTextColor: string;
  accentKeyBgColor: string;
  accentKeyTextColor: string;
  smartbarBgColor: string;
  smartbarTextColor: string;
  strokeColor: string;
  strokeWidth: number;
  cornerRadius: number;
  wallpaperUrl?: string;
  bgDimOpacity?: number;
  keyTranslucencyAlpha?: number;
  rgbTextEnabled?: boolean;
}

export interface KeyboardConfig {
  keyboardLanguage: KeyboardLanguage;
  vibrateOnKeypress: boolean;
  soundOnKeypress: SoundEffectType;
  keypressSoundVolume: number;
  showPopupOnKeypress: boolean;
  enableSentencesCapitalization: boolean;
  smartAutoCorrection: boolean;
  showEmojiKey: boolean;
  showLanguageSwitchKey: boolean;
  showNumbersRow: boolean;
  keyBordersEnabled: boolean;
  keyBorderWidth: number;
  rgbTextEnabled: boolean;
  bgImageEnabled: boolean;
  bgDimOpacity: number;
  bgBlurRadius: number;
  keyTranslucencyAlpha: number;
  customSpacebarText: string;
  keyTextColor: string;
  spacebarColor: string;
  activeThemeId: string;
  emojiStyle: EmojiStyleType;
  recentlyUsedEmojis: string[];
  numberRowPasswords: boolean;
  showCommaKey: boolean;
  showPeriodKey: boolean;
  fontScale: number;
  emojiScale: number;
  keyboardHeightPercentage: number;
  suggestionStripEnabled: boolean;
  oneHandedMode: OneHandedMode;
  doubleSpacePeriod: boolean;
  autoSpacePunctuation: boolean;
  touchHoldSymbols: boolean;
  touchHoldDelay: number;
  spellCheckEnabled: boolean;
  nextWordSuggestions: boolean;
}
