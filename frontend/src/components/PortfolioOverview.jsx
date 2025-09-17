import MetricCard from './MetricCard';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  Activity,
  Calendar,
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

export default function PortfolioOverview({ portfolioData }) {
  const currentValue = portfolioData[portfolioData.length - 1]?.value || 0;
  const previousValue = portfolioData[portfolioData.length - 2]?.value || 0;
  const dailyChange = currentValue - previousValue;
  const dailyChangePercent =
    previousValue !== 0 ? (dailyChange / previousValue) * 100 : 0;

  const initialValue = portfolioData[0]?.value || 0;
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
          title='Period'
          value='15d'
          subtitle='Sep 1-15, 2024'
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
            <button className='px-3 py-1 text-sm rounded-md bg-blue-100 text-blue-700 font-medium'>
              15D
            </button>
            <button className='px-3 py-1 text-sm rounded-md text-gray-600 hover:bg-gray-100'>
              1M
            </button>
            <button className='px-3 py-1 text-sm rounded-md text-gray-600 hover:bg-gray-100'>
              3M
            </button>
            <button className='px-3 py-1 text-sm rounded-md text-gray-600 hover:bg-gray-100'>
              1Y
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
}
