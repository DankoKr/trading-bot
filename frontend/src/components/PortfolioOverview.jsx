import { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { RefreshCw } from 'lucide-react';
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
  const [totalPortfolioValue, setTotalPortfolioValue] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPortfolioData();
    const interval = setInterval(fetchPortfolioData, 50000);
    return () => clearInterval(interval);
  }, []);

  const fetchPortfolioData = async () => {
    try {
      setLoading(true);
      setError(null);

      const balanceHistory = await ApiService.get('/user/balance/history');

      const chartData = balanceHistory.map((entry) => ({
        date: entry.date,
        value: parseFloat(entry.total_portfolio_value),
        cashBalance: parseFloat(entry.cash_balance),
        holdingsValue: parseFloat(entry.holdings_value),
      }));

      setPortfolioData(chartData);

      if (chartData.length > 0) {
        const latest = chartData[0];
        setTotalPortfolioValue(latest.value);
        setCurrentBalance(latest.cashBalance);
      }
    } catch (error) {
      console.error('Failed to fetch portfolio data:', error);
      setError('Failed to load portfolio data');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const calculatePercentageChange = () => {
    if (portfolioData.length < 2) return 0;
    const latest = portfolioData[0].value;
    const previous = portfolioData[portfolioData.length - 1].value;
    return ((latest - previous) / previous) * 100;
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

  const percentageChange = calculatePercentageChange();
  const isPositive = percentageChange >= 0;

  return (
    <div>
      <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
        <div className='flex justify-between items-center mb-6'>
          <div>
            <h2 className='text-xl font-semibold text-gray-900'>
              Portfolio Performance
            </h2>
            <div className='flex items-center gap-4 mt-2'>
              <div className='text-2xl font-bold text-gray-900'>
                {formatCurrency(totalPortfolioValue)}
              </div>
              <div
                className={`flex items-center text-sm ${
                  isPositive ? 'text-green-600' : 'text-red-600'
                }`}
              >
                <span>
                  {isPositive ? '+' : ''}
                  {percentageChange.toFixed(2)}%
                </span>
                <span className='ml-1 text-gray-500'>10 days</span>
              </div>
            </div>
          </div>
          <div className='flex gap-2'>
            <button
              onClick={fetchPortfolioData}
              className='px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
            >
              <RefreshCw className='w-4 h-4' />
            </button>
          </div>
        </div>

        {/* Portfolio breakdown */}
        <div className='grid grid-cols-2 gap-4 mb-6'>
          <div className='bg-gray-50 p-3 rounded-lg'>
            <div className='text-sm text-gray-600'>Cash Balance</div>
            <div className='text-lg font-semibold text-gray-900'>
              {formatCurrency(currentBalance)}
            </div>
          </div>
          <div className='bg-gray-50 p-3 rounded-lg'>
            <div className='text-sm text-gray-600'>Holdings Value</div>
            <div className='text-lg font-semibold text-gray-900'>
              {formatCurrency(totalPortfolioValue - currentBalance)}
            </div>
          </div>
        </div>

        <div className='h-80'>
          <ResponsiveContainer width='100%' height='100%'>
            <AreaChart data={portfolioData.slice().reverse()}>
              {' '}
              {/* Reverse for chronological order */}
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
                formatter={(value, name) => {
                  if (name === 'value')
                    return [formatCurrency(value), 'Total Portfolio'];
                  if (name === 'cashBalance')
                    return [formatCurrency(value), 'Cash Balance'];
                  if (name === 'holdingsValue')
                    return [formatCurrency(value), 'Holdings Value'];
                  return [formatCurrency(value), name];
                }}
                labelFormatter={(date) =>
                  new Date(date).toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                  })
                }
                content={({ active, payload, label }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className='bg-white p-3 border border-gray-200 rounded-lg shadow-lg'>
                        <p className='text-sm text-gray-600 mb-2'>
                          {new Date(label).toLocaleDateString('en-US', {
                            weekday: 'short',
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          })}
                        </p>
                        <div className='space-y-1'>
                          <div className='flex justify-between items-center'>
                            <span className='text-sm text-gray-600'>
                              Total Portfolio:
                            </span>
                            <span className='font-semibold'>
                              {formatCurrency(data.value)}
                            </span>
                          </div>
                          <div className='flex justify-between items-center'>
                            <span className='text-sm text-gray-600'>Cash:</span>
                            <span className='text-sm'>
                              {formatCurrency(data.cashBalance)}
                            </span>
                          </div>
                          <div className='flex justify-between items-center'>
                            <span className='text-sm text-gray-600'>
                              Holdings:
                            </span>
                            <span className='text-sm'>
                              {formatCurrency(data.holdingsValue)}
                            </span>
                          </div>
                        </div>
                      </div>
                    );
                  }
                  return null;
                }}
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
