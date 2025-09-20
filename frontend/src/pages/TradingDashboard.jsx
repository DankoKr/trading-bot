import { useState, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import PortfolioOverview from '../components/PortfolioOverview';
import TradeHistoryTable from '../components/TradeHistoryTable';
import HoldingsTable from '../components/HoldingsTable';
import BotControlPanel from '../components/BotControlPanel';

const TradingDashboard = () => {
  const { user, logout } = useAuth();
  const [botStatus, setBotStatus] = useState(null);
  const portfolioRef = useRef();
  const tradesRef = useRef();
  const holdingsRef = useRef();

  const handleBotStatusChange = (status) => {
    setBotStatus(status);

    if (portfolioRef.current?.refreshData) {
      portfolioRef.current.refreshData();
    }
    if (tradesRef.current?.refreshData) {
      tradesRef.current.refreshData();
    }
    if (holdingsRef.current?.refreshData) {
      holdingsRef.current.refreshData();
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  return (
    <div className='min-h-screen bg-gray-50 p-6 trading-dashboard'>
      <div className='max-w-7xl mx-auto space-y-8'>
        <div className='flex justify-between items-center'>
          <h1 className='text-3xl font-bold text-gray-900'>
            Crypto Trading Bot Dashboard
          </h1>
          <div className='text-sm text-gray-500'>
            Last updated: {new Date().toLocaleString()}
          </div>
        </div>

        <header className='dashboard-header'>
          <div className='header-left'>
            <h1>Auto Trading Bot</h1>
          </div>
          <div className='header-right'>
            {user && (
              <div className='user-info'>
                <span>Welcome, {user.firstName || user.username}!</span>
                <button onClick={handleLogout} className='logout-btn'>
                  Logout
                </button>
              </div>
            )}
          </div>
        </header>

        <BotControlPanel onStatusChange={handleBotStatusChange} />
        <PortfolioOverview ref={portfolioRef} />
        <HoldingsTable ref={holdingsRef} />
        <TradeHistoryTable ref={tradesRef} />
      </div>
    </div>
  );
};

export default TradingDashboard;
