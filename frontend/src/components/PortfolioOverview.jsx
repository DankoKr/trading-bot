import { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import MetricCard from './MetricCard';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  Activity,
  Calendar,
  RefreshCw,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
} from 'recharts';
import ApiService from '../services/api';

const PortfolioOverview = forwardRef((props, ref) => {
  const [portfolioData, setPortfolioData] = useState([]);
  const [currentBalance, setCurrentBalance] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPortfolioData();
    const interval = setInterval(fetchPortfolioData, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchPortfolioData = async () => {
    try {
      setLoading(true);
      setError(null);

      const balance = await ApiService.getAccountBalance();
      setCurrentBalance(balance || 0);
      const trades = await ApiService.getTradeHistory();

      const portfolioHistory = generatePortfolioHistory(trades, balance);
      setPortfolioData(portfolioHistory);
    } catch (error) {
      console.error('Failed to fetch portfolio data:', error);
      setError('Failed to load portfolio data');
    } finally {
      setLoading(false);
    }
  };

  const generatePortfolioHistory = (trades, currentBalance) => {
    const initialBalance = 1000;
    const data = [];

    for (let i = 29; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);

      const progress = (29 - i) / 29;
      const value =
        initialBalance + (currentBalance - initialBalance) * progress;

      data.push({
        date: date.toISOString().split('T')[0],
        value: Math.max(0, value),
      });
    }

    return data;
  };

  const currentValue =
    portfolioData.length > 0
      ? portfolioData[portfolioData.length - 1]?.value || 0
      : currentBalance;
  const previousValue =
    portfolioData.length > 1
      ? portfolioData[portfolioData.length - 2]?.value || 0
      : currentBalance;
  const dailyChange = currentValue - previousValue;
  const dailyChangePercent =
    previousValue !== 0 ? (dailyChange / previousValue) * 100 : 0;

  const initialValue =
    portfolioData.length > 0 ? portfolioData[0]?.value || 1000 : 1000;
  const totalReturn = currentValue - initialValue;
  const totalReturnPercent =
    initialValue !== 0 ? (totalReturn / initialValue) * 100 : 0;

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatPercent = (percent) => {
    return `${percent >= 0 ? '+' : ''}${percent.toFixed(2)}%`;
  };

  useImperativeHandle(ref, () => ({
    refreshData: fetchPortfolioData,
  }));

  if (loading && portfolioData.length === 0) {
    return (
      <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
        <div className='flex justify-center items-center h-40'>
          <RefreshCw className='w-6 h-6 animate-spin text-blue-600' />
          <span className='ml-2 text-gray-600'>Loading portfolio data...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
        <div className='flex justify-center items-center h-40 text-red-600'>
          <span>{error}</span>
          <button
            onClick={fetchPortfolioData}
            className='ml-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700'
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <MetricCard
          title='Portfolio Value'
          value={formatCurrency(currentValue)}
          icon={<DollarSign className='w-6 h-6 text-blue-600' />}
          iconBg='bg-blue-100'
        />
        <MetricCard
          title='Daily Change'
          value={formatCurrency(dailyChange)}
          subtitle={formatPercent(dailyChangePercent)}
          icon={
            dailyChange >= 0 ? (
              <TrendingUp className='w-6 h-6 text-green-600' />
            ) : (
              <TrendingDown className='w-6 h-6 text-red-600' />
            )
          }
          iconBg={dailyChange >= 0 ? 'bg-green-100' : 'bg-red-100'}
          valueClass={dailyChange >= 0 ? 'text-green-600' : 'text-red-600'}
          subtitleClass={dailyChange >= 0 ? 'text-green-600' : 'text-red-600'}
        />
        <MetricCard
          title='Total Return'
          value={formatCurrency(totalReturn)}
          subtitle={formatPercent(totalReturnPercent)}
          icon={
            <Activity
              className={`w-6 h-6 ${
                totalReturn >= 0 ? 'text-green-600' : 'text-red-600'
              }`}
            />
          }
          iconBg={totalReturn >= 0 ? 'bg-green-100' : 'bg-red-100'}
          valueClass={totalReturn >= 0 ? 'text-green-600' : 'text-red-600'}
          subtitleClass={totalReturn >= 0 ? 'text-green-600' : 'text-red-600'}
        />
        <MetricCard
          title='Account Balance'
          value={formatCurrency(currentBalance)}
          subtitle='Available Cash'
          icon={<Calendar className='w-6 h-6 text-purple-600' />}
          iconBg='bg-purple-100'
        />
      </div>

      <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-xl font-semibold text-gray-900'>
            Portfolio Performance
          </h2>
          <div className='flex gap-2'>
            <button
              onClick={fetchPortfolioData}
              className='px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
            >
              <RefreshCw className='w-4 h-4' />
            </button>
          </div>
        </div>

        <div className='h-80'>
          <ResponsiveContainer width='100%' height='100%'>
            <AreaChart data={portfolioData}>
              <defs>
                <linearGradient id='valueGradient' x1='0' y1='0' x2='0' y2='1'>
                  <stop offset='5%' stopColor='#3B82F6' stopOpacity={0.1} />
                  <stop offset='95%' stopColor='#3B82F6' stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray='3 3' stroke='#f0f0f0' />
              <XAxis
                dataKey='date'
                axisLine={false}
                tickLine={false}
                tick={{ fontSize: 12, fill: '#6B7280' }}
                tickFormatter={(date) =>
                  new Date(date).toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                  })
                }
              />
              <YAxis
                axisLine={false}
                tickLine={false}
                tick={{ fontSize: 12, fill: '#6B7280' }}
                tickFormatter={(value) => `$${(value / 1000).toFixed(0)}K`}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #E5E7EB',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                }}
                formatter={(value, name) => [
                  formatCurrency(value),
                  'Portfolio Value',
                ]}
                labelFormatter={(date) =>
                  new Date(date).toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                  })
                }
              />
              <Area
                type='monotone'
                dataKey='value'
                stroke='#3B82F6'
                strokeWidth={2}
                fill='url(#valueGradient)'
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
});

export default PortfolioOverview;
