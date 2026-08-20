/**
 * SinglishParser Engine
 * Ported faithfully from org.fossify.keyboard.helpers.SinglishParser
 * Converts Latin Singlish phonetics directly to Sinhala Unicode in real time.
 */

const HAL = "\u0DCA"; // ්
const ZWJ = "\u200D"; // Zero Width Joiner

// Vowel modifier rules when following a consonant (longest match first)
const vowelModifierRules: [string, string][] = [
  ["ruu", "\u0DF2"], // ෲ (kruu -> කෲ)
  ["ru", "\u0DD8"],  // ෘ (kru -> කෘ)
  ["aAa", "\u0DD1"], // ෑ
  ["aAA", "\u0DD1"], // ෑ
  ["Aa", "\u0DD1"],  // ෑ (kAa -> කෑ)
  ["AA", "\u0DD1"],  // ෑ (kAA -> කෑ)
  ["A", "\u0DD0"],   // ැ (kA -> කැ)
  ["aa", "\u0DCF"],  // ා (kaa -> කා)
  ["a", ""],         // Inherent pure consonant (ka -> ක)
  ["ii", "\u0DD3"],  // ී (kii -> කී)
  ["i", "\u0DD2"],   // ි (ki -> කි)
  ["uu", "\u0DD6"],  // ූ (kuu -> කූ)
  ["u", "\u0DD4"],   // ු (ku -> කු, Lu -> ළු)
  ["ee", "\u0DDA"],  // ේ (kee -> කේ)
  ["e", "\u0DD9"],   // ෙ (ke -> කෙ)
  ["ai", "\u0DDB"],  // ෛ (kai -> කෛ)
  ["oo", "\u0DDD"],  // ෝ (koo -> කෝ)
  ["o", "\u0DDC"],   // ො (ko -> කො)
  ["au", "\u0DDE"],  // ෞ (kau -> කෞ)
  ["ou", "\u0DDE"],  // ෞ (kou -> කෞ)
  ["aH", "\u0D83"],  // ඃ (kaH -> කඃ)
  ["H", "\u0D83"],   // ඃ
  ["ax", "\u0D82"],  // ං (kax -> කං)
  ["azn", "\u0D82"], // ං (kazn -> කං)
  ["x", "\u0D82"],   // ං
  ["aX", "\u0D9E"],  // ඞ (kaX -> කඞ)
  ["X", "\u0D9E"]    // ඞ
];

// Independent vowels (start of word / standalone / following another vowel)
const independentVowelRules: [string, string][] = [
  ["Ru", "\u0D8E"], // ඎ
  ["R", "\u0D8D"],  // ඍ
  ["Aa", "\u0D88"], // ඈ
  ["AA", "\u0D88"], // ඈ
  ["A", "\u0D87"],  // ඇ
  ["aa", "\u0D86"], // ආ
  ["a", "\u0D85"],  // අ
  ["ii", "\u0D8A"], // ඊ
  ["i", "\u0D89"],  // ඉ
  ["uu", "\u0D8C"], // ඌ
  ["u", "\u0D8B"],  // උ
  ["ee", "\u0D92"], // ඒ
  ["e", "\u0D91"],  // එ
  ["ai", "\u0D93"], // ඓ
  ["oo", "\u0D95"], // ඕ
  ["o", "\u0D94"],  // ඔ
  ["au", "\u0D96"], // ඖ
  ["ou", "\u0D96"]  // ඖ
];

// Consonant stems (sorted longest pattern first)
const consonantRules: [string, string][] = [
  // Sanyaka with z prefix (3 letters)
  ["zdh", "\u0DB3"], // ඳ (zdha -> සඳ/ඳ)

  // Mahaprana (3 letters)
  ["chh", "\u0DA1"], // ඡ (chha -> ඡ)
  ["thh", "\u0DAE"], // ථ (thha -> ථ)
  ["dhh", "\u0DB0"], // ධ (dhha -> ධ)

  // Sanyaka with z prefix (2 letters)
  ["zg", "\u0D9F"], // ඟ (zga -> ඟ)
  ["zj", "\u0DA6"], // ඦ (zja -> ඦ)
  ["zd", "\u0DAC"], // ඬ (zda -> ඬ)
  ["zq", "\u0DB3"], // ඳ (zqa -> ඳ)
  ["zk", "\u0DA4"], // ඤ (zka -> ඤ)
  ["zh", "\u0DA5"], // ඥ (zha -> ඥ)

  // Mahaprana & Murdhaja (2 letters)
  ["kh", "\u0D9B"], // ඛ (kha -> ඛ)
  ["gh", "\u0D9D"], // ඝ (gha -> ඝ)
  ["ph", "\u0DB5"], // ඵ (pha -> ඵ)
  ["bh", "\u0DB7"], // භ (bha -> භ)
  ["Sh", "\u0DC2"], // ෂ (Sha -> ෂ)

  // Standard consonants (2 letters)
  ["ch", "\u0DA0"], // ච (cha -> ච)
  ["th", "\u0DAD"], // ත (tha -> ත)
  ["dh", "\u0DAF"], // ද (dha -> ද)
  ["sh", "\u0DC1"], // ශ (sha -> ශ)

  // Single letter Murdhaja / Special
  ["q", "\u0DAF"],  // ද (qa -> ද)
  ["T", "\u0DA8"],  // ඨ (Ta -> ඨ)
  ["D", "\u0DAA"],  // ඪ (Da -> ඪ)
  ["N", "\u0DAB"],  // ණ (Na -> ණ)
  ["L", "\u0DC5"],  // ළ (La -> ළ, Lu -> ළු)
  ["S", "\u0DC2"],  // ෂ (Sa -> ෂ)
  ["B", "\u0DB9"],  // ඹ (Ba -> ඹ)

  // Standard single letter consonants
  ["k", "\u0D9A"],  // ක (ka -> ක, k -> ක්)
  ["g", "\u0D9C"],  // ග (ga -> ග)
  ["j", "\u0DA2"],  // ජ (ja -> ජ)
  ["t", "\u0DA7"],  // ට (ta -> ට)
  ["d", "\u0DA9"],  // ඩ (da -> ඩ)
  ["n", "\u0DB1"],  // න (na -> න)
  ["p", "\u0DB4"],  // ප (pa -> ප)
  ["b", "\u0DB6"],  // බ (ba -> බ)
  ["m", "\u0DB8"],  // ම (ma -> ම)
  ["y", "\u0DBA"],  // ය (ya -> ය)
  ["r", "\u0DBB"],  // ර (ra -> ර)
  ["l", "\u0DBD"],  // ල (la -> ල)
  ["w", "\u0DC0"],  // ව (wa -> ව)
  ["v", "\u0DC0"],  // ව (va -> ව)
  ["s", "\u0DC3"],  // ස (sa -> ස)
  ["h", "\u0DC4"],  // හ (ha -> හ)
  ["f", "\u0DC6"],  // ෆ (fa -> ෆ)
  ["c", "\u0DA0"]   // ච
];

export class SinglishParser {
  /**
   * Parses a raw Latin Singlish string into Sinhala Unicode in real-time.
   */
  static parse(input: string): string {
    if (!input) return "";

    let result = "";
    let i = 0;
    const len = input.length;

    while (i < len) {
      const ch = input[i];

      // 1. Direct Binduva / Visargaya / Gayanukitta / azn
      if (ch === "x") {
        result += "\u0D82"; // ං
        i++;
        continue;
      }
      if (ch === "X") {
        result += "\u0D9E"; // ඞ
        i++;
        continue;
      }
      if (ch === "H") {
        result += "\u0D83"; // ඃ
        i++;
        continue;
      }
      if (input.startsWith("azn", i)) {
        result += "\u0D82"; // ං
        i += 3;
        continue;
      }

      const subFromI = input.substring(i);

      // 2. Check Consonant
      const consonantMatch = this.findConsonant(subFromI);
      if (consonantMatch) {
        const cPattern = consonantMatch[0];
        const cChar = consonantMatch[1];
        const nextIdx = i + cPattern.length;

        // Check if there is a vowel modifier directly
        const vowelModifierMatch = this.findVowelModifier(input.substring(nextIdx));
        if (vowelModifierMatch) {
          result += cChar + vowelModifierMatch[1];
          i = nextIdx + vowelModifierMatch[0].length;
          continue;
        }

        // Check for Yansaya: Consonant + 'y' + Vowel (e.g. kya -> ක්‍ය, ky -> ක්‍ය්)
        if (nextIdx < len && (input[nextIdx] === "y" || input[nextIdx] === "Y")) {
          const afterY = nextIdx + 1;
          const vMatch = this.findVowelModifier(input.substring(afterY));
          if (vMatch) {
            result += cChar + HAL + ZWJ + "\u0DBA" + vMatch[1];
            i = afterY + vMatch[0].length;
          } else {
            result += cChar + HAL + ZWJ + "\u0DBA" + HAL;
            i = afterY;
          }
          continue;
        }

        // Check for Rakaransaya: Consonant + 'r' + Vowel (e.g. kra -> ක්‍ර, kr -> ක්‍ර්)
        if (nextIdx < len && (input[nextIdx] === "r" || input[nextIdx] === "R")) {
          const afterR = nextIdx + 1;
          const vMatch = this.findVowelModifier(input.substring(afterR));
          if (vMatch) {
            result += cChar + HAL + ZWJ + "\u0DBB" + vMatch[1];
            i = afterR + vMatch[0].length;
          } else {
            result += cChar + HAL + ZWJ + "\u0DBB" + HAL;
            i = afterR;
          }
          continue;
        }

        // No vowel follows -> pure hal character
        result += cChar + HAL;
        i = nextIdx;
        continue;
      }

      // 3. Check Independent Vowel
      const independentVowelMatch = this.findIndependentVowel(subFromI);
      if (independentVowelMatch) {
        result += independentVowelMatch[1];
        i += independentVowelMatch[0].length;
        continue;
      }

      // 4. Any other character (spaces, punctuation, numbers)
      result += ch;
      i++;
    }

    return result;
  }

  private static findConsonant(str: string): [string, string] | null {
    if (!str) return null;
    for (const c of consonantRules) {
      if (str.startsWith(c[0])) {
        return c;
      }
    }
    return null;
  }

  private static findVowelModifier(str: string): [string, string] | null {
    if (!str) return null;
    for (const v of vowelModifierRules) {
      if (str.startsWith(v[0])) {
        return v;
      }
    }
    return null;
  }

  private static findIndependentVowel(str: string): [string, string] | null {
    if (!str) return null;
    for (const iv of independentVowelRules) {
      if (str.startsWith(iv[0])) {
        return iv;
      }
    }
    return null;
  }
}
