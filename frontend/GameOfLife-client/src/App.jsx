import React, { useState, useEffect } from 'react';
import { Play, Pause, RefreshCw, Ghost, Zap, Skull } from 'lucide-react';

// --- NEON PURPLE ARCADE STYLES ---
const styles = `
@import url('https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap');

@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0; } 100% { opacity: 1; } }
.blink-text { animation: blink 1s infinite step-end; }

::-webkit-scrollbar { width: 10px; background: #000; }
::-webkit-scrollbar-thumb { background: #D946EF; border: 2px solid #000; }

.scanlines {
    background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%), linear-gradient(90deg, rgba(255, 0, 255, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
    background-size: 100% 2px, 3px 100%;
    pointer-events: none;
}
`;

const ArcadeBtn = ({ onClick, label, color, icon: Icon }) => {
    const colors = {
        red: "border-red-500 text-red-500 hover:bg-red-900/50 shadow-[4px_4px_0_#991b1b]",
        pink: "border-fuchsia-500 text-fuchsia-500 hover:bg-fuchsia-900/50 shadow-[4px_4px_0_#a21caf]",
        cyan: "border-cyan-400 text-cyan-400 hover:bg-cyan-900/50 shadow-[4px_4px_0_#0e7490]",
        orange: "border-orange-400 text-orange-400 hover:bg-orange-900/50 shadow-[4px_4px_0_#c2410c]",
        purple: "border-purple-500 text-purple-400 hover:bg-purple-900/50 shadow-[4px_4px_0_#6b21a8]",
        yellow: "border-yellow-400 text-yellow-400 hover:bg-yellow-900/50 shadow-[4px_4px_0_#a16207]"
    };

    return (
        <button 
            onClick={onClick}
            className={`
                w-full py-4 px-2 border-4 ${colors[color]} 
                font-['Press_Start_2P'] text-[10px] uppercase tracking-widest
                flex items-center justify-center gap-3 transition-all active:translate-y-1 active:shadow-none
            `}
        >
            {Icon && <Icon size={14} />} {label}
        </button>
    );
};

export default function GameOfLife() {
    const [view, setView] = useState('menu');
    const [gameState, setGameState] = useState(null);
    const [summary, setSummary] = useState(null);
    const [loadingSummary, setLoadingSummary] = useState(false);
    const [isConnected, setIsConnected] = useState(true);

    useEffect(() => {
        const style = document.createElement('style');
        style.textContent = styles;
        document.head.appendChild(style);
    }, []);

    const api = async (endpoint) => {
        try {
            await fetch(`http://localhost:8080/api/simulation/${endpoint}`, { method: 'POST' });
            setIsConnected(true);
        } catch { setIsConnected(false); }
    };

    const handleAddFood = () => {
        api('addFood');
        if (gameState) {
            const predictedAdd = Math.max(5, (gameState.aliveCount || 0) + 5);
            setGameState(prev => ({
                ...prev, 
                availableFood: (prev.availableFood || 0) + predictedAdd
            }));
        }
    };

    useEffect(() => {
        let interval;
        if (view === 'game') {
            interval = setInterval(() => {
                fetch('http://localhost:8080/api/simulation/status')
                    .then(res => { if(res.ok) setIsConnected(true); return res.json(); })
                    .then(data => setGameState(data))
                    .catch(() => setIsConnected(false));
            }, 50);
        }
        return () => clearInterval(interval);
    }, [view]);

    useEffect(() => {
        if (view === 'summary') {
            setLoadingSummary(true);
            setSummary(null);
            fetch('http://localhost:8080/api/simulation/ai/summary')
                .then(res => res.json())
                .then(data => { setSummary(data); setLoadingSummary(false); })
                .catch(() => { setSummary({ aiAnalysis: "AI OFFLINE" }); setLoadingSummary(false); });
        }
    }, [view]);

    const handleTerminate = async () => {
        if(window.confirm("TERMINATE SIMULATION?")) {
            await api('killAll');
            setGameState(prev => ({...prev, activeCells: [], availableFood: 0}));
        }
    };

    return (
        <div className="min-h-screen bg-black text-white font-['Press_Start_2P'] flex items-center justify-center p-4 overflow-hidden relative selection:bg-fuchsia-500 selection:text-white">
            <div className="absolute inset-0 scanlines z-50"></div>
            
            {/* CABINET FRAME: Rounded-LG (Smaller curve) + Overflow Hidden */}
            <div className="w-[1100px] h-[800px] border-[6px] border-fuchsia-600 rounded-lg bg-[#050005] relative flex flex-col shadow-[0_0_80px_rgba(217,70,239,0.3)] overflow-hidden z-40">
                
                {/* --- HEADER: Increased padding to px-16 (Safe Zone) --- */}
                <div className="h-20 flex justify-between items-center px-16 border-b-4 border-fuchsia-800 bg-black/50 z-50">
                    <div className="flex gap-16">
                        <div>
                            <div className="text-[10px] text-fuchsia-400 mb-1">POPULATION</div>
                            <div className="text-2xl text-white drop-shadow-[2px_2px_0_#d946ef]">{gameState?.aliveCount || "0"}</div>
                        </div>
                        <div>
                            <div className="text-[10px] text-orange-400 mb-1">RESOURCES</div>
                            <div className="text-2xl text-white drop-shadow-[2px_2px_0_#ea580c]">{gameState?.availableFood || "0"}</div>
                        </div>
                    </div>
                    
                    <div>
                        {!isConnected ? (
                            <div className="text-[10px] text-red-500 blink-text border-2 border-red-500 px-2 py-1 bg-black">NO SIGNAL</div>
                        ) : (
                            <div className="text-[10px] text-green-500">ONLINE</div>
                        )}
                    </div>
                </div>

                <div className="flex-1 flex p-6 gap-6 overflow-hidden">
                    
                    {/* --- LEFT CONTROLS --- */}
                    {view === 'game' && (
                        <div className="w-72 flex flex-col gap-6">
                            
                            <div className="border-4 border-fuchsia-900 bg-black p-4 text-center shadow-inner">
                                <div className="text-[8px] text-gray-500 mb-2">SYSTEM STATUS</div>
                                <div className={`text-sm ${gameState?.paused ? "text-orange-500 blink-text" : "text-green-400"}`}>
                                    {gameState?.paused ? "PAUSED" : "RUNNING"}
                                </div>
                            </div>

                            <div className="space-y-6">
                                <ArcadeBtn onClick={() => api('start')} label="START" color="green" icon={Play} />
                                <ArcadeBtn onClick={() => api('togglePause')} label={gameState?.paused ? "RESUME" : "PAUSE"} color="yellow" icon={Pause} />
                                <ArcadeBtn onClick={() => { api('reset'); api('start'); }} label="RESET" color="purple" icon={RefreshCw} />
                            </div>

                            <div className="space-y-4 mt-2 pt-4 border-t-2 border-fuchsia-900/50">
                                <div className="text-[8px] text-center text-fuchsia-700">- INJECT -</div>
                                <ArcadeBtn onClick={() => api('addCell?type=asexual')} label="ASEXUAL" color="cyan" />
                                <ArcadeBtn onClick={() => api('addCell?type=sexual')} label="SEXUAL" color="pink" />
                                <ArcadeBtn onClick={handleAddFood} label="FEED" color="orange" icon={Zap} />
                            </div>

                            <div className="mt-auto">
                                <ArcadeBtn onClick={handleTerminate} label="TERMINATE" color="red" icon={Skull} />
                            </div>
                        </div>
                    )}

                    {/* --- CENTER SCREEN --- */}
                    <div className="flex-1 border-[4px] border-fuchsia-600 rounded-lg p-1 relative bg-black flex flex-col shadow-[inset_0_0_20px_rgba(217,70,239,0.2)]">
                        
                        {view === 'menu' && (
                            <div className="flex-1 flex flex-col items-center justify-center gap-12 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-fuchsia-900/20 via-black to-black">
                                <h1 className="text-5xl text-fuchsia-500 drop-shadow-[4px_4px_0_#fff] italic tracking-tighter animate-pulse">
                                    GAME OF LIFE
                                </h1>
                                <div className="space-y-6 text-center">
                                    <div className="text-cyan-400 text-xs">CEBP // PROJECT 2</div>
                                    <div className="text-pink-400 text-xs">SYSTEM BLANAO V2.0</div>
                                </div>
                                <button 
                                    onClick={() => { api('start'); setView('game'); }}
                                    className="mt-8 px-12 py-6 bg-fuchsia-600 text-white text-sm hover:bg-fuchsia-500 hover:scale-105 transition-all shadow-[0_0_30px_#d946ef]"
                                >
                                    INSERT COIN
                                </button>
                            </div>
                        )}

                        {view === 'game' && (
                            <div className="flex-1 relative overflow-hidden">
                                <div className="absolute inset-0 opacity-20" 
                                    style={{backgroundImage: 'radial-gradient(#d946ef 1px, transparent 1px)', backgroundSize: '30px 30px'}}>
                                </div>
                                {gameState?.food?.map((f, i) => (
                                    <div key={i} className="absolute w-2 h-2 bg-orange-500 rounded-sm shadow-[0_0_5px_orange]"
                                        style={{left: `${f.x}%`, top: `${f.y}%`}} />
                                ))}
                                {gameState?.activeCells?.map(c => {
                                    let ghostColor = "bg-cyan-400 shadow-[0_0_10px_cyan]"; 
                                    if (c.type === 'Sexual') ghostColor = "bg-pink-500 shadow-[0_0_10px_magenta]"; 
                                    if (c.isHungry) ghostColor = "bg-blue-700 border border-white animate-pulse";

                                    return (
                                        <div key={c.id} 
                                            className={`absolute w-6 h-6 rounded-t-full ${ghostColor} transition-all duration-100 flex items-center justify-center`}
                                            style={{left: `${c.x}%`, top: `${c.y}%`}}
                                        >
                                            <div className="flex gap-1 mt-1">
                                                <div className="w-1.5 h-1.5 bg-white rounded-full"><div className="w-0.5 h-0.5 bg-blue-900 ml-0.5 mt-0.5"></div></div>
                                                <div className="w-1.5 h-1.5 bg-white rounded-full"><div className="w-0.5 h-0.5 bg-blue-900 ml-0.5 mt-0.5"></div></div>
                                            </div>
                                        </div>
                                    );
                                })}
                                {!gameState?.activeCells?.length && (
                                    <div className="absolute inset-0 flex items-center justify-center">
                                        <div className="text-fuchsia-500 text-xs blink-text">SYSTEM READY</div>
                                    </div>
                                )}
                            </div>
                        )}

                        {view === 'summary' && (
                            <div className="flex-1 p-8 text-[10px] leading-loose text-fuchsia-100 overflow-auto">
                                <div className="text-center text-fuchsia-400 mb-8 text-sm border-b-4 border-fuchsia-500 pb-4">
                                    MISSION REPORT
                                </div>
                                {loadingSummary ? (
                                    <div className="text-center mt-20 blink-text text-cyan-400">ACCESSING MAINFRAME...</div>
                                ) : (
                                    <>
                                        <div className="whitespace-pre-wrap mb-12 text-white font-mono">{summary?.aiAnalysis}</div>
                                        <div className="grid grid-cols-3 gap-6 text-center">
                                            <div className="border-2 border-cyan-500 p-4 bg-cyan-900/20">
                                                <div className="text-cyan-400 mb-2">SURVIVORS</div>
                                                <div className="text-xl">{summary?.aliveCount}</div>
                                            </div>
                                            <div className="border-2 border-green-500 p-4 bg-green-900/20">
                                                <div className="text-green-400 mb-2">DIVISIONS (A)</div>
                                                <div className="text-xl">{summary?.divisions}</div>
                                            </div>
                                            <div className="border-2 border-pink-500 p-4 bg-pink-900/20">
                                                <div className="text-pink-400 mb-2">REPRODUCTIONS (S)</div>
                                                <div className="text-xl">{summary?.generations}</div>
                                            </div>
                                        </div>
                                    </>
                                )}
                            </div>
                        )}
                        
                        {/* --- FOOTER: Increased padding to px-16 (Safe Zone) --- */}
                        <div className="h-10 border-t-4 border-fuchsia-800 flex justify-between items-center px-16 bg-black z-50">
                             <button onClick={() => setView('menu')} className="text-[8px] text-fuchsia-600 hover:text-white hover:underline">MAIN MENU</button>
                             <button onClick={() => setView('game')} className="text-[8px] text-fuchsia-600 hover:text-white hover:underline">SIMULATION</button>
                             <button onClick={() => setView('summary')} className="text-[8px] text-fuchsia-600 hover:text-white hover:underline">VIEW LOGS</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}