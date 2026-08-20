/**
 * WijesekaraEngine
 * Ported faithfully from org.fossify.keyboard.helpers.WijesekaraEngine
 * Maps standard hardware/software keys to traditional Sinhala Wijesekara layout characters.
 */

const directMap: Record<string, string> = {
  // Lowercase / unshifted
  'q': 'ු', 'w': 'අ', 'e': 'ැ', 'r': 'ර', 't': 'එ',
  'y': 'හ', 'u': 'ම', 'i': 'ස', 'o': 'ද', 'p': 'ච',
  'a': '්', 's': 'ි', 'd': 'ා', 'f': 'ෙ', 'g': 'ට',
  'h': 'ය', 'j': 'ව', 'k': 'න', 'l': 'ක', ';': 'ත',
  "'": '.', 'z': 'ූ', 'x': 'ං', 'c': 'ජ', 'v': 'ඩ',
  'b': 'ඉ', 'n': 'බ', 'm': 'ප', ',': 'ල', '.': 'ග',
  '/': '/',
  // Uppercase / shifted
  'Q': 'ූ', 'W': 'උ', 'E': 'ෑ', 'R': 'ඍ', 'T': 'ඔ',
  'Y': 'ශ', 'U': 'ඹ', 'I': 'ෂ', 'O': 'ධ', 'P': 'ඡ',
  'A': 'ෟ', 'S': 'ී', 'D': 'ෲ', 'F': 'ෆ', 'G': 'ඨ',
  'H': '්‍ය', 'J': 'ළු', 'K': 'ණ', 'L': 'ඛ', ':': 'ථ',
  'Z': 'ෳ', 'X': 'ඞ', 'C': 'ඣ', 'V': 'ඪ', 'B': 'ඊ',
  'N': 'භ', 'M': 'ඵ', '<': 'ළ', '>': 'ඝ', '?': '?'
};

export class WijesekaraEngine {
  static getSinhalaChar(ch: string): string {
    return directMap[ch] || ch;
  }

  static processKey(currentWord: string, newChar: string): { replacedCount: number; output: string } {
    const mapped = this.getSinhalaChar(newChar);
    return { replacedCount: 1, output: mapped };
  }
}
