import React, { useState } from 'react';
import { useKeyboard } from '../context/KeyboardContext';

interface KeyViewProps {
  label: string;
  displayLabel?: string;
  subLabel?: string;
  isAction?: boolean;
  isSpacebar?: boolean;
  flexGrow?: number;
  widthClass?: string;
  onClick?: () => void;
  onLongPress?: () => void;
  children?: React.ReactNode;
  icon?: React.ReactNode;
}

export const KeyView: React.FC<KeyViewProps> = ({
  label,
  displayLabel,
  subLabel,
  isAction = false,
  isSpacebar = false,
  flexGrow = 1,
  widthClass,
  onClick,
  onLongPress,
  children,
  icon
}) => {
  const {
    activeTheme,
    config,
    handleKeyClick,
    showKeyPreview,
    setShowKeyPreview
  } = useKeyboard();

  const [isPressed, setIsPressed] = useState(false);
  const [longPressTimer, setLongPressTimer] = useState<NodeJS.Timeout | null>(null);

  const textToDisplay = displayLabel !== undefined ? displayLabel : label;

  const handleTouchStart = () => {
    setIsPressed(true);
    if (config.showPopupOnKeypress && !isAction && !isSpacebar) {
      setShowKeyPreview(textToDisplay);
    }
    if (subLabel && config.touchHoldSymbols) {
      const timer = setTimeout(() => {
        if (onLongPress) {
          onLongPress();
        } else {
          handleKeyClick(subLabel);
        }
      }, config.touchHoldDelay || 350);
      setLongPressTimer(timer);
    }
  };

  const handleTouchEnd = () => {
    setIsPressed(false);
    if (longPressTimer) {
      clearTimeout(longPressTimer);
      setLongPressTimer(null);
    }
    setTimeout(() => {
      setShowKeyPreview(null);
    }, 150);
  };

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    if (onClick) {
      onClick();
    } else {
      handleKeyClick(label, isAction);
    }
  };

  // Dynamic Theme Colors
  const bgColor = isAction
    ? activeTheme.accentKeyBgColor
    : isPressed
    ? activeTheme.keyBgPressedColor
    : activeTheme.keyBgColor;

  const textColor = isAction ? activeTheme.accentKeyTextColor : activeTheme.keyTextColor;
  const strokeBorder = config.keyBordersEnabled
    ? `${config.keyBorderWidth}px solid ${activeTheme.strokeColor}`
    : 'none';

  const opacity = (config.keyTranslucencyAlpha || 95) / 100;

  return (
    <div
      className={`relative flex flex-col items-center justify-center select-none cursor-pointer transition-all duration-75 active:scale-95 ${
        widthClass || ''
      }`}
      style={{
        flex: flexGrow,
        margin: '3px 2px',
        height: '46px',
        minWidth: isSpacebar ? '120px' : '30px',
      }}
      onMouseDown={handleTouchStart}
      onMouseUp={handleTouchEnd}
      onMouseLeave={handleTouchEnd}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      onClick={handleClick}
    >
      {/* Visual Key Container */}
      <div
        className="w-full h-full flex flex-col items-center justify-center rounded shadow-sm relative overflow-hidden"
        style={{
          backgroundColor: isSpacebar && config.spacebarColor ? config.spacebarColor : bgColor,
          color: textColor,
          border: strokeBorder,
          borderRadius: `${activeTheme.cornerRadius || 8}px`,
          opacity,
          boxShadow: isPressed ? 'inset 0 2px 4px rgba(0,0,0,0.3)' : '0 1px 2px rgba(0,0,0,0.2)'
        }}
      >
        {/* Long-press symbol preview sub-label */}
        {subLabel && config.touchHoldSymbols && (
          <span
            className="absolute top-0.5 right-1 text-[9px] opacity-60 font-mono leading-none pointer-events-none"
            style={{ color: textColor }}
          >
            {subLabel}
          </span>
        )}

        {/* Center Content */}
        {icon ? (
          <div className="flex items-center justify-center">{icon}</div>
        ) : children ? (
          children
        ) : (
          <span
            className={`font-medium select-none pointer-events-none leading-none ${
              config.rgbTextEnabled ? 'rgb-animated-text font-bold' : ''
            } ${
              textToDisplay.length > 2 ? 'text-xs' : 'text-base sm:text-lg'
            }`}
            style={{
              fontSize: `${config.fontScale || 100}%`,
              fontFamily: "'Noto Sans Sinhala', 'Plus Jakarta Sans', sans-serif"
            }}
          >
            {textToDisplay}
          </span>
        )}
      </div>

      {/* Pop-up key preview on press */}
      {config.showPopupOnKeypress && isPressed && showKeyPreview === textToDisplay && !isAction && !isSpacebar && (
        <div
          className="absolute -top-12 z-50 px-3 py-2 rounded-lg shadow-xl font-bold text-xl flex items-center justify-center pointer-events-none animate-in fade-in zoom-in duration-100"
          style={{
            backgroundColor: activeTheme.smartbarBgColor || '#1F2937',
            color: activeTheme.smartbarTextColor || '#FFFFFF',
            border: `1px solid ${activeTheme.strokeColor}`,
            minWidth: '46px',
            minHeight: '44px',
            fontFamily: "'Noto Sans Sinhala', 'Plus Jakarta Sans', sans-serif"
          }}
        >
          {textToDisplay}
        </div>
      )}
    </div>
  );
};
