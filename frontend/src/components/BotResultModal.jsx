import {
  X,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';

export default function BotResultModal({ isOpen, onClose, result, title }) {
  if (!isOpen || !result) return null;

  const getStatusIcon = (success) => {
    return success ? (
      <CheckCircle className='w-6 h-6 text-green-600' />
    ) : (
      <AlertCircle className='w-6 h-6 text-red-600' />
    );
  };

  const getActionIcon = (action) => {
    switch (action) {
      case 'BUY':
        return <TrendingUp className='w-4 h-4 text-green-600' />;
      case 'SELL':
        return <TrendingDown className='w-4 h-4 text-red-600' />;
      default:
        return null;
    }
  };

  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto'>
        <div className='flex justify-between items-center p-6 border-b'>
          <div className='flex items-center gap-3'>
            {getStatusIcon(result.success)}
            <h2 className='text-xl font-semibold text-gray-900'>{title}</h2>
          </div>
          <button
            onClick={onClose}
            className='text-gray-400 hover:text-gray-600'
          >
            <X className='w-6 h-6' />
          </button>
        </div>

        <div className='p-6'>
          <div className='mb-6'>
            <h3 className='text-lg font-medium text-gray-900 mb-2'>Summary</h3>
            <p className='text-gray-700 bg-gray-50 p-3 rounded-md'>
              {result.summary}
            </p>
          </div>

          {result.analyses && result.analyses.length > 0 && (
            <div className='mb-6'>
              <h3 className='text-lg font-medium text-gray-900 mb-4'>
                Analysis Results
              </h3>
              <div className='space-y-4'>
                {result.analyses.map((analysis, index) => (
                  <div
                    key={index}
                    className='border border-gray-200 rounded-lg p-4'
                  >
                    <div className='flex justify-between items-start mb-3'>
                      <div>
                        <h4 className='font-medium text-gray-900 uppercase'>
                          {analysis.coinId}
                        </h4>
                        <p className='text-sm text-gray-600'>
                          {analysis.status}
                        </p>
                      </div>
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          analysis.signal === 'BUY'
                            ? 'bg-green-100 text-green-800'
                            : analysis.signal === 'SELL'
                            ? 'bg-red-100 text-red-800'
                            : analysis.signal === 'HOLD'
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {analysis.signal}
                      </span>
                    </div>

                    <div className='grid grid-cols-3 gap-4 mb-3 text-sm'>
                      <div>
                        <span className='text-gray-500'>Current Price:</span>
                        <p className='font-medium'>
                          ${analysis.currentPrice?.toFixed(2) || 'N/A'}
                        </p>
                      </div>
                      <div>
                        <span className='text-gray-500'>Short SMA:</span>
                        <p className='font-medium'>
                          ${analysis.shortTermSMA?.toFixed(2) || 'N/A'}
                        </p>
                      </div>
                      <div>
                        <span className='text-gray-500'>Long SMA:</span>
                        <p className='font-medium'>
                          ${analysis.longTermSMA?.toFixed(2) || 'N/A'}
                        </p>
                      </div>
                    </div>

                    {analysis.tradeResult && (
                      <div className='bg-gray-50 rounded-md p-3'>
                        <div className='flex items-center justify-between mb-2'>
                          <div className='flex items-center gap-2'>
                            {getActionIcon(analysis.tradeResult.action)}
                            <span className='font-medium'>
                              {analysis.tradeResult.action}
                            </span>
                          </div>
                          <span
                            className={`text-sm font-medium ${
                              analysis.tradeResult.success
                                ? 'text-green-600'
                                : 'text-red-600'
                            }`}
                          >
                            {analysis.tradeResult.success
                              ? 'Success'
                              : 'Failed'}
                          </span>
                        </div>

                        {analysis.tradeResult.success && (
                          <div className='grid grid-cols-2 gap-4 text-sm'>
                            <div>
                              <span className='text-gray-500'>Quantity:</span>
                              <p className='font-medium'>
                                {analysis.tradeResult.quantity?.toFixed(6) ||
                                  'N/A'}
                              </p>
                            </div>
                            <div>
                              <span className='text-gray-500'>
                                Total Value:
                              </span>
                              <p className='font-medium'>
                                $
                                {analysis.tradeResult.totalValue?.toFixed(2) ||
                                  'N/A'}
                              </p>
                            </div>
                          </div>
                        )}

                        <p className='text-sm text-gray-600 mt-2'>
                          {analysis.tradeResult.message}
                        </p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {result.totalReturn !== undefined && (
            <div className='mb-6'>
              <h3 className='text-lg font-medium text-gray-900 mb-4'>
                Backtest Results
              </h3>
              <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
                <div className='text-center p-3 bg-blue-50 rounded-lg'>
                  <p className='text-sm text-blue-600'>Initial Balance</p>
                  <p className='text-lg font-semibold text-blue-900'>
                    ${result.initialBalance?.toFixed(2) || 'N/A'}
                  </p>
                </div>
                <div className='text-center p-3 bg-green-50 rounded-lg'>
                  <p className='text-sm text-green-600'>Final Value</p>
                  <p className='text-lg font-semibold text-green-900'>
                    ${result.finalBalance?.toFixed(2) || 'N/A'}
                  </p>
                </div>
                <div className='text-center p-3 bg-purple-50 rounded-lg'>
                  <p className='text-sm text-purple-600'>Total Return</p>
                  <p className='text-lg font-semibold text-purple-900'>
                    ${result.totalReturn?.toFixed(2) || 'N/A'}
                  </p>
                </div>
                <div className='text-center p-3 bg-yellow-50 rounded-lg'>
                  <p className='text-sm text-yellow-600'>Return %</p>
                  <p className='text-lg font-semibold text-yellow-900'>
                    {result.totalReturnPercentage?.toFixed(2) || 'N/A'}%
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className='flex justify-end gap-3 p-6 border-t bg-gray-50'>
          <button
            onClick={onClose}
            className='px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700'
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
