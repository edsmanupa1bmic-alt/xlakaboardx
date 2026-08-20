import React, { useState, useEffect } from 'react';
import { useKeyboard } from '../context/KeyboardContext';
import {
  X,
  Smartphone,
  Download,
  ExternalLink,
  Copy,
  Check,
  Package,
  Layers,
  Terminal,
  ShieldCheck,
  Sparkles,
  Zap,
  Globe,
  ArrowRight,
  Info
} from 'lucide-react';

export const AndroidApkModal: React.FC = () => {
  const { setActiveModal } = useKeyboard();
  const [copiedUrl, setCopiedUrl] = useState(false);
  const [copiedCommand, setCopiedCommand] = useState<string | null>(null);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [isInstalled, setIsInstalled] = useState(false);
  const [activeTab, setActiveTab] = useState<'quick' | 'pwabuilder' | 'capacitor' | 'gradle'>('quick');

  const appUrl = window.location.href;

  useEffect(() => {
    // Check if already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true);
    }

    const handler = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };

    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);

  const handleInstallPwa = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      if (outcome === 'accepted') {
        setIsInstalled(true);
      }
      setDeferredPrompt(null);
    } else {
      alert(
        'To install directly on Android:\n1. Tap the Chrome 3-dot menu (⋮) in the top right.\n2. Tap "Install App" or "Add to Home screen".\n3. Android will automatically package and install it as a native WebAPK!'
      );
    }
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    if (id === 'url') {
      setCopiedUrl(true);
      setTimeout(() => setCopiedUrl(false), 2000);
    } else {
      setCopiedCommand(id);
      setTimeout(() => setCopiedCommand(null), 2000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/75 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-2xl bg-zinc-900 border border-zinc-800 text-zinc-100 rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-800 bg-zinc-900/90">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white shadow-md shadow-emerald-500/20">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white">Android APK & Installation</h2>
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Android Ready
                </span>
              </div>
              <p className="text-xs text-zinc-400">Install as native WebAPK, generate APK package, or build via Gradle</p>
            </div>
          </div>
          <button
            id="btn-close-apk-modal"
            onClick={() => setActiveModal(null)}
            className="p-2 rounded-xl hover:bg-zinc-800 text-zinc-400 hover:text-zinc-200 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-zinc-800 bg-zinc-950/60 px-4 py-2 gap-1.5 overflow-x-auto scrollbar-none">
          <button
            onClick={() => setActiveTab('quick')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all flex items-center gap-1.5 ${
              activeTab === 'quick'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/60'
            }`}
          >
            <Zap className="w-3.5 h-3.5" />
            <span>1. Instant WebAPK (Recommended)</span>
          </button>
          <button
            onClick={() => setActiveTab('pwabuilder')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all flex items-center gap-1.5 ${
              activeTab === 'pwabuilder'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/60'
            }`}
          >
            <Download className="w-3.5 h-3.5" />
            <span>2. 1-Click APK Builder</span>
          </button>
          <button
            onClick={() => setActiveTab('capacitor')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all flex items-center gap-1.5 ${
              activeTab === 'capacitor'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/60'
            }`}
          >
            <Terminal className="w-3.5 h-3.5" />
            <span>3. Capacitor Export</span>
          </button>
          <button
            onClick={() => setActiveTab('gradle')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all flex items-center gap-1.5 ${
              activeTab === 'gradle'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/60'
            }`}
          >
            <Package className="w-3.5 h-3.5" />
            <span>4. Android Studio / Gradle</span>
          </button>
        </div>

        {/* Tab Content */}
        <div className="p-6 overflow-y-auto space-y-5 flex-1">
          {/* TAB 1: Instant WebAPK */}
          {activeTab === 'quick' && (
            <div className="space-y-4 animate-in fade-in duration-100">
              <div className="p-4 rounded-2xl bg-gradient-to-r from-emerald-950/40 to-blue-950/40 border border-emerald-500/30 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <ShieldCheck className="w-4 h-4 text-emerald-400" />
                    <h3 className="text-sm font-bold text-white">Direct Android Installation (WebAPK)</h3>
                  </div>
                  <p className="text-xs text-zinc-300">
                    Android will automatically generate and install a native APK package with offline support, app drawer icon, and full-screen immersion.
                  </p>
                </div>
                <button
                  id="btn-install-webapk-action"
                  onClick={handleInstallPwa}
                  className="px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-lg shadow-emerald-600/20 whitespace-nowrap flex items-center gap-2 transition-all"
                >
                  <Download className="w-4 h-4" />
                  <span>{isInstalled ? 'App Installed ✓' : 'Install on Android'}</span>
                </button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="p-3.5 rounded-xl bg-zinc-800/50 border border-zinc-700/60 space-y-1.5">
                  <span className="text-[11px] font-bold px-2 py-0.5 rounded-md bg-blue-500/20 text-blue-300">
                    Step 1
                  </span>
                  <p className="text-xs font-semibold text-zinc-200">Open on Android Device</p>
                  <p className="text-[11px] text-zinc-400">Open this URL in Google Chrome, Brave, or Samsung Internet on your phone.</p>
                </div>

                <div className="p-3.5 rounded-xl bg-zinc-800/50 border border-zinc-700/60 space-y-1.5">
                  <span className="text-[11px] font-bold px-2 py-0.5 rounded-md bg-blue-500/20 text-blue-300">
                    Step 2
                  </span>
                  <p className="text-xs font-semibold text-zinc-200">Tap Browser Menu (⋮)</p>
                  <p className="text-[11px] text-zinc-400">Click the three dots in Chrome top-right and select <strong>"Install App"</strong> or <strong>"Add to Home screen"</strong>.</p>
                </div>

                <div className="p-3.5 rounded-xl bg-zinc-800/50 border border-zinc-700/60 space-y-1.5">
                  <span className="text-[11px] font-bold px-2 py-0.5 rounded-md bg-blue-500/20 text-blue-300">
                    Step 3
                  </span>
                  <p className="text-xs font-semibold text-zinc-200">Enjoy Native Experience</p>
                  <p className="text-[11px] text-zinc-400">The app runs standalone without browser address bar, with high performance audio & haptic feedback.</p>
                </div>
              </div>

              {/* Share / App URL Box */}
              <div className="p-3.5 rounded-xl bg-zinc-800/30 border border-zinc-800 flex items-center justify-between gap-3">
                <div className="flex items-center gap-2 overflow-hidden text-xs text-zinc-300">
                  <Globe className="w-4 h-4 text-blue-400 shrink-0" />
                  <span className="truncate font-mono">{appUrl}</span>
                </div>
                <button
                  onClick={() => copyToClipboard(appUrl, 'url')}
                  className="px-3 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-xs font-medium flex items-center gap-1.5 shrink-0 transition-colors"
                >
                  {copiedUrl ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedUrl ? 'Copied' : 'Copy Link'}</span>
                </button>
              </div>
            </div>
          )}

          {/* TAB 2: PWABuilder 1-Click APK */}
          {activeTab === 'pwabuilder' && (
            <div className="space-y-4 animate-in fade-in duration-100">
              <div className="p-4 rounded-2xl bg-blue-950/40 border border-blue-500/30 space-y-2">
                <div className="flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-blue-400" />
                  <h3 className="text-sm font-bold text-white">Generate Signed APK / AAB via PWABuilder</h3>
                </div>
                <p className="text-xs text-zinc-300 leading-relaxed">
                  PWABuilder (maintained by Microsoft & Google) turns any PWA into an installable Android APK package or Google Play Store ready AAB package in under 1 minute with 0 coding required.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-zinc-800/40 border border-zinc-700/60 space-y-3">
                <h4 className="text-xs font-bold text-zinc-200 uppercase tracking-wider">How to generate your .apk file:</h4>
                <ol className="text-xs text-zinc-300 space-y-2 list-decimal list-inside">
                  <li>
                    Copy your live App URL: <span className="font-mono text-blue-400">{appUrl}</span>
                  </li>
                  <li>
                    Visit <strong>PWABuilder.com</strong> in your browser.
                  </li>
                  <li>Paste the URL and click <strong>"Start"</strong> (Manifest & Service Worker are already 100% configured!).</li>
                  <li>Click <strong>"Package for Stores"</strong> and choose <strong>"Android"</strong> to download your <span className="font-mono text-emerald-400">LakmalKeyboard.apk</span>.</li>
                </ol>

                <div className="pt-2 flex flex-wrap gap-2">
                  <a
                    href={`https://www.pwabuilder.com?url=${encodeURIComponent(appUrl)}`}
                    target="_blank"
                    rel="noreferrer"
                    className="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold flex items-center gap-2 transition-all shadow-md shadow-blue-500/20"
                  >
                    <span>Open PWABuilder.com</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                  <button
                    onClick={() => copyToClipboard(appUrl, 'url')}
                    className="px-3.5 py-2.5 rounded-xl bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-xs font-semibold flex items-center gap-1.5 transition-colors"
                  >
                    {copiedUrl ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>Copy App URL</span>
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: Capacitor Export */}
          {activeTab === 'capacitor' && (
            <div className="space-y-4 animate-in fade-in duration-100">
              <div className="p-4 rounded-2xl bg-indigo-950/40 border border-indigo-500/30 space-y-1.5">
                <h3 className="text-sm font-bold text-white">Build Native APK using Capacitor</h3>
                <p className="text-xs text-zinc-300">
                  Capacitor wraps the web keyboard in a native Android WebView with access to hardware vibration, native clipboard, and audio.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-zinc-950 border border-zinc-800 space-y-3 font-mono text-xs">
                <div className="flex items-center justify-between text-zinc-400 border-b border-zinc-800 pb-2">
                  <span>Terminal Commands</span>
                  <button
                    onClick={() =>
                      copyToClipboard(
                        'npm install @capacitor/core @capacitor/cli @capacitor/android\nnpx cap init "Lakmal Keyboard" "com.lakmal.keyboard"\nnpx cap add android\nnpm run build\nnpx cap sync\nnpx cap open android',
                        'cap'
                      )
                    }
                    className="flex items-center gap-1 text-[11px] text-blue-400 hover:text-blue-300 font-sans"
                  >
                    {copiedCommand === 'cap' ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedCommand === 'cap' ? 'Copied' : 'Copy All'}</span>
                  </button>
                </div>
                <pre className="text-zinc-200 overflow-x-auto leading-relaxed">
{`# 1. Export the project ZIP from AI Studio Settings
# 2. Extract and open folder in terminal, then run:

npm install @capacitor/core @capacitor/cli @capacitor/android
npx cap init "Lakmal Keyboard" "com.lakmal.keyboard"
npx cap add android
npm run build
npx cap sync
npx cap open android

# 3. Android Studio will open: Click "Build" -> "Build APK"`}
                </pre>
              </div>
            </div>
          )}

          {/* TAB 4: Android Studio & Gradle */}
          {activeTab === 'gradle' && (
            <div className="space-y-4 animate-in fade-in duration-100">
              <div className="p-4 rounded-2xl bg-amber-950/30 border border-amber-500/30 space-y-1.5">
                <div className="flex items-center gap-2">
                  <Package className="w-4 h-4 text-amber-400" />
                  <h3 className="text-sm font-bold text-white">Native Android Studio & Gradle Project</h3>
                </div>
                <p className="text-xs text-zinc-300">
                  This workspace contains the complete Kotlin/Gradle keyboard structure in <span className="font-mono text-amber-300">/app</span> and <span className="font-mono text-amber-300">build.gradle.kts</span>.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-zinc-800/40 border border-zinc-700/60 space-y-2.5 text-xs text-zinc-300">
                <h4 className="font-bold text-white uppercase tracking-wider text-[11px]">How to compile the APK on your machine:</h4>
                <ol className="space-y-2 list-decimal list-inside">
                  <li>In AI Studio top-right menu, click <strong>Settings</strong> → <strong>Export as ZIP</strong> or <strong>Export to GitHub</strong>.</li>
                  <li>Download and extract the files on your computer.</li>
                  <li>
                    Open a terminal in the folder and run:
                    <div className="mt-1 p-2 rounded-lg bg-zinc-950 font-mono text-emerald-400 border border-zinc-800 flex items-center justify-between">
                      <span>./gradlew assembleDebug</span>
                      <button
                        onClick={() => copyToClipboard('./gradlew assembleDebug', 'gradle')}
                        className="text-zinc-400 hover:text-zinc-200"
                      >
                        {copiedCommand === 'gradle' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                      </button>
                    </div>
                  </li>
                  <li>Your compiled APK will be at: <span className="font-mono text-amber-300">app/build/outputs/apk/debug/app-debug.apk</span>.</li>
                </ol>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-3.5 border-t border-zinc-800 bg-zinc-900/90 flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-1.5 text-xs text-zinc-400">
            <Info className="w-3.5 h-3.5 text-blue-400" />
            <span>PWA & APK ready for Android 8.0 through Android 15+</span>
          </div>
          <button
            id="btn-close-apk-modal-done"
            onClick={() => setActiveModal(null)}
            className="px-4 py-2 rounded-xl bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-xs font-semibold transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
