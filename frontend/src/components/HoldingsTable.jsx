import { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { RefreshCw, TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import ApiService from '../services/api';

const HoldingsTable = forwardRef((props, ref) => {
  const [holdings, setHoldings] = useState([]);
  const [portfolioSummary, setPortfolioSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchHoldings();
    const interval = setInterval(fetchHoldings, 10000);
    return () => clearInterval(interval);
  }, []);

  useImperativeHandle(ref, () => ({
    refreshData: fetchHoldings,
  }));

  const fetchHoldings = async () => {
    try {
      setLoading(true);
      setError(null);

      const [detailedHoldings, summary] = await Promise.all([
        ApiService.getDetailedHoldings(),
        ApiService.getPortfolioSummary(),
      ]);

      setHoldings(detailedHoldings);
      setPortfolioSummary(summary);
    } catch (error) {
      console.error('Failed to fetch holdings:', error);
      setError('Failed to load holdings data');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatCrypto = (amount) => {
    if (amount === null || amount === undefined) return '0.000000';
    return parseFloat(amount).toFixed(6);
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

  if (loading && holdings.length === 0) {
    return (
      <div className='bg-white rounded-lg shadow-sm border border-gray-200 p-6'>
        <div className='flex justify-center items-center h-40'>
          <RefreshCw className='w-6 h-6 animate-spin text-blue-600' />
          <span className='ml-2 text-gray-600'>Loading holdings...</span>
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
            onClick={fetchHoldings}
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
      <div className='bg-white rounded-lg shadow-sm border border-gray-200 p-6'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-xl font-semibold text-gray-900'>
            Current Holdings
          </h2>
          <button
            onClick={fetchHoldings}
            className='px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
          >
            <RefreshCw className='w-4 h-4' />
          </button>
        </div>

        {holdings.length === 0 ? (
          <div className='text-center py-8'>
            <TrendingDown className='w-12 h-12 text-gray-400 mx-auto mb-4' />
            <h3 className='text-lg font-medium text-gray-900 mb-2'>
              No Holdings
            </h3>
            <p className='text-gray-500'>
              You don't have any cryptocurrency holdings yet. Start trading to
              build your portfolio!
            </p>
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <table className='w-full'>
              <thead>
                <tr className='border-b border-gray-200'>
                  <th className='text-left py-3 px-4 font-medium text-gray-600'>
                    Cryptocurrency
                  </th>
                  <th className='text-right py-3 px-4 font-medium text-gray-600'>
                    Quantity
                  </th>
                  <th className='text-right py-3 px-4 font-medium text-gray-600'>
                    Current Price
                  </th>
                  <th className='text-right py-3 px-4 font-medium text-gray-600'>
                    Current Value
                  </th>
                  <th className='text-right py-3 px-4 font-medium text-gray-600'>
                    Date Acquired
                  </th>
                </tr>
              </thead>
              <tbody>
                {holdings.map((holding, index) => (
                  <tr
                    key={index}
                    className='border-b border-gray-100 hover:bg-gray-50 transition-colors'
                  >
                    <td className='py-4 px-4'>
                      <div className='flex items-center'>
                        <div className='w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3'>
                          <span className='text-blue-600 font-semibold text-sm'>
                            {holding.symbol.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div>
                          <p className='font-medium text-gray-900 uppercase'>
                            {holding.symbol}
                          </p>
                          <p className='text-sm text-gray-500'>
                            {holding.symbol.charAt(0).toUpperCase() +
                              holding.symbol.slice(1)}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className='py-4 px-4 text-right'>
                      <span className='font-medium text-gray-900'>
                        {formatCrypto(holding.quantity)}
                      </span>
                    </td>
                    <td className='py-4 px-4 text-right'>
                      <span className='font-medium text-gray-900'>
                        {formatCurrency(holding.current_price)}
                      </span>
                    </td>
                    <td className='py-4 px-4 text-right'>
                      <span className='font-semibold text-green-600'>
                        {formatCurrency(holding.current_value)}
                      </span>
                    </td>
                    <td className='py-4 px-4 text-right text-sm text-gray-500'>
                      {formatDate(holding.created_at)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
});

export default HoldingsTable;
