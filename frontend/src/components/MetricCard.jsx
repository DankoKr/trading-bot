export default function MetricCard({
  title,
  value,
  subtitle,
  icon,
  iconBg,
  valueClass,
  subtitleClass,
}) {
  return (
    <div className='bg-white p-6 rounded-lg shadow-sm border border-gray-200'>
      <div className='flex items-center justify-between'>
        <div>
          <p className='text-sm font-medium text-gray-600'>{title}</p>
          <p className={`text-2xl font-bold ${valueClass}`}>{value}</p>
          {subtitle && <p className={`text-sm ${subtitleClass}`}>{subtitle}</p>}
        </div>
        <div className={`p-3 rounded-full ${iconBg}`}>{icon}</div>
      </div>
    </div>
  );
}
