import React, { useState, useEffect } from 'react';
import { Play, Pause, RefreshCw, Zap, Skull, Monitor, Activity, FileText } from 'lucide-react';

// --- STYLES & ANIMATIONS ---
const styles = `
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&family=Press+Start+2P&display=swap');

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.blink-text { animation: blink 1s steps(1) infinite; }

@keyframes scanline {
    0% { transform: translateY(0); }
    100% { transform: translateY(100vh); }
}

.scanline-bar {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 10px;
    background: linear-gradient(to bottom, rgba(255,255,255,0), rgba(255,255,255,0.1) 50%, rgba(255,255,255,0));
    animation: scanline 4s linear infinite;
    pointer-events: none;
    z-index: 50;
}

/* CRT Screen Effect */
.crt-overlay {
    background: radial-gradient(circle, rgba(18, 16, 16, 0) 60%, rgba(0,0,0,0.6) 100%);
    pointer-events: none;
}

/* Custom Scrollbar */
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { bg-black; }
::-webkit-scrollbar-thumb { background: #4b5563; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #d946ef; }
`;

// Helper Components
const NavButton = ({ active, onClick, icon: Icon, label }) => (
    <button 
        onClick={onClick}
        className={`
            flex items-center justify-center gap-2 flex-1 py-4 
            transition-all duration-200 uppercase text-xs tracking-wider font-bold
            ${active 
                ? 'bg-fuchsia-900/40 text-fuchsia-400 border-t-2 border-fuchsia-500 shadow-[0_-4px_10px_rgba(217,70,239,0.3)]' 
                : 'bg-black text-gray-600 hover:text-gray-300 border-t-2 border-gray-900 hover:bg-gray-900'}
        `}
    >
        <Icon size={16} />
        {label}
    </button>
);

const ActionBtn = ({ onClick, label, color = "cyan", icon: Icon }) => {
    // Cyberpunk button styles
    const colors = {
        cyan: "border-cyan-500 text-cyan-400 hover:shadow-[0_0_15px_rgba(6,182,212,0.5)] bg-cyan-950/30",
        pink: "border-fuchsia-500 text-fuchsia-400 hover:shadow-[0_0_15px_rgba(217,70,239,0.5)] bg-fuchsia-950/30",
        orange: "border-orange-500 text-orange-400 hover:shadow-[0_0_15px_rgba(249,115,22,0.5)] bg-orange-950/30",
        red: "border-red-600 text-red-500 hover:shadow-[0_0_15px_rgba(220,38,38,0.5)] bg-red-950/30",
        green: "border-emerald-500 text-emerald-400 hover:shadow-[0_0_15px_rgba(16,185,129,0.5)] bg-emerald-950/30",
    };

    return (
        <button 
            onClick={onClick}
            className={`
                w-full py-3 px-4 border border-l-4 ${colors[color]}
                font-['Jersey_10'] text-lg tracking-wide
                flex items-center justify-between group transition-all
            `}
        >
            <span>{label}</span>
            {Icon && <Icon size={18} className="opacity-70 group-hover:opacity-100 transition-opacity" />}
        </button>
    );
};

export default function GameOfLife() {
    const [view, setView] = useState('menu'); // menu, game, summary
    const [gameState, setGameState] = useState(null);
    const [summary, setSummary] = useState(null);
    const [loadingSummary, setLoadingSummary] = useState(false);
    const [isConnected, setIsConnected] = useState(true);

    // Inject styles
    useEffect(() => {
        const styleTag = document.createElement('style');
        styleTag.textContent = styles;
        document.head.appendChild(styleTag);
        return () => document.head.removeChild(styleTag);
    }, []);

    // API Helper
    const api = async (endpoint) => {
        try {
            await fetch(`http://localhost:8080/api/simulation/${endpoint}`, { method: 'POST' });
            setIsConnected(true);
        } catch { setIsConnected(false); }
    };

    // Polling for Game State
    useEffect(() => {
        let interval;
        if (view === 'game') {
            interval = setInterval(() => {
                fetch('http://localhost:8080/api/simulation/status')
                    .then(res => { if(res.ok) setIsConnected(true); return res.json(); })
                    .then(data => setGameState(data))
                    .catch(() => setIsConnected(false));
            }, 100); // 100ms update rate is smoother
        }
        return () => clearInterval(interval);
    }, [view]);

    // Summary Fetch
    useEffect(() => {
        if (view === 'summary') {
            setLoadingSummary(true);
            setSummary(null);
            fetch('http://localhost:8080/api/simulation/ai/summary')
                .then(res => res.json())
                .then(data => { setSummary(data); setLoadingSummary(false); })
                .catch(() => { setSummary({ aiAnalysis: "CONNECTION TO AI MAINFRAME FAILED." }); setLoadingSummary(false); });
        }
    }, [view]);

    const handleAddFood = () => {
        api('addFood');
        // Optimistic UI update
        if (gameState) setGameState(prev => ({ ...prev, availableFood: (prev.availableFood||0) + 10 }));
    };

    return (
        <div className="min-h-screen bg-[#050505] text-gray-300 font-sans selection:bg-fuchsia-500/30 overflow-hidden flex items-center justify-center p-4">
            
            {/* --- MAIN TERMINAL CONTAINER --- */}
            <div className="w-[1200px] h-[850px] bg-black border border-gray-800 rounded-xl shadow-2xl overflow-hidden flex flex-col relative">
                
                {/* CRT Overlay Effects */}
                <div className="scanline-bar"></div>
                <div className="absolute inset-0 crt-overlay pointer-events-none z-40"></div>
                
                {/* --- HEADER --- */}
                <header className="h-16 bg-black border-b border-gray-800 grid grid-cols-3 items-center px-8 z-30 shrink-0">
                    <div className="flex items-center gap-2 pl-2">
                        <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-emerald-500 shadow-[0_0_8px_#10b981]' : 'bg-red-500 animate-pulse'}`}></div>
                    </div>
                    
                    <div className="flex justify-center gap-12 font-['Jersey_10'] text-2xl">
                        <div className="flex gap-3 text-fuchsia-500">
                            <span className="text-gray-600 text-lg align-middle pt-1">CELL</span>
                            <span>{gameState?.aliveCount || "000"}</span>
                        </div>
                        <div className="flex gap-3 text-cyan-500">
                            <span className="text-gray-600 text-lg align-middle pt-1">FOOD</span>
                            <span>{gameState?.availableFood || "000"}</span>
                        </div>
                    </div>
                    
                    <div className="w-24 text-right">
                         <span className="text-[10px] text-gray-600 font-mono">{new Date().toLocaleTimeString()}</span>
                    </div>
                </header>

                {/* --- MAIN CONTENT AREA --- */}
                <main className="flex-1 flex overflow-hidden relative z-20">
                    
                    {/* LEFT SIDEBAR (Controls) - Only visible in Game Mode */}
                    {view === 'game' && (
                        <aside className="w-64 bg-[#0a0a0a] border-r border-gray-800 p-4 flex flex-col gap-6 transition-all">
                            <div className="p-4 bg-gray-900/50 rounded border border-gray-800 text-center">
                                <div className="text-[10px] uppercase text-gray-500 mb-1">Status</div>
                                <div className={`font-['Jersey_10'] text-2xl tracking-wide ${gameState?.paused ? 'text-orange-500' : 'text-emerald-500'}`}>
                                    {gameState?.paused ? "PAUSED" : "ACTIVE"}
                                </div>
                            </div>
                            
                            <div className="space-y-3">
                                <div className="text-[10px] font-mono text-gray-600 uppercase mb-2">Simulation</div>
                                <ActionBtn onClick={() => api('start')} label="START" color="green" icon={Play} />
                                <ActionBtn onClick={() => api('togglePause')} label={gameState?.paused ? "RESUME" : "PAUSE"} color="orange" icon={Pause} />
                                <ActionBtn onClick={() => {api('reset'); api('start')}} label="RESET" color="cyan" icon={RefreshCw} />
                            </div>

                            <div className="space-y-3">
                                <div className="text-[10px] font-mono text-gray-600 uppercase mb-2">Injectors</div>
                                <ActionBtn onClick={() => api('addCell?type=asexual')} label="+ ASEXUAL" color="cyan" />
                                <ActionBtn onClick={() => api('addCell?type=sexual')} label="+ SEXUAL" color="pink" />
                                <ActionBtn onClick={handleAddFood} label="DROP FOOD" color="orange" icon={Zap} />
                            </div>

                            <div className="mt-auto pt-4 border-t border-gray-800">
                                <ActionBtn onClick={() => { if(confirm('Terminate?')) api('killAll'); }} label="KILL ALL" color="red" icon={Skull} />
                            </div>
                        </aside>
                    )}

                    {/* CENTRAL VIEWPORT */}
                    <div className="flex-1 relative bg-gray-950 flex flex-col overflow-hidden">
                        
                        {/* VIEW: SPLASH SCREEN (MENU) */}
                        {view === 'menu' && (
                            <div className="flex-1 flex flex-col items-center justify-center relative overflow-hidden">
                                {/* Decorative grid background */}
                                <div className="absolute inset-0 opacity-10" 
                                     style={{backgroundImage: 'linear-gradient(#333 1px, transparent 1px), linear-gradient(90deg, #333 1px, transparent 1px)', backgroundSize: '40px 40px'}}>
                                </div>
                                
                                <div className="z-10 text-center space-y-8 p-12 bg-black/80 backdrop-blur-sm border border-gray-800 rounded-2xl shadow-2xl">
                                    <div>
                                        <h1 className="font-['Jersey_10'] text-8xl text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-fuchsia-500 animate-pulse drop-shadow-[0_0_15px_rgba(217,70,239,0.5)]">
                                            LIFE SIM
                                        </h1>
                                    </div>

                                    <div className="space-y-2 font-mono text-xs text-gray-400">
                                        <p>Cellular evolution</p>
                                        <p>LOCAL LLM INTEGRATION ACTIVE</p>
                                    </div>

                                    <button 
                                        onClick={() => { api('start'); setView('game'); }}
                                        className="px-12 py-4 bg-fuchsia-600 hover:bg-fuchsia-500 text-black font-bold font-['Jersey_10'] text-2xl tracking-widest rounded transition-all hover:scale-105 shadow-[0_0_20px_rgba(217,70,239,0.6)]"
                                    >
                                        INITIALIZE
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* VIEW: GAME UI */}
                        {view === 'game' && (
                            <div className="flex-1 relative">
                                {/* Grid Pattern */}
                                <div className="absolute inset-0 opacity-20 pointer-events-none" 
                                    style={{
                                        backgroundImage: 'radial-gradient(#4b5563 1px, transparent 1px)', 
                                        backgroundSize: '20px 20px'
                                    }}>
                                </div>

                                {/* Render Food */}
                                {gameState?.food?.map((f, i) => (
                                    <div key={`food-${i}`} 
                                        className="absolute w-1.5 h-1.5 bg-orange-500 rounded-full shadow-[0_0_8px_orange]"
                                        style={{ left: `${f.x}%`, top: `${f.y}%`, transition: 'all 0.2s ease-out' }} 
                                    />
                                ))}

                                {/* Render Cells */}
                                {gameState?.activeCells?.map(c => {
                                    const isSexual = c.type === 'Sexual';
                                    const colorClass = isSexual ? 'bg-fuchsia-500 shadow-[0_0_12px_magenta]' : 'bg-cyan-400 shadow-[0_0_12px_cyan]';
                                    
                                    return (
                                        <div key={c.id} 
                                            className={`absolute w-4 h-4 rounded-full ${colorClass} transition-all duration-300 flex items-center justify-center opacity-90 hover:opacity-100 hover:scale-125 z-10 cursor-crosshair`}
                                            style={{ left: `${c.x}%`, top: `${c.y}%` }}
                                            title={`ID: ${c.id} | ${c.type} | Hunger: ${c.isHungry} | Repro: ${c.isReproducing}`}
                                        >
                                            {/* Inner Nucleus */}
                                            <div className="w-1.5 h-1.5 bg-black/50 rounded-full"></div>
                                            
                                            {/* Status Indicators */}
                                            {c.isHungry && <div className="absolute -top-3 w-1 h-1 bg-red-500 rounded-full animate-bounce"></div>}
                                            {c.isReproducing && <div className="absolute -top-3 w-1 h-1 bg-white rounded-full animate-ping"></div>}
                                        </div>
                                    );
                                })}

                                {!gameState?.activeCells?.length && (
                                    <div className="absolute inset-0 flex items-center justify-center">
                                        <div className="px-6 py-3 bg-gray-900 border border-gray-700 text-gray-400 font-mono text-sm rounded pointer-events-none">
                                            [ ENVIRONMENT STERILE ]
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* VIEW: SUMMARY LOGS */}
                        {view === 'summary' && (
                            <div className="flex-1 p-12 overflow-y-auto bg-[#080808]">
                                <div className="max-w-4xl mx-auto space-y-8">
                                    <div className="border-b border-gray-800 pb-4 mb-4">
                                        <h2 className="font-['Jersey_10'] text-4xl text-gray-200">SIMULATION_LOGS</h2>
                                        <p className="font-mono text-xs text-gray-500 mt-1">AI ANALYSIS REPORT</p>
                                    </div>

                                    {!summary && loadingSummary && (
                                        <div className="p-12 border border-dashed border-gray-800 rounded text-center text-cyan-500 animate-pulse font-mono text-sm">
                                            &gt; ESTABLISHING NEURAL LINK...
                                        </div>
                                    )}

                                    {summary && (
                                        <div className="animate-in fade-in duration-500 space-y-8">
                                            <div className="bg-gray-900/50 p-6 rounded border-l-4 border-fuchsia-500">
                                                <h3 className="text-gray-400 font-mono text-xs uppercase mb-4">Executive Summary</h3>
                                                <p className="font-mono text-sm leading-relaxed text-gray-200 whitespace-pre-wrap">
                                                    {summary.aiAnalysis}
                                                </p>
                                            </div>

                                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                                <div className="bg-gray-900 p-6 rounded border border-gray-800 text-center">
                                                    <div className="text-fuchsia-500 font-['Jersey_10'] text-5xl mb-2">{summary.generations}</div>
                                                    <div className="text-gray-500 text-xs font-mono uppercase">Sexual Events</div>
                                                </div>
                                                <div className="bg-gray-900 p-6 rounded border border-gray-800 text-center">
                                                    <div className="text-cyan-500 font-['Jersey_10'] text-5xl mb-2">{summary.divisions}</div>
                                                    <div className="text-gray-500 text-xs font-mono uppercase">Asexual Divisions</div>
                                                </div>
                                                <div className="bg-gray-900 p-6 rounded border border-gray-800 text-center">
                                                    <div className="text-emerald-500 font-['Jersey_10'] text-5xl mb-2">{summary.aliveCount}</div>
                                                    <div className="text-gray-500 text-xs font-mono uppercase">Total Survivors</div>
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}

                    </div>
                </main>

                {/* --- NAVIGATION FOOTER --- */}
                <footer className="h-16 bg-black flex border-t border-gray-800 z-30 shrink-0">
                    <NavButton active={view==='menu'} onClick={()=>setView('menu')} icon={Monitor} label="MAIN SYSTEM" />
                    <NavButton active={view==='game'} onClick={()=>setView('game')} icon={Activity} label="SIMULATION" />
                    <NavButton active={view==='summary'} onClick={()=>setView('summary')} icon={FileText} label="DATA LOGS" />
                </footer>

            </div>
        </div>
    );
}
