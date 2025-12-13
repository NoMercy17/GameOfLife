import React, { useState } from 'react';
import { Play, FileText, Plus, Apple, RotateCcw } from 'lucide-react';
import gameIcon from './assets/game_icon.png';

const Button = ({ onClick, className, children }) => (
    <button
        onClick={onClick}
        className={`px-4 py-2 rounded-lg transition-colors flex items-center gap-2 ${className}`}
    >
        {children}
    </button>
);

export default function GameOfLife() {
    const [activeView, setActiveView] = useState('menu');
    const [gameStarted, setGameStarted] = useState(false);

    const handleStart = () => {
        setActiveView('game');
        setGameStarted(true);
    };

    const handleSummary = () => setActiveView('summary');
    const handleStop = () => setGameStarted(false);
    const goToMenu = () => setActiveView('menu');

    const handleReset = () => {
        setGameStarted(false);
        // TODO: later
    };

    const handleAddCells = () => {
        // TODO: later
        console.log('Add cells clicked');
    };

    const handleAddFood = () => {
        // TODO: later
        console.log('Add food clicked');
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 to-indigo-50 p-6 flex flex-col">
            {activeView === 'menu' && (
                <div className="flex-1 flex items-center justify-center">
                    <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
                        <div className="text-center mb-8">
                            <div className="flex items-center justify-center gap-3 mb-2">
                                <img src={gameIcon} alt="Game of Life" className="w-10 h-10" />
                                <h1 className="text-3xl font-bold text-gray-800">Game of Life</h1>
                            </div>
                        </div>
                        <div className="space-y-4">
                            <h2 className="text-xl font-semibold text-gray-800 text-center">Main Menu</h2>
                            <Button onClick={handleStart} className="w-full bg-green-600 text-white py-4 text-lg font-medium hover:bg-green-700 justify-center">
                                <Play size={24} /> Start
                            </Button>
                            <Button onClick={handleSummary} className="w-full bg-blue-600 text-white py-4 text-lg font-medium hover:bg-blue-700 justify-center">
                                <FileText size={24} /> Summary
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            {activeView === 'game' && (
                <div className="flex-1 max-w-7xl mx-auto w-full">
                    <div className="bg-white rounded-lg shadow-lg p-6">
                        <div className="flex justify-between items-center mb-6 pb-4 border-b">
                            <h2 className="text-xl font-semibold text-gray-800">
                                Simulation {gameStarted ? '(Running)' : '(Paused)'}
                            </h2>
                            <div className="flex gap-2">
                                <Button onClick={handleAddCells} className="bg-purple-600 text-white hover:bg-purple-700"><Plus size={20} /> Add Cells</Button>
                                <Button onClick={handleAddFood} className="bg-orange-600 text-white hover:bg-orange-700"><Apple size={20} /> Add Food</Button>
                                <Button onClick={handleStop} className="bg-red-600 text-white hover:bg-red-700">Stop</Button>
                                <Button onClick={handleReset} className="bg-gray-600 text-white hover:bg-gray-700"><RotateCcw size={20} /> Reset</Button>
                                <Button onClick={goToMenu} className="bg-gray-800 text-white hover:bg-gray-900">Menu</Button>
                            </div>
                        </div>
                        <div className="bg-gray-100 rounded-lg p-8 min-h-[500px] flex items-center justify-center">
                            <div className="text-center text-gray-500">
                                <img src={gameIcon} alt="Game of Life" className="w-16 h-16 mx-auto mb-4 opacity-50" />
                                <p className="text-lg">Game grid will be displayed here</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {activeView === 'summary' && (
                <div className="flex-1 max-w-7xl mx-auto w-full">
                    <div className="bg-white rounded-lg shadow-lg p-6">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-2xl font-semibold text-gray-800">Summary</h2>
                            <Button onClick={goToMenu} className="bg-gray-600 text-white hover:bg-gray-700">Back to Menu</Button>
                        </div>
                        <div className="space-y-4">
                            <div className="bg-gradient-to-r from-purple-50 to-indigo-50 p-6 rounded-lg">
                                <h3 className="font-bold text-lg mb-3 text-purple-800">AI Game Summary</h3>
                                <p className="text-gray-500 italic">No game has been played yet.</p>
                            </div>
                            <div className="bg-gradient-to-r from-green-50 to-emerald-50 p-6 rounded-lg">
                                <h3 className="font-bold text-lg mb-3 text-green-800">Statistics</h3>
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                    {['Total Cells', 'Generations', 'Food Items', 'Alive'].map((label) => (
                                        <div key={label} className="text-center">
                                            <p className="text-3xl font-bold text-gray-800">0</p>
                                            <p className="text-sm text-gray-600">{label}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
