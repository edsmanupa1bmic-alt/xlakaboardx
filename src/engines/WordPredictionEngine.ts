import { SuggestionItem } from '../types';
import { SinglishParser } from './SinglishParser';

class TrieNode {
  char: string;
  children: Map<string, TrieNode> = new Map();
  isWord: boolean = false;
  frequency: number = 0;
  word: string | null = null;

  constructor(char: string) {
    this.char = char;
  }
}

export class WordPredictionEngine {
  private static instance: WordPredictionEngine;

  private sinhalaTrie = new TrieNode(' ');
  private englishTrie = new TrieNode(' ');
  private userLearnedSinhala = new Map<string, number>();
  private userLearnedEnglish = new Map<string, number>();

  private readonly STORAGE_KEY_SINHALA = 'lakmal_user_learned_sinhala';
  private readonly STORAGE_KEY_ENGLISH = 'lakmal_user_learned_english';

  private readonly englishTypos: Record<string, string> = {
    "teh": "the", "adn": "and", "waht": "what", "taht": "that",
    "wiht": "with", "tiem": "time", "recieve": "receive", "recieved": "received",
    "seperate": "separate", "definately": "definitely", "untill": "until",
    "becuase": "because", "beleive": "believe", "thier": "their", "wierd": "weird",
    "dont": "don't", "cant": "can't", "wont": "won't", "didnt": "didn't",
    "isnt": "isn't", "arent": "aren't", "couldnt": "couldn't", "wouldnt": "wouldn't",
    "shouldnt": "shouldn't", "hasnt": "hasn't", "havent": "haven't",
    "im": "I'm", "youre": "you're", "theyre": "they're", "ill": "I'll", "ive": "I've",
    "hellp": "hello", "thx": "thanks", "ty": "thank you", "pls": "please", "plz": "please",
    "sory": "sorry", "tommorow": "tomorrow", "tomorow": "tomorrow", "alot": "a lot",
    "gud": "good", "bt": "but", "ur": "your", "hw": "how",
    "thnks": "thanks", "welcom": "welcome", "bday": "birthday",
    "rember": "remember", "truely": "truly", "freind": "friend", "freinds": "friends",
    "peaple": "people", "intresting": "interesting", "fav": "favorite"
  };

  private readonly sinhalaTypos: Record<string, string> = {
    "කරනන": "කරන්න", "යනන": "යන්න", "ඉනන": "ඉන්න", "ගනන": "ගන්න", "දෙනන": "දෙන්න",
    "බලනන": "බලන්න", "කියනන": "කියන්න", "හිතනන": "හිතන්න", "එනන": "එන්න",
    "සතුටුඉ": "සතුටුයි", "හොඳඉ": "හොඳයි", "ලස්සනඉ": "ලස්සනයි", "එලඉ": "එළයි",
    "ආදරෙඉ": "ආදරෙයි", "නැහෑ": "නැහැ", "නෑහෑ": "නැහැ", "නෙවෙඉ": "නෙවෙයි",
    "ස්තුති": "ස්තූතියි", "ස්තුතියි": "ස්තූතියි", "ආයුබෝවන": "ආයුබෝවන්",
    "සුබ පැතුම්": "සුබපැතුම්", "පරිස්සමින්": "පරිස්සමෙන්", "එල": "එළ"
  };

  private readonly singlishInputCorrections: Record<string, string> = {
    "thx": "ස්තූතියි",
    "ty": "ස්තූතියි",
    "sthuthi": "ස්තූතියි",
    "sthuthiy": "ස්තූතියි",
    "stuti": "ස්තූතියි",
    "ayubowan": "ආයුබෝවන්",
    "ayubowang": "ආයුබෝවන්",
    "aayubowan": "ආයුබෝවන්",
    "subapathum": "සුබපැතුම්",
    "puluwan": "පුළුවන්",
    "kohomada": "කොහොමද",
    "mokada": "මොකද",
    "mokakda": "මොකක්ද",
    "machang": "මචං",
    "machan": "මචන්"
  };

  constructor() {
    this.initDictionaries();
    this.loadUserLearnedWords();
  }

  static getInstance(): WordPredictionEngine {
    if (!WordPredictionEngine.instance) {
      WordPredictionEngine.instance = new WordPredictionEngine();
    }
    return WordPredictionEngine.instance;
  }

  private insertWord(root: TrieNode, word: string, freq: number) {
    if (!word || !word.trim()) return;
    let current = root;
    for (const char of word) {
      if (!current.children.has(char)) {
        current.children.set(char, new TrieNode(char));
      }
      current = current.children.get(char)!;
    }
    current.isWord = true;
    current.word = word;
    current.frequency = Math.max(current.frequency, freq);
  }

  private initDictionaries() {
    // 1. High-frequency Sinhala Vocabulary
    const commonSinhala: [string, number][] = [
      // Pronouns & Core Questions
      ["මම", 500], ["මට", 480], ["මගේ", 470], ["අපි", 460], ["අපිට", 450], ["අපේ", 440],
      ["ඔයා", 520], ["ඔයාට", 510], ["ඔයාගේ", 500], ["ඔයාලා", 470], ["ඔයාලට", 460], ["ඔයාලගේ", 450],
      ["එයා", 420], ["එයාට", 410], ["එයාගේ", 400], ["එයාලා", 390],
      ["මොකද", 460], ["මොකක්ද", 450], ["කොහොමද", 480], ["කොහෙද", 440], ["ඇයි", 450],
      ["කවුද", 430], ["කවදාද", 400], ["කීයද", 380], ["කොච්චරද", 370], ["මොනවද", 410],

      // Common Verbs & States
      ["කරන්න", 490], ["කරනවා", 480], ["කරන්නේ", 470], ["කළා", 440], ["කරලා", 450], ["කරමු", 430],
      ["වෙනවා", 460], ["වුණා", 440], ["වෙලා", 450], ["වෙන්න", 455],
      ["ගන්න", 470], ["ගත්තා", 440], ["ගන්නේ", 430], ["ගන්නවා", 450],
      ["එන්න", 460], ["ආවා", 450], ["එනවා", 440], ["එමු", 410],
      ["යන්න", 470], ["ගියා", 460], ["යනවා", 450], ["යමු", 440],
      ["ඉන්න", 480], ["හිටියා", 450], ["ඉන්නවා", 470], ["ඉන්නේ", 460],
      ["තියෙනවා", 490], ["තිබ්බා", 450], ["තියෙන්නේ", 460], ["තියෙයි", 410],
      ["බලන්න", 440], ["බැලුවා", 420], ["බලනවා", 430],
      ["කියන්න", 460], ["කිව්වා", 450], ["කියනවා", 440],
      ["දෙන්න", 450], ["දුන්නා", 430], ["දෙනවා", 420],
      ["හිතන්න", 420], ["හිතුණා", 410], ["හිතනවා", 400],
      ["දන්නවා", 460], ["දන්නේ", 440], ["දන්නෑ", 410],

      // Negation & Modals
      ["නෑ", 500], ["නැහැ", 490], ["නේද", 480], ["නෙවෙයි", 460],
      ["පුළුවන්", 480], ["බෑ", 470], ["බැහැ", 460], ["ඕන", 470], ["ඕනේ", 480], ["එපා", 460],

      // Polite phrases & Greetings
      ["ස්තූතියි", 490], ["ආයුබෝවන්", 480], ["සුබ", 460], ["පැතුම්", 440], ["සුබපැතුම්", 470],
      ["සුබ උදෑසනක්", 450], ["සුබ රාත්‍රියක්", 440],
      ["හොඳයි", 480], ["හරි", 490], ["ලස්සනයි", 440], ["එල", 460], ["සුපිරි", 470],
      ["ආදරෙයි", 460], ["පරිස්සමෙන්", 450], ["සතුටුයි", 430], ["කමක් නෑ", 440],

      // Common Nouns & Adverbs
      ["දැන්", 480], ["පස්සේ", 470], ["අද", 480], ["ඊයේ", 440], ["හෙට", 460],
      ["ඉක්මනින්", 430], ["ටිකක්", 460], ["ගොඩක්", 480], ["වගේ", 470], ["ගැන", 460],
      ["එක්ක", 470], ["සමඟ", 430], ["නිසා", 460], ["හින්දා", 470], ["විතරක්", 440],
      ["නමුත්", 450], ["සහ", 460], ["නැත්නම්", 440],
      ["ගෙදර", 470], ["රට", 440], ["වැඩ", 470], ["යාළුවා", 460], ["මිතුරා", 420],
      ["පොත", 410], ["පාසල", 420], ["කාර්යාලය", 400], ["දුරකථනය", 430], ["පණිවිඩය", 420],
      ["මුදල්", 440], ["වෙලාව", 460], ["තැන", 430], ["ලංකාව", 460], ["සිංහල", 470],
      ["යාලුවා", 450], ["මචං", 480], ["මචන්", 480], ["බ්‍රෝ", 460]
    ];

    for (const [word, freq] of commonSinhala) {
      this.insertWord(this.sinhalaTrie, word, freq);
    }

    // 2. High-frequency English Vocabulary
    const commonEnglish: [string, number][] = [
      ["the", 500], ["be", 490], ["to", 490], ["of", 480], ["and", 480], ["a", 480],
      ["in", 470], ["that", 470], ["have", 460], ["i", 500], ["it", 470], ["for", 460],
      ["not", 460], ["on", 450], ["with", 450], ["he", 440], ["as", 440], ["you", 500],
      ["do", 470], ["at", 450], ["this", 470], ["but", 460], ["his", 430], ["by", 430],
      ["from", 440], ["they", 450], ["we", 470], ["say", 440], ["her", 420], ["she", 440],
      ["or", 450], ["an", 430], ["will", 470], ["my", 480], ["one", 450], ["all", 460],
      ["would", 440], ["there", 450], ["their", 440], ["what", 470], ["so", 460], ["up", 450],
      ["out", 440], ["if", 450], ["about", 450], ["who", 440], ["get", 460], ["which", 430],
      ["go", 470], ["me", 480], ["when", 460], ["make", 450], ["can", 480], ["like", 470],
      ["time", 460], ["no", 470], ["just", 460], ["him", 430], ["know", 470], ["take", 450],
      ["people", 440], ["into", 430], ["year", 420], ["your", 480], ["good", 480], ["some", 450],
      ["could", 440], ["them", 440], ["see", 460], ["other", 430], ["than", 430], ["then", 450],
      ["now", 470], ["look", 450], ["only", 440], ["come", 460], ["its", 430], ["over", 430],
      ["think", 460], ["also", 450], ["back", 450], ["after", 440], ["use", 440], ["two", 430],
      ["how", 470], ["our", 460], ["work", 460], ["first", 440], ["well", 450], ["way", 450],
      ["even", 430], ["new", 450], ["want", 460], ["because", 450], ["any", 440], ["these", 430],
      ["give", 450], ["day", 450], ["most", 440], ["us", 460],
      ["hello", 490], ["hi", 490], ["thanks", 490], ["thank", 480], ["okay", 490], ["ok", 490],
      ["yes", 490], ["please", 480], ["sorry", 480], ["love", 480], ["friend", 470], ["happy", 470],
      ["great", 470], ["nice", 470], ["cool", 470], ["bro", 480], ["machan", 480], ["super", 470]
    ];

    for (const [word, freq] of commonEnglish) {
      this.insertWord(this.englishTrie, word, freq);
    }
  }

  private loadUserLearnedWords() {
    try {
      const rawSinhala = localStorage.getItem(this.STORAGE_KEY_SINHALA);
      if (rawSinhala) {
        const parsed = JSON.parse(rawSinhala);
        for (const [w, f] of Object.entries(parsed)) {
          this.userLearnedSinhala.set(w, Number(f));
          this.insertWord(this.sinhalaTrie, w, Number(f));
        }
      }

      const rawEnglish = localStorage.getItem(this.STORAGE_KEY_ENGLISH);
      if (rawEnglish) {
        const parsed = JSON.parse(rawEnglish);
        for (const [w, f] of Object.entries(parsed)) {
          this.userLearnedEnglish.set(w, Number(f));
          this.insertWord(this.englishTrie, w, Number(f));
        }
      }
    } catch {
      // ignore
    }
  }

  learnWord(word: string, isSinhala: boolean) {
    const trimmed = word.trim();
    if (trimmed.length < 2) return;

    const targetMap = isSinhala ? this.userLearnedSinhala : this.userLearnedEnglish;
    const currentFreq = targetMap.get(trimmed) || 550;
    const updatedFreq = currentFreq + 25;
    targetMap.set(trimmed, updatedFreq);

    const targetTrie = isSinhala ? this.sinhalaTrie : this.englishTrie;
    this.insertWord(targetTrie, trimmed, updatedFreq);

    this.saveUserLearnedWords();
  }

  getLearnedWords(): { sinhala: [string, number][]; english: [string, number][] } {
    return {
      sinhala: Array.from(this.userLearnedSinhala.entries()),
      english: Array.from(this.userLearnedEnglish.entries())
    };
  }

  clearLearnedWords() {
    this.userLearnedSinhala.clear();
    this.userLearnedEnglish.clear();
    localStorage.removeItem(this.STORAGE_KEY_SINHALA);
    localStorage.removeItem(this.STORAGE_KEY_ENGLISH);
    this.sinhalaTrie = new TrieNode(' ');
    this.englishTrie = new TrieNode(' ');
    this.initDictionaries();
  }

  private saveUserLearnedWords() {
    try {
      const sinhalaObj = Object.fromEntries(this.userLearnedSinhala);
      const englishObj = Object.fromEntries(this.userLearnedEnglish);
      localStorage.setItem(this.STORAGE_KEY_SINHALA, JSON.stringify(sinhalaObj));
      localStorage.setItem(this.STORAGE_KEY_ENGLISH, JSON.stringify(englishObj));
    } catch {
      // ignore
    }
  }

  getSuggestions(buffer: string, isSinglishMode: boolean = true): SuggestionItem[] {
    if (!buffer || !buffer.trim()) return [];

    const results: SuggestionItem[] = [];
    const seenWords = new Set<string>();

    if (isSinglishMode) {
      // 1. Candidate 1 (Exact Transliteration from SinglishParser)
      const sinhalaExact = SinglishParser.parse(buffer);
      if (sinhalaExact && sinhalaExact.trim()) {
        results.push({
          text: sinhalaExact,
          isSinhala: true,
          isPrimary: true,
          isExactMatch: true
        });
        seenWords.add(sinhalaExact);
      }

      // 2. Candidates 2-5: Search Sinhala Trie completions with root matching sinhalaExact
      if (sinhalaExact) {
        const completions = this.searchTrie(this.sinhalaTrie, sinhalaExact, 5);
        for (const [word] of completions) {
          if (!seenWords.has(word)) {
            results.push({
              text: word,
              isSinhala: true,
              isPrimary: false,
              isExactMatch: false
            });
            seenWords.add(word);
          }
        }
      }

      // 3. English alternatives / completions for the typed latin prefix
      const lowerBuffer = buffer.toLowerCase();
      const englishMatches = this.searchTrie(this.englishTrie, lowerBuffer, 3);
      for (const [engWord] of englishMatches) {
        if (!seenWords.has(engWord)) {
          results.push({
            text: engWord,
            isSinhala: false,
            isPrimary: false,
            isExactMatch: false
          });
          seenWords.add(engWord);
        }
      }

      // Also offer raw Latin buffer if length > 1
      if (!seenWords.has(buffer) && buffer.length > 1) {
        results.push({
          text: buffer,
          isSinhala: false,
          isPrimary: false,
          isExactMatch: false
        });
        seenWords.add(buffer);
      }
    } else {
      // English / Latin Mode
      const lowerBuffer = buffer.toLowerCase();
      const englishMatches = this.searchTrie(this.englishTrie, lowerBuffer, 8);
      let isFirst = true;

      for (const [engWord] of englishMatches) {
        const formatted = buffer[0] && buffer[0] === buffer[0].toUpperCase()
          ? engWord.charAt(0).toUpperCase() + engWord.slice(1)
          : engWord;

        if (!seenWords.has(formatted)) {
          results.push({
            text: formatted,
            isSinhala: false,
            isPrimary: isFirst,
            isExactMatch: formatted.toLowerCase() === buffer.toLowerCase()
          });
          seenWords.add(formatted);
          isFirst = false;
        }
      }

      if (!seenWords.has(buffer)) {
        results.unshift({
          text: buffer,
          isSinhala: false,
          isPrimary: results.length === 0,
          isExactMatch: true
        });
      }
    }

    return results.slice(0, 10);
  }

  private searchTrie(root: TrieNode, prefix: string, limit: number): [string, number][] {
    let current = root;
    for (const char of prefix) {
      if (!current.children.has(char)) {
        return [];
      }
      current = current.children.get(char)!;
    }

    const collected: [string, number][] = [];
    this.collectWords(current, collected, limit * 4, 0, 8);

    collected.sort((a, b) => b[1] - a[1]);
    return collected.slice(0, limit);
  }

  private collectWords(node: TrieNode, list: [string, number][], maxCollect: number, depth: number, maxDepth: number) {
    if (depth > maxDepth || list.length >= maxCollect) return;

    if (node.isWord && node.word) {
      list.push([node.word, node.frequency]);
    }

    for (const child of node.children.values()) {
      if (list.length >= maxCollect) break;
      this.collectWords(child, list, maxCollect, depth + 1, maxDepth);
    }
  }

  getAutoCorrection(rawWord: string, isSinglish: boolean): string | null {
    const trimmed = rawWord.trim();
    if (trimmed.length < 2) return null;

    if (isSinglish) {
      const lower = trimmed.toLowerCase();
      if (this.singlishInputCorrections[lower]) {
        return this.singlishInputCorrections[lower];
      }

      const parsedSinhala = SinglishParser.parse(trimmed);
      if (this.sinhalaTypos[parsedSinhala]) return this.sinhalaTypos[parsedSinhala];
      if (this.sinhalaTypos[trimmed]) return this.sinhalaTypos[trimmed];

      return null;
    } else {
      const lower = trimmed.toLowerCase();
      if (this.englishTypos[lower]) {
        const replacement = this.englishTypos[lower];
        return trimmed[0] === trimmed[0].toUpperCase()
          ? replacement.charAt(0).toUpperCase() + replacement.slice(1)
          : replacement;
      }
      return null;
    }
  }
}
