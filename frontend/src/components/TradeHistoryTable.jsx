import { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { ArrowUpRight, ArrowDownRight, RefreshCw } from 'lucide-react';
import ApiService from '../services/api';

const TradeHistoryTable = forwardRef((props, ref) => {
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortField, setSortField] = useState('timestamp');
  const [sortDirection, setSortDirection] = useState('desc');
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    fetchTrades();
    const interval = setInterval(fetchTrades, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchTrades = async () => {
    try {
      setLoading(true);
      setError(null);
      const tradesData = await ApiService.getTradeHistory();

      const formattedTrades = tradesData.map((trade, index) => ({
        id: index + 1,
        date: trade.timestamp,
        action: trade.action,
        symbol: trade.symbol.toUpperCase(),
        quantity: parseFloat(trade.quantity),
        price: parseFloat(trade.price),
        profitLoss: trade.profit_loss ? parseFloat(trade.profit_loss) : null,
      }));

      setTrades(formattedTrades);
    } catch (error) {
      console.error('Failed to fetch trades:', error);
      setError('Failed to load trade history');
    } finally {
      setLoading(false);
    }
  };

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('desc');
    }
  };

  const filteredTrades = trades.filter((trade) => {
    if (filter === 'all') return true;
    if (filter === 'profit') return trade.profitLoss > 0;
    if (filter === 'loss') return trade.profitLoss < 0;
    if (filter === 'buy') return trade.action === 'BUY';
    if (filter === 'sell') return trade.action === 'SELL';
    return true;
  });

  const sortedTrades = [...filteredTrades].sort((a, b) => {
    let aVal = a[sortField];
    let bVal = b[sortField];

    if (sortField === 'date') {
      aVal = new Date(aVal);
      bVal = new Date(bVal);
    }

    if (sortDirection === 'asc') {
      return aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
    }
    return aVal > bVal ? -1 : aVal < bVal ? 1 : 0;
  });

  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  useImperativeHandle(ref, () => ({
    refreshData: fetchTrades,
  }));

  if (loading) {
    return (
      <div className='bg-white rounded-lg shadow-sm border border-gray-200 p-6'>
        <div className='flex justify-center items-center h-40'>
          <RefreshCw className='w-6 h-6 animate-spin text-blue-600' />
          <span className='ml-2 text-gray-600'>Loading trade history...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='bg-white rounded-lg shadow-sm border border-gray-200 p-6'>
        <div className='flex justify-center items-center h-40 text-red-600'>
          <span>{error}</span>
          <button
            onClick={fetchTrades}
            className='ml-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700'
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className='bg-white rounded-lg shadow-sm border border-gray-200 p-6'>
      <div className='flex justify-between items-center mb-6'>
        <h2 className='text-xl font-semibold text-gray-900'>Trade History</h2>
        <div className='flex gap-2'>
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className='px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500'
          >
            <option value='all'>All Trades</option>
            <option value='buy'>Buy Only</option>
            <option value='sell'>Sell Only</option>
            <option value='profit'>Profitable</option>
            <option value='loss'>Losses</option>
          </select>
          <button
            onClick={fetchTrades}
            className='px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
          >
            <RefreshCw className='w-4 h-4' />
          </button>
        </div>
      </div>

      <div className='overflow-x-auto'>
        <table className='w-full'>
          <thead>
            <tr className='border-b border-gray-200'>
              <th
                className='text-left py-3 px-4 font-medium text-gray-600 cursor-pointer hover:text-gray-900 select-none'
                onClick={() => handleSort('date')}
              >
                <div className='flex items-center gap-1'>
                  Date
                  {sortField === 'date' &&
                    (sortDirection === 'asc' ? (
                      <ArrowUpRight className='w-4 h-4' />
                    ) : (
                      <ArrowDownRight className='w-4 h-4' />
                    ))}
                </div>
              </th>
              <th
                className='text-left py-3 px-4 font-medium text-gray-600 cursor-pointer hover:text-gray-900 select-none'
                onClick={() => handleSort('action')}
              >
                <div className='flex items-center gap-1'>
                  Action
                  {sortField === 'action' &&
                    (sortDirection === 'asc' ? (
                      <ArrowUpRight className='w-4 h-4' />
                    ) : (
                      <ArrowDownRight className='w-4 h-4' />
                    ))}
                </div>
              </th>
              <th className='text-left py-3 px-4 font-medium text-gray-600'>
                Symbol
              </th>
              <th
                className='text-right py-3 px-4 font-medium text-gray-600 cursor-pointer hover:text-gray-900 select-none'
                onClick={() => handleSort('quantity')}
              >
                <div className='flex items-center justify-end gap-1'>
                  Quantity
                  {sortField === 'quantity' &&
                    (sortDirection === 'asc' ? (
                      <ArrowUpRight className='w-4 h-4' />
                    ) : (
                      <ArrowDownRight className='w-4 h-4' />
                    ))}
                </div>
              </th>
              <th
                className='text-right py-3 px-4 font-medium text-gray-600 cursor-pointer hover:text-gray-900 select-none'
                onClick={() => handleSort('price')}
              >
                <div className='flex items-center justify-end gap-1'>
                  Price
                  {sortField === 'price' &&
                    (sortDirection === 'asc' ? (
                      <ArrowUpRight className='w-4 h-4' />
                    ) : (
                      <ArrowDownRight className='w-4 h-4' />
                    ))}
                </div>
              </th>
              <th
                className='text-right py-3 px-4 font-medium text-gray-600 cursor-pointer hover:text-gray-900 select-none'
                onClick={() => handleSort('profitLoss')}
              >
                <div className='flex items-center justify-end gap-1'>
                  P&L
                  {sortField === 'profitLoss' &&
                    (sortDirection === 'asc' ? (
                      <ArrowUpRight className='w-4 h-4' />
                    ) : (
                      <ArrowDownRight className='w-4 h-4' />
                    ))}
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            {sortedTrades.map((trade) => (
              <tr
                key={trade.id}
                className='border-b border-gray-100 hover:bg-gray-50 transition-colors'
              >
                <td className='py-4 px-4 text-sm text-gray-900'>
                  {formatDate(trade.date)}
                </td>
                <td className='py-4 px-4'>
                  <span
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      trade.action === 'BUY'
                        ? 'bg-green-100 text-green-800'
                        : trade.action === 'SELL'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-blue-100 text-blue-800'
                    }`}
                  >
                    {trade.action}
                  </span>
                </td>
                <td className='py-4 px-4 text-sm font-medium text-gray-900'>
                  {trade.symbol}
                </td>
                <td className='py-4 px-4 text-sm text-gray-900 text-right'>
                  {trade.quantity.toFixed(6)}
                </td>
                <td className='py-4 px-4 text-sm text-gray-900 text-right'>
                  {formatCurrency(trade.price)}
                </td>
                <td className='py-4 px-4 text-sm text-right'>
                  {trade.profitLoss !== null ? (
                    <span
                      className={`font-medium ${
                        trade.profitLoss >= 0
                          ? 'text-green-600'
                          : 'text-red-600'
                      }`}
                    >
                      {trade.profitLoss >= 0 ? '+' : ''}
                      {formatCurrency(trade.profitLoss)}
                    </span>
                  ) : (
                    <span className='text-gray-400'>-</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {sortedTrades.length === 0 && (
        <div className='text-center py-8 text-gray-500'>
          No trades found matching the selected filter.
        </div>
      )}
    </div>
  );
});

export default TradeHistoryTable;
