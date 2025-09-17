import PortfolioOverview from './PortfolioOverview';
import TradeHistoryTable from './TradeHistoryTable';

const sampleTrades = [
  {
    id: 1,
    date: '2024-09-15',
    action: 'BUY',
    symbol: 'AAPL',
    quantity: 100,
    price: 175.5,
    profitLoss: null,
    status: 'Open',
  },
  {
    id: 2,
    date: '2024-09-14',
    action: 'SELL',
    symbol: 'TSLA',
    quantity: 50,
    price: 245.75,
    profitLoss: 1250.5,
    status: 'Closed',
  },
  {
    id: 3,
    date: '2024-09-13',
    action: 'BUY',
    symbol: 'TSLA',
    quantity: 50,
    price: 220.74,
    profitLoss: null,
    status: 'Closed',
  },
  {
    id: 4,
    date: '2024-09-12',
    action: 'SELL',
    symbol: 'GOOGL',
    quantity: 25,
    price: 138.2,
    profitLoss: -125.75,
    status: 'Closed',
  },
  {
    id: 5,
    date: '2024-09-11',
    action: 'BUY',
    symbol: 'GOOGL',
    quantity: 25,
    price: 143.23,
    profitLoss: null,
    status: 'Closed',
  },
  {
    id: 6,
    date: '2024-09-10',
    action: 'BUY',
    symbol: 'MSFT',
    quantity: 75,
    price: 412.3,
    profitLoss: null,
    status: 'Open',
  },
];

const portfolioData = [
  { date: '2024-09-01', value: 50000, dailyChange: 0 },
  { date: '2024-09-02', value: 50250, dailyChange: 250 },
  { date: '2024-09-03', value: 49800, dailyChange: -450 },
  { date: '2024-09-04', value: 51200, dailyChange: 1400 },
  { date: '2024-09-05', value: 51750, dailyChange: 550 },
  { date: '2024-09-06', value: 51100, dailyChange: -650 },
  { date: '2024-09-07', value: 52300, dailyChange: 1200 },
  { date: '2024-09-08', value: 52800, dailyChange: 500 },
  { date: '2024-09-09', value: 51900, dailyChange: -900 },
  { date: '2024-09-10', value: 53450, dailyChange: 1550 },
  { date: '2024-09-11', value: 54200, dailyChange: 750 },
  { date: '2024-09-12', value: 53800, dailyChange: -400 },
  { date: '2024-09-13', value: 55100, dailyChange: 1300 },
  { date: '2024-09-14', value: 56350, dailyChange: 1250 },
  { date: '2024-09-15', value: 55900, dailyChange: -450 },
];

export default function TradingDashboard() {
  return (
    <div className='min-h-screen bg-gray-50 p-6'>
      <div className='max-w-7xl mx-auto space-y-8'>
        <div className='flex justify-between items-center'>
          <h1 className='text-3xl font-bold text-gray-900'>
            Trading Dashboard
          </h1>
          <div className='text-sm text-gray-500'>
            Last updated: {new Date().toLocaleString()}
          </div>
        </div>
        <PortfolioOverview portfolioData={portfolioData} />
        <TradeHistoryTable trades={sampleTrades} />
      </div>
    </div>
  );
}
