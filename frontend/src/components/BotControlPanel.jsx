import { useState, useEffect } from 'react';
import {
  Play,
  Pause,
  Square,
  RotateCcw,
  Activity,
  AlertCircle,
} from 'lucide-react';
import ApiService from '../services/api';
import BotResultModal from './BotResultModal';

export default function BotControlPanel({ onStatusChange }) {
  const [botStatus, setBotStatus] = useState({
    status: 'ACTIVE',
    lastStatusChange: new Date(),
    statusChangeReason: 'Bot initialized',
    message: '',
    success: true,
  });
  const [loading, setLoading] = useState(false);
  const [selectedMode, setSelectedMode] = useState('TRADING');
  const [selectedCoins, setSelectedCoins] = useState('bitcoin,ethereum,solana');
  const [apiStatus, setApiStatus] = useState(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [modalResult, setModalResult] = useState(null);
  const [modalTitle, setModalTitle] = useState('');

  useEffect(() => {
    fetchBotStatus();
    const interval = setInterval(fetchBotStatus, 5000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    fetchApiStatus();
  }, []);

  const fetchBotStatus = async () => {
    try {
      const status = await ApiService.getBotStatus();
      setBotStatus(status);
      if (onStatusChange) onStatusChange(status);
    } catch (error) {
      console.error('Failed to fetch bot status:', error);
    }
  };

  const fetchApiStatus = async () => {
    try {
      const status = await ApiService.getApiStatus();
      setApiStatus(status);
    } catch (error) {
      console.error('Failed to fetch API status:', error);
    }
  };

  const showResultModal = (result, title) => {
    setModalResult(result);
    setModalTitle(title);
    setModalOpen(true);
  };

  const handleBotAction = async (action, reason = '') => {
    setLoading(true);
    try {
      let result;
      switch (action) {
        case 'activate':
          result = await ApiService.activateBot(reason);
          break;
        case 'hold':
          result = await ApiService.holdBot(reason);
          break;
        case 'stop':
          result = await ApiService.stopBot(reason);
          break;
        case 'reset':
          result = await ApiService.resetBot(1000);
          break;
        default:
          throw new Error(`Unknown action: ${action}`);
      }
      setBotStatus(result);
      if (onStatusChange) onStatusChange(result);

      setTimeout(() => {
        if (onStatusChange) onStatusChange(result);
      }, 1000);
    } catch (error) {
      console.error(`Failed to ${action} bot:`, error);
    } finally {
      setLoading(false);
    }
  };

  const handleRunBot = async () => {
    setLoading(true);
    try {
      const result = await ApiService.runBot(selectedCoins, selectedMode);
      showResultModal(result, `${selectedMode} Mode - Bot Execution Results`);

      setTimeout(() => {
        if (onStatusChange)
          onStatusChange({ ...botStatus, timestamp: new Date() });
      }, 2000);
    } catch (error) {
      console.error('Failed to run bot:', error);
      showResultModal(
        {
          success: false,
          summary: `Failed to run bot: ${error.message}`,
          analyses: [],
        },
        'Bot Execution Error'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleBacktest = async () => {
    setLoading(true);
    try {
      const coinId = selectedCoins.split(',')[0];
      const result = await ApiService.runBacktest(coinId, 365, 1000);
      showResultModal(result, `Backtest Results - ${coinId.toUpperCase()}`);
    } catch (error) {
      console.error('Failed to run backtest:', error);
      showResultModal(
        { success: false, summary: `Failed to run backtest: ${error.message}` },
        'Backtest Error'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleHistoricalTraining = async () => {
    setLoading(true);
    try {
      const result = await ApiService.runHistoricalTraining(selectedCoins, 365);
      showResultModal(result, 'Historical Training Results');
    } catch (error) {
      console.error('Failed to run historical training:', error);
      showResultModal(
        {
          success: false,
          summary: `Failed to run historical training: ${error.message}`,
          analyses: [],
        },
        'Historical Training Error'
      );
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'text-green-600 bg-green-100';
      case 'ON_HOLD':
        return 'text-yellow-600 bg-yellow-100';
      case 'STOPPED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'ACTIVE':
        return <Activity className='w-4 h-4' />;
      case 'ON_HOLD':
        return <Pause className='w-4 h-4' />;
      case 'STOPPED':
        return <Square className='w-4 h-4' />;
      default:
        return <AlertCircle className='w-4 h-4' />;
    }
  };

  return (
    <>
      <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-xl font-semibold text-gray-900'>
            Bot Control Panel
          </h2>
          <div
            className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(
              botStatus.status
            )}`}
          >
            {getStatusIcon(botStatus.status)}
            <span className='ml-2'>{botStatus.status}</span>
          </div>
        </div>

        <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
          <div className='space-y-4'>
            <h3 className='text-lg font-medium text-gray-900'>Bot Controls</h3>

            <div className='grid grid-cols-2 gap-2'>
              <button
                onClick={() => handleBotAction('activate', 'Manual activation')}
                disabled={loading || botStatus.status === 'ACTIVE'}
                className='flex items-center justify-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed'
              >
                <Play className='w-4 h-4 mr-2' />
                Start
              </button>

              <button
                onClick={() => handleBotAction('hold', 'Manual pause')}
                disabled={loading || botStatus.status !== 'ACTIVE'}
                className='flex items-center justify-center px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700 disabled:opacity-50 disabled:cursor-not-allowed'
              >
                <Pause className='w-4 h-4 mr-2' />
                Pause
              </button>

              <button
                onClick={() => handleBotAction('stop', 'Manual stop')}
                disabled={loading}
                className='flex items-center justify-center px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed'
              >
                <Square className='w-4 h-4 mr-2' />
                Stop
              </button>

              <button
                onClick={() => handleBotAction('reset')}
                disabled={loading}
                className='flex items-center justify-center px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed'
              >
                <RotateCcw className='w-4 h-4 mr-2' />
                Reset
              </button>
            </div>
          </div>

          <div className='space-y-4'>
            <h3 className='text-lg font-medium text-gray-900'>
              Trading Configuration
            </h3>

            <div className='space-y-3'>
              <div>
                <label className='block text-sm font-medium text-gray-700 mb-1'>
                  Trading Mode
                </label>
                <select
                  value={selectedMode}
                  onChange={(e) => setSelectedMode(e.target.value)}
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                >
                  <option value='TRADING'>Live Trading</option>
                  <option value='TRAINING'>Training Mode</option>
                </select>
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-1'>
                  Cryptocurrencies
                </label>
                <input
                  type='text'
                  value={selectedCoins}
                  onChange={(e) => setSelectedCoins(e.target.value)}
                  placeholder='bitcoin,ethereum,solana'
                  className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
              </div>

              <div className='grid grid-cols-1 gap-2'>
                <button
                  onClick={handleRunBot}
                  disabled={loading}
                  className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50'
                >
                  Run Bot ({selectedMode})
                </button>

                <div className='grid grid-cols-2 gap-2'>
                  <button
                    onClick={handleBacktest}
                    disabled={loading}
                    className='px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 disabled:opacity-50'
                  >
                    Backtest
                  </button>

                  <button
                    onClick={handleHistoricalTraining}
                    disabled={loading}
                    className='px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50'
                  >
                    Historical
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        {apiStatus && (
          <div className='mt-6 p-4 bg-blue-50 rounded-md'>
            <div className='text-sm text-blue-800'>
              <p>
                <strong>API Status:</strong>{' '}
                {apiStatus.apiConnected ? '✅ Connected' : '❌ Disconnected'}
              </p>
              <p>
                <strong>API Key Type:</strong>{' '}
                {apiStatus.apiKeyType || 'None (Free tier)'}
              </p>
              {apiStatus.lastTestMessage && (
                <p>
                  <strong>Last Test:</strong> {apiStatus.lastTestMessage}
                </p>
              )}
            </div>
          </div>
        )}

        <div className='mt-6 p-4 bg-gray-50 rounded-md'>
          <div className='text-sm text-gray-600'>
            <p>
              <strong>Last Status Change:</strong>{' '}
              {new Date(botStatus.lastStatusChange).toLocaleString()}
            </p>
            <p>
              <strong>Reason:</strong> {botStatus.statusChangeReason}
            </p>
            {botStatus.message && (
              <p>
                <strong>Message:</strong> {botStatus.message}
              </p>
            )}
          </div>
        </div>
      </div>

      <BotResultModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        result={modalResult}
        title={modalTitle}
      />
    </>
  );
}
