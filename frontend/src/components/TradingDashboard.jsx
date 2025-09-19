import { useState, useRef } from 'react';
import PortfolioOverview from './PortfolioOverview';
import TradeHistoryTable from './TradeHistoryTable';
import HoldingsTable from './HoldingsTable';
import BotControlPanel from './BotControlPanel';

export default function TradingDashboard() {
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

  return (
    <div className='min-h-screen bg-gray-50 p-6'>
      <div className='max-w-7xl mx-auto space-y-8'>
        <div className='flex justify-between items-center'>
          <h1 className='text-3xl font-bold text-gray-900'>
            Crypto Trading Bot Dashboard
          </h1>
          <div className='text-sm text-gray-500'>
            Last updated: {new Date().toLocaleString()}
          </div>
        </div>

        <BotControlPanel onStatusChange={handleBotStatusChange} />
        <PortfolioOverview ref={portfolioRef} />
        <HoldingsTable ref={holdingsRef} />
        <TradeHistoryTable ref={tradesRef} />
      </div>
    </div>
  );
}
