import React from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import { Smartbar } from './Smartbar';
import { KeyView } from './KeyView';
import { EmojiPanel } from './EmojiPanel';
import { ClipsPanel } from './ClipsPanel';
import {
  ArrowBigUp,
  Delete,
  CornerDownLeft,
  Globe,
  Smile,
  ChevronLeft,
  ChevronRight,
  Maximize2
} from 'lucide-react';
import { WijesekaraEngine } from '../engines/WijesekaraEngine';

export const KeyboardRoot: React.FC = () => {
  const {
    activeTheme,
    config,
    updateConfig,
    layoutMode,
    setLayoutMode,
    shiftState,
    toggleShift,
    handleBackspace,
    handleSpace,
    handleEnter,
    handleKeyClick
  } = useKeyboard();

  const isSinglish = config.keyboardLanguage === 'sinhala_singlish';
  const isWijesekara = config.keyboardLanguage === 'sinhala_wijesekara';
  const isEnglish = config.keyboardLanguage === 'english_qwerty';

  const isShifted = shiftState !== 'off';

  // Toggle Language
  const toggleLanguage = () => {
    if (isSinglish) updateConfig({ keyboardLanguage: 'sinhala_wijesekara' });
    else if (isWijesekara) updateConfig({ keyboardLanguage: 'english_qwerty' });
    else updateConfig({ keyboardLanguage: 'sinhala_singlish' });
  };

  // Keyboard scale height
  const heightMultiplier = (config.keyboardHeightPercentage || 100) / 100;

  // Render Sub-panel if in Emoji or Clips mode
  if (layoutMode === 'emoji') {
    return (
      <div className="w-full shadow-2xl rounded-t-xl overflow-hidden border-t" style={{ borderColor: activeTheme.strokeColor }}>
        <Smartbar />
        <EmojiPanel />
      </div>
    );
  }

  if (layoutMode === 'clips') {
    return (
      <div className="w-full shadow-2xl rounded-t-xl overflow-hidden border-t" style={{ borderColor: activeTheme.strokeColor }}>
        <Smartbar />
        <ClipsPanel />
      </div>
    );
  }

  // Row Key Definitions
  const numberRow = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'];

  const letterRow1 = ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'];
  const letterRow2 = ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'];
  const letterRow3 = ['z', 'x', 'c', 'v', 'b', 'n', 'm'];

  // Symbols pages
  const symbolsRow1 = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'];
  const symbolsRow2 = ['@', '#', '$', '%', '&', '-', '+', '(', ')', '/'];
  const symbolsRow3 = ['*', '"', "'", ':', ';', '!', '?', '_'];

  const altSymbolsRow1 = ['~', '`', '|', '•', '√', 'π', '÷', '×', '¶', '∆'];
  const altSymbolsRow2 = ['£', '¢', '€', '¥', '^', '°', '=', '{', '}', '\\'];
  const altSymbolsRow3 = ['%', '©', '®', '™', '[', ']', '<', '>'];

  // Phone keypad
  const phoneRows = [
    ['1', '2', '3'],
    ['4', '5', '6'],
    ['7', '8', '9'],
    ['*', '0', '#'],
    ['+', ' ', 'Back']
  ];

  // Helper to resolve display label on keys
  const getLetterDisplay = (rawChar: string): { label: string; sub?: string } => {
    if (isWijesekara) {
      const charToUse = isShifted ? rawChar.toUpperCase() : rawChar.toLowerCase();
      const mapped = WijesekaraEngine.getSinhalaChar(charToUse);
      return { label: mapped, sub: rawChar.toUpperCase() };
    }

    const label = isShifted ? rawChar.toUpperCase() : rawChar.toLowerCase();
    const sub = isSinglish ? getSinglishSub(rawChar) : undefined;
    return { label, sub };
  };

  const getSinglishSub = (char: string): string => {
    switch (char.toLowerCase()) {
      case 'k': return 'ක';
      case 'g': return 'ග';
      case 't': return 'ට';
      case 'd': return 'ඩ';
      case 'n': return 'න';
      case 'p': return 'ප';
      case 'b': return 'බ';
      case 'm': return 'ම';
      case 'y': return 'ය';
      case 'r': return 'ර';
      case 'l': return 'ල';
      case 'w': return 'ව';
      case 's': return 'ස';
      case 'h': return 'හ';
      case 'a': return 'අ';
      case 'i': return 'ඉ';
      case 'u': return 'උ';
      case 'e': return 'එ';
      case 'o': return 'ඔ';
      default: return '';
    }
  };

  return (
    <div
      className="w-full select-none relative overflow-hidden transition-all duration-150 border-t shadow-2xl"
      style={{
        backgroundColor: activeTheme.keyboardBgColor,
        borderColor: activeTheme.strokeColor,
        transform: `scaleY(${heightMultiplier})`,
        transformOrigin: 'bottom'
      }}
    >
      {/* Optional Custom Wallpaper background */}
      {config.bgImageEnabled && activeTheme.wallpaperUrl && (
        <div
          className="absolute inset-0 bg-cover bg-center pointer-events-none z-0"
          style={{
            backgroundImage: `url(${activeTheme.wallpaperUrl})`,
            filter: `blur(${config.bgBlurRadius || 0}px)`
          }}
        >
          <div
            className="w-full h-full"
            style={{
              backgroundColor: activeTheme.isNight ? '#000000' : '#FFFFFF',
              opacity: config.bgDimOpacity || 0.5
            }}
          />
        </div>
      )}

      {/* Smartbar Suggestion Strip */}
      <div className="relative z-10">
        <Smartbar />
      </div>

      {/* Main Keys Container (Supports One-Handed Mode) */}
      <div className="relative z-10 p-1 flex items-center justify-center">
        {/* Left Side Expansion / Swap controls if in One-Handed Right mode */}
        {config.oneHandedMode === 'right' && (
          <div className="flex flex-col gap-2 p-1 border-r border-white/10 shrink-0">
            <button
              onClick={() => updateConfig({ oneHandedMode: 'left' })}
              className="p-2 rounded bg-white/10 hover:bg-white/20 text-white cursor-pointer"
              title="Switch to Left Hand"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => updateConfig({ oneHandedMode: 'off' })}
              className="p-2 rounded bg-white/10 hover:bg-white/20 text-white cursor-pointer"
              title="Full Width Keyboard"
            >
              <Maximize2 className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Center Keyboard Grid */}
        <div
          className={`flex-1 flex flex-col items-center justify-center transition-all ${
            config.oneHandedMode === 'left'
              ? 'max-w-[85%] mr-auto'
              : config.oneHandedMode === 'right'
              ? 'max-w-[85%] ml-auto'
              : 'w-full max-w-3xl mx-auto'
          }`}
        >
          {/* 1. Optional Numbers Row */}
          {config.showNumbersRow && layoutMode === 'letters' && (
            <div className="w-full flex justify-center">
              {numberRow.map(num => (
                <KeyView key={num} label={num} />
              ))}
            </div>
          )}

          {/* 2. Letters Mode */}
          {layoutMode === 'letters' && (
            <>
              {/* Row 1 */}
              <div className="w-full flex justify-center">
                {letterRow1.map(char => {
                  const { label, sub } = getLetterDisplay(char);
                  return (
                    <KeyView
                      key={char}
                      label={char}
                      displayLabel={label}
                      subLabel={sub}
                    />
                  );
                })}
              </div>

              {/* Row 2 */}
              <div className="w-full flex justify-center px-2">
                {letterRow2.map(char => {
                  const { label, sub } = getLetterDisplay(char);
                  return (
                    <KeyView
                      key={char}
                      label={char}
                      displayLabel={label}
                      subLabel={sub}
                    />
                  );
                })}
                {isWijesekara && (
                  <KeyView
                    label=";"
                    displayLabel={isShifted ? 'ථ' : 'ත'}
                    subLabel=";"
                  />
                )}
              </div>

              {/* Row 3: Shift + letters + Backspace */}
              <div className="w-full flex justify-center">
                {/* Shift Key */}
                <KeyView
                  label="shift"
                  flexGrow={1.4}
                  isAction={shiftState !== 'off'}
                  onClick={toggleShift}
                  icon={
                    <div className="flex flex-col items-center">
                      <ArrowBigUp
                        className={`w-5 h-5 ${
                          shiftState === 'caps_lock'
                            ? 'fill-current text-blue-400'
                            : shiftState === 'shift'
                            ? 'fill-current'
                            : 'opacity-70'
                        }`}
                      />
                      {shiftState === 'caps_lock' && (
                        <div className="w-3 h-0.5 bg-blue-400 mt-0.5 rounded-full" />
                      )}
                    </div>
                  }
                />

                {letterRow3.map(char => {
                  const { label, sub } = getLetterDisplay(char);
                  return (
                    <KeyView
                      key={char}
                      label={char}
                      displayLabel={label}
                      subLabel={sub}
                    />
                  );
                })}

                {isWijesekara && (
                  <>
                    <KeyView
                      label=","
                      displayLabel={isShifted ? 'ළ' : 'ල'}
                      subLabel=","
                    />
                    <KeyView
                      label="."
                      displayLabel={isShifted ? 'ඝ' : 'ග'}
                      subLabel="."
                    />
                  </>
                )}

                {/* Backspace Key */}
                <KeyView
                  label="backspace"
                  flexGrow={1.4}
                  onClick={handleBackspace}
                  icon={<Delete className="w-5 h-5 opacity-80" />}
                />
              </div>
            </>
          )}

          {/* 3. Symbols Mode (?123) */}
          {layoutMode === 'symbols' && (
            <>
              {/* Row 1 */}
              <div className="w-full flex justify-center">
                {symbolsRow1.map(char => (
                  <KeyView key={char} label={char} />
                ))}
              </div>

              {/* Row 2 */}
              <div className="w-full flex justify-center">
                {symbolsRow2.map(char => (
                  <KeyView key={char} label={char} />
                ))}
              </div>

              {/* Row 3 */}
              <div className="w-full flex justify-center">
                <KeyView
                  label="=\<"
                  flexGrow={1.4}
                  displayLabel="=\<"
                  onClick={() => setLayoutMode('symbols_alt')}
                />
                {symbolsRow3.map(char => (
                  <KeyView key={char} label={char} />
                ))}
                <KeyView
                  label="backspace"
                  flexGrow={1.4}
                  onClick={handleBackspace}
                  icon={<Delete className="w-5 h-5 opacity-80" />}
                />
              </div>
            </>
          )}

          {/* 4. Alt Symbols Mode (=\<+) */}
          {layoutMode === 'symbols_alt' && (
            <>
              {/* Row 1 */}
              <div className="w-full flex justify-center">
                {altSymbolsRow1.map(char => (
                  <KeyView key={char} label={char} />
                ))}
              </div>

              {/* Row 2 */}
              <div className="w-full flex justify-center">
                {altSymbolsRow2.map(char => (
                  <KeyView key={char} label={char} />
                ))}
              </div>

              {/* Row 3 */}
              <div className="w-full flex justify-center">
                <KeyView
                  label="123"
                  flexGrow={1.4}
                  displayLabel="?123"
                  onClick={() => setLayoutMode('symbols')}
                />
                {altSymbolsRow3.map(char => (
                  <KeyView key={char} label={char} />
                ))}
                <KeyView
                  label="backspace"
                  flexGrow={1.4}
                  onClick={handleBackspace}
                  icon={<Delete className="w-5 h-5 opacity-80" />}
                />
              </div>
            </>
          )}

          {/* 5. Phone Keypad Mode */}
          {layoutMode === 'phone' && (
            <div className="w-full max-w-sm flex flex-col items-center py-1">
              {phoneRows.map((row, rIdx) => (
                <div key={rIdx} className="w-full flex justify-center">
                  {row.map(char => {
                    if (char === 'Back') {
                      return (
                        <KeyView
                          key={char}
                          label="backspace"
                          onClick={handleBackspace}
                          icon={<Delete className="w-5 h-5 opacity-80" />}
                        />
                      );
                    }
                    return (
                      <KeyView
                        key={char}
                        label={char}
                        onClick={() => handleKeyClick(char)}
                      />
                    );
                  })}
                </div>
              ))}
            </div>
          )}

          {/* 6. Universal Bottom Row */}
          <div className="w-full flex items-center justify-center">
            {/* Symbol Switcher / Letters Switcher */}
            {layoutMode === 'letters' ? (
              <KeyView
                label="?123"
                flexGrow={1.2}
                displayLabel="?123"
                onClick={() => setLayoutMode('symbols')}
              />
            ) : (
              <KeyView
                label="ABC"
                flexGrow={1.2}
                displayLabel="ABC"
                onClick={() => setLayoutMode('letters')}
              />
            )}

            {/* Language Switch Key */}
            {config.showLanguageSwitchKey && (
              <KeyView
                label="lang"
                flexGrow={0.9}
                onClick={toggleLanguage}
                icon={<Globe className="w-4 h-4 opacity-75" />}
              />
            )}

            {/* Emoji Key */}
            {config.showEmojiKey && (
              <KeyView
                label="emoji"
                flexGrow={0.9}
                onClick={() => setLayoutMode('emoji')}
                icon={<Smile className="w-4 h-4 opacity-75" />}
              />
            )}

            {/* Comma Key */}
            {config.showCommaKey && layoutMode === 'letters' && !isWijesekara && (
              <KeyView label="," flexGrow={0.8} />
            )}

            {/* Spacebar */}
            <KeyView
              label="space"
              isSpacebar={true}
              flexGrow={4.2}
              onClick={handleSpace}
              displayLabel={
                config.customSpacebarText ||
                (isSinglish ? 'සිංහල (Singlish)' : isWijesekara ? 'විජේසේකර' : 'English')
              }
            />

            {/* Period Key */}
            {config.showPeriodKey && layoutMode === 'letters' && !isWijesekara && (
              <KeyView label="." flexGrow={0.8} subLabel="?" />
            )}

            {/* Enter / Action Key */}
            <KeyView
              label="enter"
              isAction={true}
              flexGrow={1.5}
              onClick={handleEnter}
              icon={<CornerDownLeft className="w-5 h-5 font-bold" />}
            />
          </div>
        </div>

        {/* Right Side Expansion / Swap controls if in One-Handed Left mode */}
        {config.oneHandedMode === 'left' && (
          <div className="flex flex-col gap-2 p-1 border-l border-white/10 shrink-0">
            <button
              onClick={() => updateConfig({ oneHandedMode: 'right' })}
              className="p-2 rounded bg-white/10 hover:bg-white/20 text-white cursor-pointer"
              title="Switch to Right Hand"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
            <button
              onClick={() => updateConfig({ oneHandedMode: 'off' })}
              className="p-2 rounded bg-white/10 hover:bg-white/20 text-white cursor-pointer"
              title="Full Width Keyboard"
            >
              <Maximize2 className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
